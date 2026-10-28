// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.logging.RunLifecycleLog

private object DefaultRunLifecycleLog : RunLifecycleLog {
    private const val MILLISECONDS_PER_SECOND = 1000
    private const val MILLISECOND_DIGITS = 3

    /** Record the application version greeting. */
    override fun welcome(version: String) = EventLog.info(
        "application.version", "Welcome to arara $version!\n${RunLog.separator()}", mapOf("version" to version)
    )

    /** Record input metadata when informational logging is enabled. */
    override fun fileInformation(file: ProjectFile) {
        if (!EventLog.isInfoEnabled) return
        val size = file.sourcePath.fileSize
        EventLog.info(
            "file.started",
            "Processing \"${file.path}\" (size: $size bytes, last modified: ${file.sourcePath.lastModified})",
            mapOf("path" to file.path.toString(), "sourcePath" to file.sourcePath.toString(), "sizeBytes" to size)
        )
    }

    /** Record an application error with optional details and the original cause. */
    override fun exception(message: String?, details: String?, cause: Throwable?) {
        EventLog.error("run.exception", message ?: "EXCEPTION PROVIDES NO MESSAGE", emptyMap(), cause)
        if (details != null) EventLog.error("run.exception.details", details, emptyMap())
    }

    /** Record elapsed seconds as text and milliseconds as a structured field. */
    override fun executionTime(seconds: Double) {
        if (!EventLog.isInfoEnabled) return
        val duration = seconds.seconds
        val milliseconds = duration.inWholeMilliseconds % MILLISECONDS_PER_SECOND
        val time = "${duration.inWholeSeconds}.${milliseconds.toString().padStart(MILLISECOND_DIGITS, '0')}"
        EventLog.info("run.elapsed", "Elapsed time: $time seconds", mapOf("durationMs" to duration.inWholeMilliseconds))
    }

    /** Record the project name and working directory. */
    override fun project(name: String, directory: String) = EventLog.info(
        "project.started", "Project \"$name\" (working directory: $directory)",
        mapOf("name" to name, "workingDirectory" to directory)
    )

    /** Record invocation startup with the application version. */
    override fun executionStarted(version: String) =
        EventLog.info("run.started", "Run started", mapOf("version" to version))

    /** Record the final exit code; unsuccessful runs use the error level. */
    override fun executionOutcome(exitCode: Int) {
        val fields = mapOf("exitCode" to exitCode, "success" to (exitCode == 0))
        if (exitCode == 0) EventLog.info("run.completed", "Run completed with exit code $exitCode", fields)
        else EventLog.error("run.failed", "Run failed with exit code $exitCode", fields)
    }

}

internal val runLifecycleLog: RunLifecycleLog get() = DefaultRunLifecycleLog
