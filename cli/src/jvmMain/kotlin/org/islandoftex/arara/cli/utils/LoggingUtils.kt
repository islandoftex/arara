// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configurator
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LoggingOptions
import java.util.UUID

/** Configures a single file appender and releases it when logging is disabled. */
object LoggingUtils {
    private data class Destination(val path: String, val append: Boolean, val format: LogFormat)
    private var active: Destination? = null

    internal val isJson: Boolean get() = active?.format == LogFormat.JSON

    internal fun beginRun(): String? = ThreadContext.get("araraRunId").also {
        ThreadContext.put("araraRunId", UUID.randomUUID().toString())
    }

    internal fun restoreRun(previous: String?) {
        if (previous == null) ThreadContext.remove("araraRunId") else ThreadContext.put("araraRunId", previous)
    }

    /** Apply log settings once per destination, without truncating on repeated configuration. */
    fun setupLogging(loggingOptions: LoggingOptions) {
        val context = LogManager.getContext(false) as LoggerContext
        if (loggingOptions.format == LogFormat.NONE) {
            Configurator.setRootLevel(Level.OFF)
            if (active != null) context.configuration.appenders["File"]?.stop()
            active = null
            ThreadContext.remove("araraLogFile")
            ThreadContext.remove("araraAppendLog")
        } else {
            val destination = Destination(
                loggingOptions.logFile.normalize().toString(), loggingOptions.appendLog, loggingOptions.format
            )
            if (active != destination) {
                context.configuration.appenders["File"]?.stop()
                ThreadContext.put("araraLogFile", destination.path)
                ThreadContext.put("araraAppendLog", destination.append.toString())
                val resource = if (destination.format == LogFormat.JSON) "log4j2-json.xml" else "log4j2.xml"
                context.configLocation = LoggingUtils::class.java
                    .getResource("/org/islandoftex/arara/cli/configuration/$resource").toURI()
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
