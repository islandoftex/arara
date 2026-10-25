// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.PreambleState
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

class CLIRegressionTest {
    private fun fixture(test: (File) -> Unit) {
        val root = Files.createTempDirectory("arara-cli-regression").toFile()
        val options = LinearExecutor.executionOptions
        val hooks = LinearExecutor.hooks
        val ui = Session.userInterfaceOptions
        val logging = Session.loggingOptions
        val preambles = PreambleState.preambles.toMap()
        val defaultPreamble = PreambleState.defaultPreamble
        val prepend = PreambleState.prependPreambleIfDirectivesGiven
        try {
            Session.clear()
            LinearExecutor.executionOptions = ExecutionOptions()
            test(root)
        } finally {
            LinearExecutor.executionOptions = options
            LinearExecutor.hooks = hooks
            Session.userInterfaceOptions = ui
            Session.loggingOptions = logging
            PreambleState.preambles.clear()
            PreambleState.preambles.putAll(preambles)
            PreambleState.defaultPreamble = defaultPreamble
            PreambleState.prependPreambleIfDirectivesGiven = prepend
            Session.clear()
            HaltRequest.clear()
            root.deleteRecursively()
        }
    }

    @Test
    fun engineSelectionStaysWithEachInvocationIncludingReplicatedDirectives() = fixture { directory ->
        val root = directory.toPath().toMPPPath()
        root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
        root.resolve("run.tex").writeText("% arara: probe: { files: ['run.tex'] }\n")
        root.resolve("probe.yaml").writeText("!config\nidentifier: probe\nname: Probe\n" +
            "commands:\n  - command: '@{ true }'\narguments: []\n")
        root.resolve("probe.lua").writeText("return {identifier='probe', name='Probe', " +
            "commands={{command=function() return false end}}}")
        listOf(
            listOf("--preferred-engine", "lua") to 1,
            listOf("-e", "lua") to 1,
            listOf("-e", "yaml") to 0,
            emptyList<String>() to 1
        ).forEach { (flags, expected) ->
            val status = assertFailsWith<ProgramResult> {
                CLI().parse(listOf("-d", root.toString()) + flags + "run.tex")
            }
            assertEquals(expected, status.statusCode)
        }
    }

    @Test
    fun fallbackUsesTheResolvedRulesInterpolationAndConditionalLanguage() = fixture { directory ->
        val root = directory.toPath().toMPPPath()
        root.resolve("arararc.lua").writeText("return { paths={cwd} }")
        root.resolve("legacy.yaml").writeText("""
            !config
            identifier: legacy
            name: Legacy
            commands:
              - command: '@{ return options[0] == "3"; }'
            arguments:
              - identifier: options
                flag: '@{ return parameters.options; }'
        """.trimIndent())
        root.resolve("modern.lua").writeText("""
            return { identifier='modern', name='Modern',
              commands={{command=function(ctx) return ctx.options[1] == '3' end}},
              arguments={{identifier='options', flag=function(ctx, value) return value end}} }
        """.trimIndent())
        root.resolve("run.tex").writeText("""
            % arara: legacy: { options: '@{1+2}' } if true && true
            % arara: modern: { options: '{{1+2}}' } if true and true
        """.trimIndent())
        for (preferred in listOf("lua", "yaml")) {
            val status = assertFailsWith<ProgramResult> {
                CLI().parse(listOf("--preferred-engine", preferred, "-d", root.toString(), "run.tex"))
            }
            assertEquals(0, status.statusCode)
        }
        root.resolve("modern.yaml").writeText("""
            !config
            identifier: modern
            name: Modern fallback
            commands:
              - command: '@{ true }'
            arguments: []
        """.trimIndent())
        root.resolve("modern.lua").writeText("return { invalid Lua")
        root.resolve("run.tex").writeText("% arara: modern\n")
        val malformed = assertFailsWith<ProgramResult> {
            CLI().parse(listOf("-e", "lua", "-d", root.toString(), "run.tex"))
        }
        assertEquals(2, malformed.statusCode)
    }

