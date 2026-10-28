// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configurator
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LogRetention
import org.islandoftex.arara.api.configuration.LoggingOptions

/** Owns only appender configuration and the currently open file segment. */
internal class LogFileAppender(private val state: LogRunState) {
    private data class Destination(
        val path: String, val append: Boolean, val format: LogFormat, val retention: LogRetention
    )
    private var active: Destination? = null
    private var segment: LogRetentionTracker.Segment? = null

    val isJson: Boolean get() = active?.format == LogFormat.JSON
    fun resetSegment() { segment = null }

    fun configureLogging(loggingOptions: LoggingOptions, preserveExisting: Boolean) {
        val context = LogManager.getContext(false) as LoggerContext
        if (loggingOptions.format == LogFormat.NONE) {
            Configurator.setRootLevel(Level.OFF)
            closeAppender(context)
            active = null
            ThreadContext.remove("araraLogFile")
            ThreadContext.remove("araraAppendLog")
        } else {
            val destination = Destination(
                loggingOptions.logFile.normalize().toString(), loggingOptions.appendLog, loggingOptions.format,
                loggingOptions.retention
            )
            if (active != destination) {
                closeAppender(context)
                val path = loggingOptions.logFile.normalize()
                val append = preserveExisting || destination.append || state.tracker?.hasOpened(path) == true ||
                    (state.watching && destination.path in state.watchDestinations)
                segment = state.tracker?.open(path, append, destination.retention)
                if (state.watching) state.watchDestinations += destination.path
                ThreadContext.put("araraLogFile", destination.path)
                ThreadContext.put("araraAppendLog", append.toString())
                val resource = if (destination.format == LogFormat.JSON) "log4j2-json.xml" else "log4j2.xml"
                context.configLocation = LoggingUtils::class.java
                    .getResource("/org/islandoftex/arara/cli/configuration/$resource").toURI()
                active = destination
            }
            Configurator.setRootLevel(Level.valueOf(loggingOptions.level.name))
        }
    }

    private fun closeAppender(context: LoggerContext) {
        if (active != null) context.configuration.appenders["File"]?.stop()
        segment?.let { state.tracker?.close(it) }
        segment = null
    }

}
