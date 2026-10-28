// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.session.LinearExecutor

/** Diagnostic events for tool output, completion and process failures. */
interface CommandExecutionLog {
    /** Record an explicitly supplied raw output buffer at trace level. */
    fun commandOutput(output: String)

    /** Record the command exit code and duration in milliseconds. */
    fun commandCompleted(command: Command, exitCode: Int, durationMs: Long)

    /** Record a backend failure category, duration in milliseconds and original cause. */
    fun commandFailed(command: Command, kind: String, durationMs: Long, cause: Throwable)
}

private object DefaultCommandExecutionLog : CommandExecutionLog {
    /** Record an explicitly supplied raw output buffer at trace level. */
    override fun commandOutput(output: String) {
        if (!EventLog.isTraceEnabled) return
        val message = if (output.isBlank()) "The tool produced no output."
        else buildString {
            appendLine(RunLog.outputSeparator("BEGIN OUTPUT BUFFER"))
            append(output)
            if (!output.endsWith('\n')) appendLine()
            append(RunLog.outputSeparator("END OUTPUT BUFFER"))
        }
        EventLog.trace("command.output", message, mapOf("output" to output, "empty" to output.isBlank()))
    }

    /** Record the command exit code and duration in milliseconds. */
    override fun commandCompleted(command: Command, exitCode: Int, durationMs: Long) {
        if (!EventLog.isInfoEnabled) return
        EventLog.info(
            "command.completed", "System command exited with value $exitCode after $durationMs ms",
            commandFields(command) + mapOf("exitCode" to exitCode, "durationMs" to durationMs)
        )
    }

    /** Record a backend failure category, duration in milliseconds and original cause. */
    override fun commandFailed(command: Command, kind: String, durationMs: Long, cause: Throwable) {
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

}

internal val commandExecutionLog: CommandExecutionLog get() = DefaultCommandExecutionLog