    @Test
    fun bareHaltReachesTheCliWithItsExactStatusAndStopsFollowingDirectives() = fixture { root ->
        File(root, "arararc.yaml").writeText("!config\npaths: ['.']\n")
        File(root, "stop.yaml").writeText("""
            !config
            identifier: stop
            name: Stop
            commands:
              - command: "@{ halt(7); }"
            arguments: []
        """.trimIndent())
        File(root, "run.tex").writeText("% arara: stop\n% arara: missingrule\n")
        val status = assertFailsWith<ProgramResult> { CLI().parse(listOf("--preferred-engine", "yaml", "-d", root.path, "run.tex")) }
        assertEquals(7, status.statusCode)
    }

    @Test
    fun luaProjectsLoadTheConfigurationFromTheirOwnWorkingDirectory() = fixture { root ->
        val project = File(root, "project").apply { mkdir() }
        File(project, "arararc.yaml").writeText("!config\npaths: ['.']\n")
        markerRule(project)
        File(project, "run.tex").writeText("% arara: marker\n")
        File(root, "project.proj.lua").writeText("""
            return {
              name = "Configured",
              working_directory = "${project.invariantSeparatorsPath}",
              files = { ["run.tex"] = { file_type = { extension = "tex" } } }
            }
        """.trimIndent())
        val status = assertFailsWith<ProgramResult> { CLI().parse(listOf("--preferred-engine", "yaml", "-d", root.path, "project.proj.lua")) }
        assertEquals(0, status.statusCode)
        assertEquals(true, Session["marker"])
    }

    @Test
    fun explicitPreambleOverridesTheAutomaticPrependingSetting() = fixture { root ->
        File(root, "arararc.yaml").writeText("""
            !config
            paths: ['.']
            prependPreambleIfDirectivesGiven: false
            preambles:
              selected: "% arara: marker"
        """.trimIndent())
        markerRule(root)
        File(root, "noop.yaml").writeText("!config\nidentifier: noop\nname: Noop\ncommands:\n  - command: '@{ true }'\narguments: []\n")
        File(root, "run.tex").writeText("% arara: noop\n")
        val status = assertFailsWith<ProgramResult> {
            CLI().parse(listOf("--preferred-engine", "yaml", "-d", root.path, "-p", "selected", "run.tex"))
        }
        assertEquals(0, status.statusCode)
        assertEquals(true, Session["marker"])
    }

    @Test
    fun cliRestoresHostConfigurationAndPreamblesAfterRunning() = fixture { root ->
        val original = LinearExecutor.executionOptions
        PreambleState.preambles["host"] = "% arara: host"
        File(root, "arararc.yaml").writeText("!config\npaths: ['.']\nloops: 3\npreambles: {local: '% arara: marker'}\n")
        markerRule(root)
        File(root, "run.tex").writeText("% arara: marker\n")
        val status = assertFailsWith<ProgramResult> { CLI().parse(listOf("--preferred-engine", "yaml", "-d", root.path, "run.tex")) }
        assertEquals(0, status.statusCode)
        assertEquals(original, LinearExecutor.executionOptions)
        assertEquals(mapOf("host" to "% arara: host"), PreambleState.preambles)
    }

    private fun markerRule(root: File) {
        File(root, "marker.yaml").writeText("""
            !config
            identifier: marker
            name: Marker
            commands:
              - command: "@{ getSession().put('marker', true); return true; }"
            arguments: []
        """.trimIndent())
    }

    @Test
    fun malformedPropertiesRemainBestEffortAndAreLoggedAfterLoggingStarts() = fixture { directory ->
        val root = directory.toPath().toMPPPath()
        root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
        root.resolve("run.tex").writeText("% arara: marker\n")
        markerRule(directory)
        val properties = root.resolve("broken.properties")
        properties.writeText("loaded=from-file\nbroken=\\uZZZZ\n")
        val log = root.resolve("diagnostic.log")
        Session.loggingOptions = org.islandoftex.arara.core.configuration.LoggingOptions(logFile = log)
        val result = assertFailsWith<ProgramResult> {
            CLI().parse(listOf("--preferred-engine", "yaml", "-d", root.toString(), "-l", "--properties-file", properties.toString(),
                "--call-property", "fallback=from-cli", "run.tex"))
        }
        assertEquals(0, result.statusCode)
        assertEquals("from-file", Session["arg:loaded"])
        assertEquals("from-cli", Session["arg:fallback"])
        assertTrue(log.readText().contains("Could not load the properties file $properties"))
    }
}
