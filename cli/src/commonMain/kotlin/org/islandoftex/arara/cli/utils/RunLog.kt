// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.session.Session
import kotlin.time.Duration.Companion.seconds

/** Application log labels are English; script, tool and exception text remains literal. */
object RunLog {
    private val logger = KotlinLogging.logger { }

    internal val applicationPath: String
        get() = try {
            ConfigurationUtils.applicationPath.toString()
        } catch (_: AraraException) {
            "[unknown application path]"
        }

    internal fun separator(): String = "-".repeat(Session.userInterfaceOptions.terminalOutputWidth)

    internal fun outputSeparator(message: String): String =
        " $message ".center(Session.userInterfaceOptions.terminalOutputWidth, '-')

    fun welcome(version: String) = logger.info { "Welcome to arara $version!\n${separator()}" }

    fun fileInformation(file: ProjectFile) = logger.info {
        "Processing \"${file.path}\" (size: ${file.path.fileSize} bytes, last modified: ${file.path.lastModified})"
    }

    fun entry(name: String, task: String) = logger.info { "Interpreting task \"$task\" from rule \"$name\"" }

    fun taskResult(success: Boolean) = logger.info { "Task result: " + if (success) "SUCCESS" else "FAILURE" }

    fun exception(message: String?, details: String?) {
        logger.error { message ?: "EXCEPTION PROVIDES NO MESSAGE" }
        if (details != null) logger.error { details }
    }

    fun executionTime(seconds: Double) = logger.info {
        val duration = seconds.seconds
        val milliseconds = duration.inWholeMilliseconds % MILLISECONDS_PER_SECOND
        "Elapsed time: ${duration.inWholeSeconds}.${milliseconds.toString().padStart(MILLISECOND_DIGITS, '0')} seconds"
    }

    private const val MILLISECONDS_PER_SECOND = 1000
    private const val MILLISECOND_DIGITS = 3
}
