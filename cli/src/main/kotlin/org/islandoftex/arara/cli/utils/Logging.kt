// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.config.Configurator
import org.islandoftex.arara.api.configuration.LoggingOptions

/**
 * Implements the logging controller, which mounts the file-backed
 * configuration when logging is enabled and silences the backend
 * otherwise. A run either appends to the log file or starts it
 * fresh, exactly as the options ask: the append flag travels to the
 * configuration as a context value, and the file appender reads
 * it, so a persisted log survives across runs when requested.
 *
 * @author Island of TeX
 */
object Logging {
    /**
     * Sets the logging configuration according to the provided options.
     * If logging is enabled, the log entries go to the configured
     * file, appended to or truncating it per the append option;
     * otherwise the logging feature keeps silent.
     *
     * @param loggingOptions The configuration of the logging behavior.
     */
    fun setupLogging(loggingOptions: LoggingOptions) {
        if (loggingOptions.enableLogging) {
            // note: multi-threading has not been checked here
            ThreadContext.put(
                "araraLogFile",
                loggingOptions.logFile.normalize().toString()
            )
            ThreadContext.put(
                "araraAppend",
                loggingOptions.appendLog.toString()
            )
            val loggerContext = LogManager.getContext(false)
                as org.apache.logging.log4j.core.LoggerContext
            loggerContext.configLocation = Logging::class.java
                .getResource("/org/islandoftex/arara/cli/configuration/log4j2.xml")
                .toURI()
            loggerContext.reconfigure()
        } else {
            ThreadContext.remove("araraLogFile")
            ThreadContext.remove("araraAppend")
            Configurator.setRootLevel(Level.OFF)
        }
    }

    /**
     * Initializes the logging controller by disabling it. I don't want an odd
     * behaviour out of the box.
     */
    fun init() {
        setupLogging(org.islandoftex.arara.core.configuration.LoggingOptions())
    }
}
