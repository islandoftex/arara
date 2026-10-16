// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath

internal data class RuleQuery(
    val directory: MPPPath?, val engine: RuleEngine?, val info: String?, val filter: String?, val all: Boolean
)

internal class RulesCommand(private val query: (RuleQuery) -> RuleDiscoveryResult) : CliktCommand(name = "rules") {
    private val list by option("--list", help = "List rules (the default)").flag()
    private val all by option("--all", help = "Include shadowed rules and alternative formats").flag()
    private val filter by option("--filter", help = "Filter identifiers by a case-insensitive substring")
    private val info by option("--info", help = "Inspect a rule; loads its script without evaluating commands")
    private val json by option("--json", help = "Write JSON rules and diagnostics").flag()
    private val directory by option("-d", "--working-directory",
        help = "Directory used for configuration and rule paths")
        .path(mustExist = true, canBeFile = false, mustBeReadable = true)
        .convert { it.toMPPPath() }
    private val engine by option("-e", "--preferred-engine", help = "Preferred rule format")
        .choice("lua" to RuleEngine.LUA, "yaml" to RuleEngine.YAML)

    override fun run() {
        if (info != null) {
            if (all || filter != null || list) {
                throw UsageError("--info cannot be combined with --list, --all or --filter")
            }
        }
        val result = try {
            query(RuleQuery(directory, engine, info, filter, all))
        } catch (exception: AraraException) {
            RuleDiscoveryResult(emptyList(), listOf(RuleDiagnostic(null, exception.message.orEmpty())))
        }
        if (json) echo(result.json()) else printResult(result)
        if (result.diagnostics.isNotEmpty()) throw ProgramResult(2)
    }

    private fun printResult(result: RuleDiscoveryResult) {
        result.rules.forEach { location ->
            echo("${location.identifier}\t${location.format}\t${location.path}" +
                if (location.effective) "" else "\t(shadowed)")
        }
        result.metadata?.let { printMetadata(it, result.rules.single().format) }
        result.diagnostics.forEach { echo(listOfNotNull(it.path, it.message).joinToString(": "), err = true) }
    }

    private fun printMetadata(rule: org.islandoftex.arara.api.rules.Rule, format: String) {
        rule.displayName?.let { echo("Name: $it") }
        echo("Authors: ${rule.authors.joinToString(", ")}")
        rule.commands.forEachIndexed { index, command ->
            echo("Command ${index + 1}: ${command.name ?: "[unnamed]"}")
        }
        rule.arguments.forEach { argument ->
            val value = argument.defaultValue
            val default = if (format == "yaml" && value is String && "@{" in value)
                "dynamic" else value?.toString() ?: "none"
            echo("Argument: ${argument.identifier}; required=${argument.isRequired}; default=$default")
        }
    }
}
