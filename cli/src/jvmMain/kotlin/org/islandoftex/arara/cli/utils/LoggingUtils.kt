// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.IOException
import java.util.UUID
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configurator
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LogRetention
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.core.localization.LanguageController

/** Configures a single file appender and releases it when logging is disabled. */
object LoggingUtils {
    private data class Destination(
        val path: String, val append: Boolean, val format: LogFormat, val retention: LogRetention
    )
    private var active: Destination? = null
    private var tracker: LogRetentionTracker? = null
    private var segment: LogRetentionTracker.Segment? = null

    internal val isJson: Boolean get() = active?.format == LogFormat.JSON

    internal fun beginRun(): String? = ThreadContext.get("araraRunId").also {
        tracker = LogRetentionTracker()
        ThreadContext.put("araraRunId", UUID.randomUUID().toString())
    }

    internal fun restoreRun(previous: String?) {
        if (tracker != null) finishRun(false)
        if (previous == null) ThreadContext.remove("araraRunId") else ThreadContext.put("araraRunId", previous)
    }

    /** Apply log settings once per destination, without truncating on repeated configuration. */
    fun setupLogging(loggingOptions: LoggingOptions) = configureLogging(loggingOptions, false)

    internal fun resumeLogging(loggingOptions: LoggingOptions) {
        if (loggingOptions.retention == LogRetention.ON_FAILURE && !loggingOptions.logFile.exists) return
        configureLogging(loggingOptions, true)
    }

    private fun configureLogging(loggingOptions: LoggingOptions, preserveExisting: Boolean) {
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
                val append = preserveExisting || destination.append || tracker?.hasOpened(path) == true
                segment = tracker?.open(path, append, destination.retention)
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
        segment?.let { tracker?.close(it) }
        segment = null
    }

    /** Close run-owned log files and discard successful output according to each destination's policy. */
    internal fun finishRun(successful: Boolean) {
        try {
            init()
            if (successful) tracker?.discardSuccessfulOutput()
        } catch (exception: IOException) {
            reportCleanupFailure(exception)
        } catch (exception: AraraException) {
            reportCleanupFailure(exception)
        } catch (exception: IllegalStateException) {
            reportCleanupFailure(exception)
        } finally {
            tracker = null
            segment = null
        }
    }

    private fun reportCleanupFailure(exception: Exception) {
        System.err.println(LanguageController.messages.errorLogCleanupFailed(exception.toString()))
    }

    /** Disable logging, close its file handle and remove its thread-local destination. */
    fun init() {
        setupLogging(org.islandoftex.arara.core.configuration.LoggingOptions())
    }
}
