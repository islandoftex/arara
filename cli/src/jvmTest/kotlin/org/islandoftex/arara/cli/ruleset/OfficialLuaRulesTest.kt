// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
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
            assertEquals(yaml.authors, lua.authors, identifier)
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
        val file = Files.createTempFile(MPPPath(".").toJVMPath(), "input with spaces ", ".tex")
            .toMPPPath().normalize()
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
            Files.deleteIfExists(file.toJVMPath())
            directory.removeDirectory()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parameters(rule: Rule, reference: MPPPath, values: Map<String, Any>): Map<String, Any> =
        rule.arguments.associate { argument ->
            check(!argument.isRequired || argument.identifier in values) { "Missing ${argument.identifier}" }
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

    private fun parity(
        identifier: String,
        values: Map<String, Any> = emptyMap(),
        expectedElements: ((MPPPath) -> List<String>)? = null
    ) = inProject { reference ->
        val yaml = org.islandoftex.arara.mvel.rules.Rule.parse(
            rules.resolve("arara-rule-$identifier.yaml"), identifier)
        val lua = LuaRuleParser.parse(rules.resolve("arara-rule-$identifier.lua"), identifier)
        val expected = yaml.commands.flatMap {
            try {
                commandValues(MvelCommandEvaluator(it as org.islandoftex.arara.mvel.rules.SerialRuleCommand)
                    .evaluate(parameters(yaml, reference, values)))
            } catch (exception: org.islandoftex.arara.api.AraraException) {
                throw AssertionError("$identifier: ${exception.exception}", exception)
            }
        }
        val actual = lua.commands.flatMap { commandValues(it.evaluate(parameters(lua, reference, values))) }
        assertEquals(expected, actual, "$identifier $values")
        expectedElements?.let { assertEquals(it(reference), (actual.single() as Pair<*, *>).first) }
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

    @Test
    fun bibliographyAndIndexCommandsMatchYaml() {
        listOf("authorindex", "bib2gls", "biber", "bibtex", "bibtex8", "bibtexu", "makeglossaries",
            "makeglossarieslite", "makeindex", "nomencl", "pbibtex", "texindy", "upbibtex", "upmendex", "xindex", "xindy")
            .forEach { parity(it) }
        parity("songidx", mapOf("input" to "../README.md"))
        parity("bibtex", mapOf("options" to listOf("-min-crossrefs=2")))
        parity("makeindex", mapOf("style" to "custom style.ist", "options" to listOf("-q")))
    }

    @Test
    fun upmendexPreservesArgumentsAndUsesItsOwnCompressionFlag() {
        parity("upmendex") { reference ->
            val base = reference.fileName.removeSuffix(".tex")
            listOf("upmendex", "-t", "$base.ilg", "$base.idx", "-o", "$base.ind")
        }
        parity("upmendex", mapOf("compress" to false, "order" to "word")) { reference ->
            val base = reference.fileName.removeSuffix(".tex")
            listOf("upmendex", "-t", "$base.ilg", "$base.idx", "-o", "$base.ind")
        }
        parity("upmendex", mapOf("compress" to true, "order" to "letter", "style" to "style file.ist",
            "dictionary" to "readings file.dic", "input" to "custom idx", "output" to "custom ind",
            "log" to "custom ilg", "options" to listOf("-q", "-r"))) { reference ->
            val base = reference.fileName.removeSuffix(".tex")
            listOf("upmendex", "-c", "-s", "style file.ist", "-d", "readings file.dic", "-l", "-q", "-r",
                "-t", "$base.custom ilg", "$base.custom idx", "-o", "$base.custom ind")
        }
    }

    @Test
    fun upmendexRejectsInvalidOrderingAndNonListOptions() = inProject { reference ->
        val rules = listOf(
            LuaRuleParser.parse(rules.resolve("arara-rule-upmendex.lua"), "upmendex"),
            org.islandoftex.arara.mvel.rules.Rule.parse(rules.resolve("arara-rule-upmendex.yaml"), "upmendex")
        )
        for (rule in rules) {
            assertFailsWith<AraraException> { parameters(rule, reference, mapOf("order" to "invalid")) }
            assertFailsWith<AraraException> { parameters(rule, reference, mapOf("options" to "-q")) }
        }
    }

    @Test
    fun conversionCommandsMatchYaml() {
        listOf("animate", "dvipdfm", "dvipdfmx", "dvips", "dvipspdf", "dvisvgm", "pdfcrop", "pdftk",
            "pdftocairo", "ps2pdf", "xdvipdfmx").forEach { parity(it) }
        listOf("convert", "ghostscript", "qpdf").forEach { parity(it, mapOf("options" to listOf("--help"))) }
        parity("fig2dev", mapOf("language" to "pdf", "output" to "figure with spaces.pdf"))
        parity("dvipdfmx", mapOf("output" to "../README.md", "options" to listOf("-v")))
        parity("pdfcrop", mapOf("margins" to listOf(10, 20, 30, 40), "clip" to true))
    }

    @Test
    fun mptopdfPreservesItsOwnOptionsAndReferenceArguments() {
        parity("mptopdf") { reference -> listOf("mptopdf", reference.fileName) }
        parity("mptopdf", mapOf("metafun" to true, "texexec" to true, "latex" to true,
            "options" to listOf("--custom=value with spaces"))) { reference ->
            listOf("mptopdf", "--metafun", "--texexec", "--latex", "--custom=value with spaces", reference.fileName)
        }
        parity("mptopdf", mapOf("metafun" to false, "texexec" to false, "latex" to false)) { reference ->
            listOf("mptopdf", reference.fileName)
        }
    }

    @Test
    fun mptopdfRejectsNonListOptionsInBothEngines() = inProject { reference ->
        val rules = listOf(
            LuaRuleParser.parse(rules.resolve("arara-rule-mptopdf.lua"), "mptopdf"),
            org.islandoftex.arara.mvel.rules.Rule.parse(rules.resolve("arara-rule-mptopdf.yaml"), "mptopdf")
        )
        for (rule in rules) {
            assertFailsWith<AraraException> { parameters(rule, reference, mapOf("options" to "--latex")) }
        }
    }

    @Test
    fun workflowCommandsMatchYaml() {
        listOf("asymptote", "detex", "frontespizio", "indent", "knitr", "latexmk", "llmk", "ltx2any",
            "make", "metapost", "perltex", "pythontex", "sage", "sketch", "spix", "texcount", "texfmt", "tikzmake")
            .forEach { parity(it) }
        parity("datatooltk", mapOf("output" to "data output.dbtex"))
        parity("gnuplot", mapOf("input" to listOf("plot.gnuplot")))
        parity("clean", mapOf("extensions" to listOf("aux", "log")))
        parity("copy", mapOf("target" to "copy target.tex"))
    }

    @Test
    fun cleanLabelsIdentifyEachExtension() = inProject { reference ->
        val rule = LuaRuleParser.parse(rules.resolve("arara-rule-clean.lua"), "clean")
        val values = parameters(rule, reference, mapOf("extensions" to listOf("aux", "log")))
        val commands = rule.commands.single().evaluate(values) as List<*>
        assertEquals(listOf("Cleaning aux", "Cleaning log"), commands.map { (it as Command).displayName })
    }

    @Test
    fun workflowHelpersKeepOriginalFileProtectionAndDirectoryBehavior() = inProject { reference ->
        listOf("clean", "move").forEach { identifier ->
            val rule = LuaRuleParser.parse(rules.resolve("arara-rule-$identifier.lua"), identifier)
            val values = if (identifier == "move") mapOf("target" to "moved.tex") else emptyMap()
            kotlin.test.assertFailsWith<org.islandoftex.arara.api.AraraException> {
                rule.commands.single().evaluate(parameters(rule, reference, values))
            }
        }
        val target = reference.parent.resolve("arara lua directory ${reference.fileName}")
        try {
            listOf("mkdir" to true, "rmdir" to false).forEach { (identifier, exists) ->
                val rule = LuaRuleParser.parse(rules.resolve("arara-rule-$identifier.lua"), identifier)
                assertEquals(listOf(true), rule.commands.single().evaluate(
                    parameters(rule, reference, mapOf("target" to target.toString()))))
                assertEquals(exists, target.exists)
            }
        } finally {
            target.removeDirectory()
        }
    }
}
