// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configurator
import org.islandoftex.arara.api.configuration.LoggingOptions

/** Configures a single file appender and releases it when logging is disabled. */
object LoggingUtils {
    private data class Destination(val path: String, val append: Boolean)
    private var active: Destination? = null

    /** Apply log settings once per destination, without truncating on repeated configuration. */
    fun setupLogging(loggingOptions: LoggingOptions) {
        val context = LogManager.getContext(false) as LoggerContext
        if (!loggingOptions.enableLogging) {
            Configurator.setRootLevel(Level.OFF)
            if (active != null) context.configuration.appenders["File"]?.stop()
            active = null
            ThreadContext.remove("araraLogFile")
            ThreadContext.remove("araraAppendLog")
        } else {
            val destination = Destination(loggingOptions.logFile.normalize().toString(), loggingOptions.appendLog)
            if (active != destination) {
                ThreadContext.put("araraLogFile", destination.path)
                ThreadContext.put("araraAppendLog", destination.append.toString())
                context.configLocation = LoggingUtils::class.java
                    .getResource("/org/islandoftex/arara/cli/configuration/log4j2.xml").toURI()
                active = destination
            }
            Configurator.setRootLevel(Level.ALL)
        }
    }

    /** Disable logging, close its file handle and remove its thread-local destination. */
    fun init() {
        setupLogging(org.islandoftex.arara.core.configuration.LoggingOptions())
    }
}
