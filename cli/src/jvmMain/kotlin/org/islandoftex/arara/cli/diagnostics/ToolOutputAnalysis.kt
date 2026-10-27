// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.diagnostics

import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.EventLog
import org.islandoftex.arara.core.session.LinearExecutor

/** Sends parsed diagnostics to logging and the invocation ledger without coupling their filters. */
internal object ToolOutputAnalysis {
    fun record(command: Command, rule: String?, output: String) {
        if (output.isBlank()) {
            EventLog.trace("command.output", "The tool produced no output.", mapOf("output" to output, "empty" to true))
            return
        }
        ToolOutputAnalyzer.analyze(command, output, rule).forEach { diagnostic ->
            DisplayUtils.presentation.diagnostic(diagnostic)
            val fields = fields(command, rule, diagnostic)
            when (diagnostic.level) {
                LogLevel.ERROR -> EventLog.error("tool.diagnostic", diagnostic.message, fields)
                LogLevel.WARN -> EventLog.warn("tool.diagnostic", diagnostic.message, fields)
                LogLevel.INFO -> EventLog.info("tool.diagnostic", diagnostic.message, fields)
                LogLevel.DEBUG -> EventLog.debug("tool.diagnostic", diagnostic.message, fields)
                LogLevel.TRACE -> EventLog.trace("tool.diagnostic", diagnostic.message, fields)
            }
        }
    }

    private fun fields(command: Command, rule: String?, diagnostic: ToolDiagnostic): Map<String, Any> = buildMap {
        put("tool", diagnostic.tool)
        put("outputLine", diagnostic.outputLine)
        put("argv", command.elements)
        rule?.let { put("rule", it) }
        diagnostic.file?.let { put("sourceFile", it) }
        diagnostic.line?.let { put("sourceLine", it) }
        LinearExecutor.currentFile?.let { put("inputFile", it.sourcePath.toString()) }
        (command.workingDirectory ?: LinearExecutor.currentProject?.workingDirectory)?.let {
            put("workingDirectory", it.normalize().toString())
        }
    }
}
