// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.diagnostics

import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.session.Command

/** Parses process output without reading possibly stale transcripts or changing execution status. */
internal object ToolOutputAnalyzer {
    private val escapes = Regex("\u001B(?:\\[[0-?]*[ -/]*[@-~]|\\][^\u0007\u001B]*(?:\u0007|\u001B\\\\))")
    private val location = Regex("^(.+?):\\s*(\\d+)(?:[.:]\\d+)?:\\s*(.*)$")
    private val gnuLocation = Regex("^\"(.+)\" line (\\d+):\\s*(.*)$")
    private val sketchLocation = Regex("^(.+)\\((\\d+)\\)\\s*:\\s*(.*)$")
    private val indexLocation = Regex(
        "^!! (?:Input index|Input style|Output) error \\(file = (.+), line = (\\d+)\\):\\s*(.*)$")
    private val texLine = Regex("^l\\.(\\d+)\\s")
    private val inputLine = Regex("\\bon input line (\\d+)")
    private val continuation = Regex(
        "^(?:\\([\\w@.-]+\\)\\s|\\s+|l\\.\\d+\\s|" +
            "(?:Error|Warning) in |Caused by:|[A-Za-z_.]*(?:Error|Exception):)")

    fun analyze(command: Command, output: String, rule: String? = null): List<ToolDiagnostic> {
        val tool = ToolProfiles.executable(command)
        val profiles = ToolProfiles.profiles(tool, rule)
        val result = mutableListOf<ToolDiagnostic>()
        output.lineSequence().forEachIndexed { index, raw ->
            val text = escapes.replace(raw, "").trimEnd()
            if (text.isNotEmpty()) append(result, tool, profiles, text, index + 1)
        }
        return result
    }

    private fun append(
        result: MutableList<ToolDiagnostic>, tool: String, profiles: List<String>, text: String, outputLine: Int
    ) {
        val at = location.matchEntire(text) ?: gnuLocation.matchEntire(text)
            ?: indexLocation.matchEntire(text) ?: sketchLocation.matchEntire(text)
        var level = ToolProfiles.severity(text.trimStart(), profiles)
        if (at != null && level == LogLevel.TRACE) {
            level = ToolProfiles.severity(at.groupValues[LOCATION_MESSAGE].trimStart(), profiles)
        }
        if (at != null && level == LogLevel.TRACE && profiles.any { it in SOURCE_LOCATIONS }) level = LogLevel.ERROR
        val previous = result.lastOrNull()
        if (level == LogLevel.TRACE && isContinuation(previous, text, outputLine)) {
            checkNotNull(previous)
            result[result.lastIndex] = previous.copy(message = previous.message + "\n" + text,
                line = previous.line ?: reportedLine(text))
        } else {
            result.add(ToolDiagnostic(tool, level, text, outputLine,
                at?.groupValues?.get(1), at?.groupValues?.get(2)?.toIntOrNull() ?: reportedLine(text)))
        }
    }

    private fun isContinuation(previous: ToolDiagnostic?, text: String, outputLine: Int): Boolean =
        previous != null && previous.level >= LogLevel.WARN && continuation.containsMatchIn(text) &&
            previous.outputLine + previous.message.count { it == '\n' } + 1 == outputLine

    private fun reportedLine(text: String): Int? =
        (texLine.find(text) ?: inputLine.find(text))?.groupValues?.get(1)?.toIntOrNull()

    private const val LOCATION_MESSAGE = 3
    private val SOURCE_LOCATIONS = setOf("tex", "asymptote", "scripts", "sketch", "gnuplot")
}
