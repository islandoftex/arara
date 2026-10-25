// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/** Application log labels are English; script, tool and exception text remains literal. */
@Suppress("TooManyFunctions")
object RunLog {
    internal val applicationPath: String
        get() = try {
            ConfigurationUtils.applicationPath.toString()
        } catch (_: AraraException) {
            "[unknown application path]"
        }

    internal fun separator(): String = "-".repeat(Session.userInterfaceOptions.terminalOutputWidth)

    internal fun outputSeparator(message: String): String =
        " $message ".center(Session.userInterfaceOptions.terminalOutputWidth, '-')

    /** Record the application version greeting. */
    fun welcome(version: String) = EventLog.info(
        "application.version", "Welcome to arara $version!\n${separator()}", mapOf("version" to version)
    )

    /** Record input metadata when informational logging is enabled. */
    fun fileInformation(file: ProjectFile) {
        if (!EventLog.isInfoEnabled) return
        val size = file.path.fileSize
        EventLog.info(
            "file.started",
            "Processing \"${file.path}\" (size: $size bytes, last modified: ${file.path.lastModified})",
            mapOf("path" to file.path.toString(), "sizeBytes" to size)
        )
    }

    /** Record a task starting within a rule. */
    fun entry(name: String, task: String) = EventLog.info(
        "task.started", "Interpreting task \"$task\" from rule \"$name\"", mapOf("rule" to name, "task" to task)
    )

    /** Record the task success flag. */
    fun taskResult(success: Boolean) = EventLog.info(
        "task.result", "Task result: " + if (success) "SUCCESS" else "FAILURE", mapOf("success" to success)
    )

    /** Record an application error with optional details and the original cause. */
    fun exception(message: String?, details: String?, cause: Throwable? = null) {
        EventLog.error("run.exception", message ?: "EXCEPTION PROVIDES NO MESSAGE", emptyMap(), cause)
        if (details != null) EventLog.error("run.exception.details", details, emptyMap())
    }

    /** Record elapsed seconds as text and milliseconds as a structured field. */
    fun executionTime(seconds: Double) {
        if (!EventLog.isInfoEnabled) return
        val duration = seconds.seconds
        val milliseconds = duration.inWholeMilliseconds % MILLISECONDS_PER_SECOND
        val time = "${duration.inWholeSeconds}.${milliseconds.toString().padStart(MILLISECOND_DIGITS, '0')}"
        EventLog.info("run.elapsed", "Elapsed time: $time seconds", mapOf("durationMs" to duration.inWholeMilliseconds))
    }

    /** Record the project name and working directory. */
    fun project(name: String, directory: String) = EventLog.info(
        "project.started", "Project \"$name\" (working directory: $directory)",
        mapOf("name" to name, "workingDirectory" to directory)
    )

    /** Record invocation startup with the application version. */
    fun executionStarted(version: String) = EventLog.info("run.started", "Run started", mapOf("version" to version))

    /** Record the final exit code; unsuccessful runs use the error level. */
    fun executionOutcome(exitCode: Int) {
        val fields = mapOf("exitCode" to exitCode, "success" to (exitCode == 0))
        if (exitCode == 0) EventLog.info("run.completed", "Run completed with exit code $exitCode", fields)
        else EventLog.error("run.failed", "Run failed with exit code $exitCode", fields)
    }

    /** Record the resolved rule path and engine. */
    fun rule(identifier: String, path: String, engine: String) = EventLog.info(
        "rule.resolved", "Rule file: \"$path\" (engine: $engine)",
        mapOf("rule" to identifier, "path" to path, "engine" to engine)
    )

    /** Record each resolved legacy rule, including skipped directives and dry runs. */
    fun deprecatedYaml(identifier: String, path: String) = EventLog.warn(
        "rule.deprecated", "Deprecated YAML rule: $identifier ($path); YAML rules will be discontinued in v9.",
        mapOf("rule" to identifier, "path" to path, "engine" to "yaml", "removalVersion" to "9")
    )

    /** Record captured tool output, including an explicit empty-output marker. */
    fun commandOutput(output: String) {
        if (!EventLog.isInfoEnabled) return
        val message = if (output.isBlank()) "The tool produced no output."
            else """
                ${outputSeparator("BEGIN OUTPUT BUFFER")}
                $output
                ${outputSeparator("END OUTPUT BUFFER")}
            """.trimIndent()
        EventLog.info("command.output", message, mapOf("output" to output, "empty" to output.isBlank()))
    }

    /** Record the command exit code and duration in milliseconds. */
    fun commandCompleted(command: Command, exitCode: Int, durationMs: Long) {
        if (!EventLog.isInfoEnabled) return
        EventLog.info(
            "command.completed", "System command exited with value $exitCode after $durationMs ms",
            commandFields(command) + mapOf("exitCode" to exitCode, "durationMs" to durationMs)
        )
    }

    /** Record a backend failure category, duration in milliseconds and original cause. */
    fun commandFailed(command: Command, kind: String, durationMs: Long, cause: Throwable) {
        if (!EventLog.isErrorEnabled) return
        EventLog.error(
            "command.failed", "System command failed ($kind) after $durationMs ms",
            commandFields(command) + mapOf("failureKind" to kind, "durationMs" to durationMs), cause
        )
    }

    private fun commandFields(command: Command): Map<String, Any> = buildMap {
        put("argv", command.elements.toList())
        (command.workingDirectory ?: LinearExecutor.currentProject?.workingDirectory)
            ?.let { put("workingDirectory", it.normalize().toString()) }
        LinearExecutor.currentFile?.let { put("inputFile", it.path.toString()) }
    }

    private const val MILLISECONDS_PER_SECOND = 1000
    private const val MILLISECOND_DIGITS = 3
}
