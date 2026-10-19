// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.lua.rules.LuaRuleParser
import org.islandoftex.arara.mvel.interpreter.MvelCommandEvaluator

class OfficialLuaRulesTest {
    private val rules = MPPPath("../rules").normalize()

    @Test
    fun everyLuaRuleParsesAndMatchesItsYamlArgumentContract() {
        val files = Files.list(rules.toJVMPath()).use { paths ->
            paths.filter { it.fileName.toString().endsWith(".lua") }.sorted().iterator().asSequence().toList()
        }
        assertTrue(files.isNotEmpty())
        files.forEach { path ->
            val identifier = path.fileName.toString().removePrefix("arara-rule-").removeSuffix(".lua")
            val lua = LuaRuleParser.parse(path.toMPPPath(), identifier)
            val yaml = org.islandoftex.arara.mvel.rules.Rule.parse(
                rules.resolve("arara-rule-$identifier.yaml"), identifier)
            assertEquals(yaml.displayName, lua.displayName, identifier)
            assertEquals(yaml.commands.map { it.name }, lua.commands.map { it.name }, identifier)
            assertEquals(yaml.arguments.map { it.identifier to it.isRequired },
                lua.arguments.map { it.identifier to it.isRequired }, identifier)
        }
    }

    /** Captures command values within a real executor context without launching them. */
    private fun inProject(block: (MPPPath) -> Unit) {
        val directory = Files.createTempDirectory("arara Lua parity ").toMPPPath()
        val hooks = LinearExecutor.hooks
        val options = LinearExecutor.executionOptions
        val file = directory.resolve("input with spaces.tex")
        try {
            val input = object : ProjectFile(file, FileType("tex", "^%")) {
                override fun fetchDirectives(parseOnlyHeader: Boolean) = emptyList<org.islandoftex.arara.api.rules.Directive>()
            }
            LinearExecutor.executionOptions = ExecutionOptions()
            LinearExecutor.hooks = ExecutorHooks(processDirectives = { _, _ -> block(file); emptyList() })
            assertEquals(0, LinearExecutor.execute(listOf(Project("parity", directory, setOf(input)))).exitCode)
        } finally {
            LinearExecutor.hooks = hooks
            LinearExecutor.executionOptions = options
            directory.removeDirectory()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parameters(rule: Rule, reference: MPPPath, values: Map<String, Any>): Map<String, Any> =
        rule.arguments.associate { argument ->
            val input = values[argument.identifier] ?: argument.defaultValue
            argument.identifier to if (input == null) emptyList<String>() else {
                (argument as RuleArgument<Any?>).processor(input,
                    mapOf("parameters" to values, "reference" to reference))
            }
        } + ("reference" to reference)

    private fun commandValues(value: Any?): List<Any?> = when (value) {
        is List<*> -> value.flatMap { commandValues(it) }
        is Command -> listOf(value.elements to value.workingDirectory)
        else -> listOf(value)
    }

    private fun parity(identifier: String, values: Map<String, Any> = emptyMap()) = inProject { reference ->
        val yaml = org.islandoftex.arara.mvel.rules.Rule.parse(
            rules.resolve("arara-rule-$identifier.yaml"), identifier)
        val lua = LuaRuleParser.parse(rules.resolve("arara-rule-$identifier.lua"), identifier)
        val expected = yaml.commands.flatMap {
            commandValues(MvelCommandEvaluator(it as org.islandoftex.arara.mvel.rules.SerialRuleCommand).evaluate(parameters(yaml, reference, values)))
        }
        val actual = lua.commands.flatMap { commandValues(it.evaluate(parameters(lua, reference, values))) }
        assertEquals(expected, actual, "$identifier $values")
    }

    @Test
    fun texCommandsMatchYamlForDefaultsAndFlags() {
        listOf("context", "csplain", "etex", "latex", "luahbtex", "lualatex", "luatex", "pdfcsplain",
            "pdflatex", "pdftex", "platex", "ptex", "tex", "uplatex", "uptex", "xelatex", "xetex")
            .forEach { parity(it) }
        listOf("latex", "lualatex", "pdflatex", "xelatex").forEach {
            parity(it, mapOf("interaction" to "nonstopmode", "shell" to true, "synctex" to false,
                "options" to listOf("--jobname=with spaces")))
        }
        parity("pdflatex", mapOf("branch" to "developer", "draft" to true))
    }
}
