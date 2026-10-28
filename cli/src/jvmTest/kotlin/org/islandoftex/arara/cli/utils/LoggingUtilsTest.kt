// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.apache.logging.log4j.ThreadContext
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.junit.jupiter.api.io.TempDir

class LoggingUtilsTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `properly set log file path`() {
        val options = LoggingOptions(LogFormat.TEXT, logFile = temporaryDirectory.resolve("arara.log").toMPPPath())
        LoggingUtils.init()
        try {
            assertNull(ThreadContext.get("araraLogFile"))
            LoggingUtils.setupLogging(options)
            assertEquals(options.logFile.normalize().toString(), ThreadContext.get("araraLogFile"))
        } finally {
            LoggingUtils.init()
        }
    }
}
