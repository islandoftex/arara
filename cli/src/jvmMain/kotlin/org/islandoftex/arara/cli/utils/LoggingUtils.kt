// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.IOException
import java.util.UUID
import org.apache.logging.log4j.ThreadContext
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LogRetention
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.core.localization.LanguageController

/** Configures a single file appender and releases it when logging is disabled. */

object LoggingUtils {
    private val state = LogRunState()
    private val appender = LogFileAppender(state)

    internal fun beginWatch() {
        finishRun(true)
        state.watching = true
        state.watchDestinations.clear()
    }

    internal fun beginWatchCycle() {
        state.tracker = LogRetentionTracker()
    }

    internal fun endWatch() {
        state.watching = false
        state.watchDestinations.clear()
    }

    internal val isJson: Boolean get() = appender.isJson

    internal fun beginRun(): String? = ThreadContext.get("araraRunId").also {
        state.tracker = LogRetentionTracker()
        ThreadContext.put("araraRunId", UUID.randomUUID().toString())
    }

    internal fun restoreRun(previous: String?) {
        if (state.tracker != null) finishRun(false)
        if (previous == null) ThreadContext.remove("araraRunId") else ThreadContext.put("araraRunId", previous)
    }

    /** Apply log settings once per destination, without truncating on repeated configuration. */
    fun setupLogging(loggingOptions: LoggingOptions) = appender.configureLogging(loggingOptions, false)

    internal fun resumeLogging(loggingOptions: LoggingOptions) {
        if (loggingOptions.retention == LogRetention.ON_FAILURE && !loggingOptions.logFile.exists) return
        appender.configureLogging(loggingOptions, true)
    }

    /** Close run-owned log files and discard successful output according to each destination's policy. */
    internal fun finishRun(successful: Boolean) {
        try {
            init()
            if (successful) state.tracker?.discardSuccessfulOutput()
        } catch (exception: IOException) {
            reportCleanupFailure(exception)
        } catch (exception: AraraException) {
            reportCleanupFailure(exception)
        } catch (exception: IllegalStateException) {
            reportCleanupFailure(exception)
        } finally {
            state.tracker = null
            appender.resetSegment()
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

/** Retention state is independent of the appender’s platform-specific configuration. */
internal class LogRunState {
    var tracker: LogRetentionTracker? = null
    var watching = false
    val watchDestinations = mutableSetOf<String>()
}
