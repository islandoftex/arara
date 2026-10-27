// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.diagnostics

import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.session.Command

/** Audited rule/executable coverage and shared, precompiled diagnostic grammars. */
internal object ToolProfiles {
    internal data class Tool(val rule: String, val executables: List<String>, val profiles: List<String>)
    private data class Pattern(val level: LogLevel, val regex: Regex)
    internal val tools: List<Tool> = resource("tools.tsv").map { columns ->
        Tool(columns[0], columns[1].split(','), columns[2].split(','))
    }
    private val patterns = resource("patterns.tsv").groupBy { it[0] }.mapValues { (_, rows) ->
        rows.map { Pattern(LogLevel.valueOf(it[1]), Regex(it[2])) }
    }

    fun executable(command: Command): String {
        val first = command.elements.firstOrNull().orEmpty()
        val name = basename(first)
        return if (name == "cmd" && command.elements.getOrNull(1).equals("/c", true))
            basename(command.elements.getOrNull(2).orEmpty()) else name
    }

    fun profiles(tool: String, rule: String?): List<String> {
        val executable = tools.firstOrNull { tool in it.executables }
        // Wrappers may emit messages from the compiler they invoke, as well as their own.
        val declared = tools.firstOrNull { it.rule == rule }
        return (executable?.profiles.orEmpty() + declared?.profiles.orEmpty() + "common").distinct()
    }

    fun severity(text: String, profiles: List<String>): LogLevel =
        match("common", text) ?: profiles.firstNotNullOfOrNull { match(it, text) } ?: LogLevel.TRACE

    private fun match(profile: String, text: String): LogLevel? =
        patterns[profile]?.firstOrNull { it.regex.containsMatchIn(text) }?.level

    private fun basename(path: String): String = path.substringAfterLast('/').substringAfterLast('\\')
        .lowercase().removeSuffix(".exe").removeSuffix(".bat").removeSuffix(".cmd")

    private fun resource(name: String): List<List<String>> = checkNotNull(javaClass.getResourceAsStream(name)) {
        "Missing diagnostic profiles: $name"
    }.bufferedReader(Charsets.UTF_8).useLines { lines ->
        lines.filter { it.isNotBlank() && !it.startsWith('#') }.map { it.split('\t') }.toList()
    }
}
