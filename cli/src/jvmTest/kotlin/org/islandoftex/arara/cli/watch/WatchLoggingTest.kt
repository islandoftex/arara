// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LogRetention
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.utils.EventLog
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.configuration.LoggingOptions

class WatchLoggingTest {

    @Test
    fun successfulCyclesRemoveOnlyTheirBytesAndLaterFailuresAppend() {
        val root = Files.createTempDirectory("arara-watch-log").toMPPPath()
        val log = root.resolve("session.log")
        val previous = LoggingUtils.beginRun()
        try {
            val options = LoggingOptions(logFile = log, format = LogFormat.JSON, retention = LogRetention.ON_FAILURE)
            LoggingUtils.beginWatch()
            fun cycle(message: String, successful: Boolean) {
                LoggingUtils.beginWatchCycle()
                LoggingUtils.setupLogging(options)
                EventLog.info("watch.test", message, emptyMap())
                LoggingUtils.finishRun(successful)
            }
            cycle("first-success", true)
            assertFalse(log.exists)
            cycle("first-failure", false)
            val failed = log.readText()
            cycle("second-success", true)
            assertTrue(log.readText() == failed)
            cycle("second-failure", false)
            assertTrue(log.readText().contains("first-failure"))
            assertTrue(log.readText().contains("second-failure"))
            assertFalse(log.readText().contains("success"))
        } finally {
            LoggingUtils.finishRun(false)
            LoggingUtils.endWatch()
            LoggingUtils.restoreRun(previous)
            root.removeDirectory()
        }
    }
}
