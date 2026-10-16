// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configurator
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.LoggingOptions as CoreLoggingOptions

/**
 * Verifies the append option end to end: the flag travels to the
 * configuration as a context value, and a fresh process either
 * appends to an existing log file or starts it from scratch, exactly
 * as asked. Each method writes its own file, so the shared stream
 * of one JVM cannot leak state between them. This class
 * deliberately mounts a real logging backend; it restores the
 * silent root level afterwards, so the suite's backendless design
 * survives it.
 */
class LoggingAppendTest {
    private val logger = KotlinLogging.logger { }
    private var logFile: java.nio.file.Path = Files.createTempFile(
        "arara-append-", ".log"
    )

    @AfterTest
    fun restoreBackend() {
        ThreadContext.remove("araraLogFile")
        ThreadContext.remove("araraAppend")
        // the file appender holds the log open until it stops:
        // Windows refuses to delete a file a live handle claims,
        // so the appenders release it before the delete - Linux
        // tolerates the opposite order, which is why this
        // teardown was born green there. The context itself keeps
        // running: stopping it would strand every shared logger
        // created before the stop, and the classes that assert
        // logged lines later in the suite depend on them
        (LogManager.getContext(false) as LoggerContext)
            .configuration.appenders.values.forEach { it.stop() }
        Files.deleteIfExists(logFile)
        Configurator.setRootLevel(Level.OFF)
    }

    private fun options(append: Boolean): LoggingOptions =
        CoreLoggingOptions(
            enableLogging = true,
            appendLog = append,
            logFile = org.islandoftex.arara.api.files.MPPPath(
                logFile.toString()
            )
        )

    @Test
    fun `the append flag travels to the configuration context`() {
        Logging.setupLogging(options(append = true))
        assertEquals(
            "true",
            ThreadContext.get("araraAppend"),
            "the context carries the append flag"
        )
        Logging.setupLogging(options(append = false))
        assertEquals(
            "false",
            ThreadContext.get("araraAppend"),
            "the context carries the truncation flag"
        )
    }

    @Test
    fun `an appended log keeps the content of an earlier run`() {
        // an earlier process wrote this line; a fresh process with
        // appending requested has to keep it - the appender opens in
        // append mode
        Files.writeString(logFile, "first run\n")
        Logging.setupLogging(options(append = true))
        logger.info { "second run" }
        val content = Files.readString(logFile)
        assertTrue("first run" in content, "the log kept the earlier run")
        assertTrue("second run" in content, "the log holds the later run")
    }

    @Test
    fun `a fresh log discards an earlier run by default`() {
        // the same earlier process wrote this line; a fresh process
        // with the default truncation has to discard it - the
        // appender opens the file from scratch
        Files.writeString(logFile, "first run\n")
        Logging.setupLogging(options(append = false))
        logger.info { "second run" }
        val content = Files.readString(logFile)
        assertTrue("first run" !in content, "the fresh log discards history")
        assertTrue("second run" in content, "the fresh log holds the run")
    }
}
