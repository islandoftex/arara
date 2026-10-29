// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.api.session.ExecutorHooks
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.lua.rules.LuaRule
import org.islandoftex.arara.lua.rules.LuaRuleParser
import org.junit.jupiter.api.io.TempDir

class LuaRuleDevelopmentTest {
    @TempDir
    lateinit var directory: Path

    private fun rule(index: Int): LuaRule {
        val guide = checkNotNull(javaClass.getResource("/tutorials/RuleDevelopment.md")).readText()
        val examples = Regex("```lua\\n(.*?)\\n```", RegexOption.DOT_MATCHES_ALL).findAll(guide)
            .map { it.groupValues[1] }
            .filter { it.startsWith("return {") && "identifier = \"petertex\"" in it }.toList()
        assertEquals(2, examples.size, "The tutorial has an initial rule and its complete replacement")
        val path = directory.toMPPPath().resolve("petertex.lua").apply { writeText(examples[index]) }
        return LuaRuleParser.parse(path, "petertex")
    }

    private fun command(
        rule: LuaRule, supplied: Map<String, Any> = emptyMap(), filename: String = "article.tex"
    ): Command {
        val directive = object : Directive {
            override val identifier = "petertex"
            override val parameters = supplied + ("reference" to MPPPath(filename))
            override val conditional = DirectiveConditional()
            override val lineNumbers = listOf(1)
            override fun execute(): Int = error("Only command construction is tested")
        }
        val previousOptions = LinearExecutor.executionOptions
        val previousHooks = LinearExecutor.hooks
        val root = directory.toMPPPath()
        val input = object : ProjectFile(root.resolve(filename), FileType("tex", "^%")) {
            override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> = emptyList()
        }
        var result: Command? = null
        try {
            LinearExecutor.executionOptions = ExecutionOptions()
            LinearExecutor.hooks = ExecutorHooks(processDirectives = { _, _ ->
                val parameters = RuleArguments.parseArguments(rule, directive)
                result = assertIs<Command>(rule.commands.single().evaluate(parameters).single())
                emptyList()
            })
            assertEquals(0, LinearExecutor.execute(listOf(Project("Tutorial", root, setOf(input)))).exitCode)
            return checkNotNull(result)
        } finally {
            LinearExecutor.hooks = previousHooks
            LinearExecutor.executionOptions = previousOptions
        }
    }

    @Test
    fun initialRuleBuildsTheDocumentedInvocationAndUsesNormalExitCodes() {
        val rule = rule(0)
        assertEquals(listOf("pdflatex", "-interaction=nonstopmode", "article.tex"), command(rule).elements)
        assertTrue(rule.commands.single().isSuccessful(0))
        assertFalse(rule.commands.single().isSuccessful(1))
    }

    @Test
    fun completeRuleAppliesDefaultsAndOmitsAbsentOptionalArguments() {
        assertEquals(listOf("pdflatex", "-interaction=nonstopmode", "-synctex=0", "article.tex"),
            command(rule(1)).elements)
    }

    @Test
    fun flagsAndOptionsKeepSpacesAndQuotesInsideTheirArgumentBoundaries() {
        val parameters = mapOf(
            "interaction" to "batchmode", "draft" to true, "synctex" to true,
            "jobname" to "Peter's notes", "options" to listOf("-file-line-error", "-output-directory=build notes")
        )
        assertEquals(listOf("pdflatex", "-interaction=batchmode", "-draftmode", "-synctex=1",
            "-jobname=Peter's notes", "-file-line-error", "-output-directory=build notes", "chapter one.tex"),
            command(rule(1), parameters, "chapter one.tex").elements)
    }

    @Test
    fun malformedParametersFailWhileConstructingTheCommand() {
        val rule = rule(1)
        val invalid: List<Map<String, Any>> = listOf(
            mapOf("interaction" to "nonstop"), mapOf("interaction" to 42),
            mapOf("draft" to "yes"), mapOf("synctex" to 1),
            mapOf("jobname" to "  "), mapOf("jobname" to false),
            mapOf("options" to "-file-line-error"), mapOf("options" to listOf(42)),
            mapOf("options" to mapOf("key" to "value"))
        )
        invalid.forEach { parameters ->
            assertFailsWith<AraraException>(parameters.toString()) { command(rule, parameters) }
        }
    }

    @Test
    fun eachSelectedReferenceGetsItsOwnNameAndPassesCanUseDifferentArguments() {
        val rule = rule(1)
        listOf("chapter one.tex", "chapter two.tex").forEach { filename ->
            assertEquals(filename, command(rule, mapOf("synctex" to true), filename).elements.last())
        }
        assertTrue("-draftmode" in command(rule, mapOf("draft" to true)).elements)
        val finalPass = command(rule, mapOf("synctex" to true)).elements
        assertTrue("-synctex=1" in finalPass)
        assertFalse("-draftmode" in finalPass)
    }
}
