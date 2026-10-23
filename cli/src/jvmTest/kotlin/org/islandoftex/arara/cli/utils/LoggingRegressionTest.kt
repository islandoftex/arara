// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.LoggingOptions

class LoggingRegressionTest {
    @BeforeTest
    fun setup() { LoggingUtils.init() }

    @AfterTest
    fun teardown() { LoggingUtils.init() }

    @Test
    fun appendPreservesExistingRecordsAcrossRepeatedConfiguration() {
        val file = Files.createTempFile("arara-log", ".log").toMPPPath()
        try {
            file.writeText("previous-record\n")
            val options = LoggingOptions(LogFormat.TEXT, true, file)
            LoggingUtils.setupLogging(options)
            LogManager.getLogger("regression").info("first-record")
            LoggingUtils.setupLogging(options)
            LogManager.getLogger("regression").info("second-record")
            LoggingUtils.init()
            val text = file.readText()
            assertTrue(text.contains("previous-record"))
            assertTrue(text.contains("first-record"))
            assertTrue(text.contains("second-record"))
            assertNull(ThreadContext.get("araraLogFile"))
        } finally {
            LoggingUtils.init()
            Files.deleteIfExists(file.toJVMPath())
        }
    }

    @Test
    fun overwriteHappensOnceAndDisablingLoggingClosesTheAppender() {
        val file = Files.createTempFile("arara-log-overwrite", ".log").toMPPPath()
        try {
            file.writeText("old-record\n")
            val options = LoggingOptions(LogFormat.TEXT, false, file)
            LoggingUtils.setupLogging(options)
            LogManager.getLogger("regression").info("new-record")
            LoggingUtils.setupLogging(options)
            val context = LogManager.getContext(false) as org.apache.logging.log4j.core.LoggerContext
            val appender = context.configuration.getAppender<org.apache.logging.log4j.core.appender.FileAppender>("File")
            LoggingUtils.init()
            assertTrue(appender.isStopped)
            val text = file.readText()
            assertFalse(text.contains("old-record"))
            assertTrue(text.contains("new-record"))
        } finally {
            LoggingUtils.init()
            Files.deleteIfExists(file.toJVMPath())
        }
    }
}
