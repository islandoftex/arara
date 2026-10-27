// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.apache.logging.log4j.LogManager
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.configuration.LogRetention
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.LoggingOptions

class LogRetentionTest {
    private fun fixture(test: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara retention ").toMPPPath()
        LoggingUtils.init()
        val previous = LoggingUtils.beginRun()
        try {
            test(root)
        } finally {
            LoggingUtils.finishRun(false)
            LoggingUtils.restoreRun(previous)
            root.removeDirectory()
        }
    }

    @Test
    fun successDeletesOverwriteLogsButFailureKeepsThemInBothFormats() {
        for (format in listOf(LogFormat.TEXT, LogFormat.JSON)) {
            for (successful in listOf(true, false)) fixture { root ->
                val file = root.resolve("arara.log")
                file.writeText("old log\n")
                LoggingUtils.setupLogging(LoggingOptions(format, false, file, retention = LogRetention.ON_FAILURE))
                LogManager.getLogger("retention").info("new log")
                LoggingUtils.finishRun(successful)
                assertEquals(!successful, file.exists)
                if (!successful) {
                    assertTrue("new log" in file.readText())
                    assertFalse("old log" in file.readText())
                }
            }
        }
    }

    @Test
    fun successfulAppendRestoresOriginalBytesIncludingUnicode() = fixture { root ->
        val file = root.resolve("arara.log")
        val original = "previous run: ação 目录\r\n"
        file.writeText(original)
        LoggingUtils.setupLogging(LoggingOptions(LogFormat.TEXT, true, file, retention = LogRetention.ON_FAILURE))
        LogManager.getLogger("retention").error("an error message does not determine the outcome")
        LoggingUtils.finishRun(true)
        assertEquals(original, file.readText())
    }

    @Test
    fun successfulAppendDeletesNewFilesButPreservesExistingEmptyFiles() = fixture { root ->
        val empty = root.resolve("empty.log")
        empty.writeText("")
        val fresh = root.resolve("fresh.log")
        for (file in listOf(empty, fresh)) {
            LoggingUtils.setupLogging(LoggingOptions(LogFormat.JSON, true, file,
                level = LogLevel.ERROR, retention = LogRetention.ON_FAILURE))
            LogManager.getLogger("retention").warn("warning")
        }
        LoggingUtils.finishRun(true)
        assertTrue(empty.exists)
        assertEquals("", empty.readText())
        assertFalse(fresh.exists)
    }

    @Test
    fun revisitedDestinationsKeepAlwaysSegmentsAndDiscardOnlySuccessfulConditionalSegments() = fixture { root ->
        val file = root.resolve("shared.log")
        val other = root.resolve("other.log")
        file.writeText("previous run\n")
        val conditional = LoggingOptions(LogFormat.TEXT, true, file, retention = LogRetention.ON_FAILURE)
        LoggingUtils.setupLogging(conditional)
        LogManager.getLogger("retention").info("discard-first")
        LoggingUtils.setupLogging(conditional.copy(logFile = other))
        LogManager.getLogger("retention").info("discard-other")
        LoggingUtils.setupLogging(conditional.copy(retention = LogRetention.ALWAYS))
        LogManager.getLogger("retention").info("keep-middle " + "ação 目录".repeat(3000))
        LoggingUtils.setupLogging(conditional)
        LogManager.getLogger("retention").info("discard-last")
        LoggingUtils.finishRun(true)
        val text = file.readText()
        assertTrue(text.startsWith("previous run\n"))
        assertTrue("keep-middle " + "ação 目录".repeat(3000) in text)
        assertFalse("discard" in text)
        assertFalse(other.exists)
    }

    @Test
    fun changingLevelFiltersRecordsWithoutReopeningTheFile() = fixture { root ->
        val file = root.resolve("arara.log")
        val options = LoggingOptions(LogFormat.TEXT, false, file, level = LogLevel.WARN)
        LoggingUtils.setupLogging(options)
        val logger = LogManager.getLogger("retention")
        logger.info("filtered-info")
        logger.warn("kept-warning")
        LoggingUtils.setupLogging(options.copy(level = LogLevel.ERROR))
        logger.warn("filtered-warning")
        logger.error("kept-error")
        LoggingUtils.finishRun(true)
        val text = file.readText()
        assertTrue("kept-warning" in text)
        assertTrue("kept-error" in text)
        assertFalse("filtered" in text)
    }
}
