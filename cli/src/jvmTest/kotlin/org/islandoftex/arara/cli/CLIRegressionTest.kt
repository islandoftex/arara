// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.mvel.utils.MvelState
import java.nio.file.Files
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CLIRegressionTest {
    private fun fixture(test: (File) -> Unit) {
        val root = Files.createTempDirectory("arara-cli-regression").toFile()
        val options = LinearExecutor.executionOptions
        val hooks = LinearExecutor.hooks
        val ui = Session.userInterfaceOptions
        val logging = Session.loggingOptions
        val preambles = MvelState.preambles.toMap()
        val defaultPreamble = MvelState.defaultPreamble
        val prepend = MvelState.prependPreambleIfDirectivesGiven
        try {
            Session.clear()
            LinearExecutor.executionOptions = ExecutionOptions()
            test(root)
        } finally {
            LinearExecutor.executionOptions = options
            LinearExecutor.hooks = hooks
            Session.userInterfaceOptions = ui
            Session.loggingOptions = logging
            MvelState.preambles.clear()
            MvelState.preambles.putAll(preambles)
            MvelState.defaultPreamble = defaultPreamble
            MvelState.prependPreambleIfDirectivesGiven = prepend
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
        listOf(listOf("--engine", "lua") to 1, emptyList<String>() to 0).forEach { (flags, expected) ->
            val status = assertFailsWith<ProgramResult> {
                CLI().parse(listOf("-d", root.toString()) + flags + "run.tex")
            }
            assertEquals(expected, status.statusCode)
        }
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
        val status = assertFailsWith<ProgramResult> { CLI().parse(listOf("-d", root.path, "run.tex")) }
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
        val status = assertFailsWith<ProgramResult> { CLI().parse(listOf("-d", root.path, "project.proj.lua")) }
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
            CLI().parse(listOf("-d", root.path, "-p", "selected", "run.tex"))
        }
        assertEquals(0, status.statusCode)
        assertEquals(true, Session["marker"])
    }

    @Test
    fun cliRestoresHostConfigurationAndPreamblesAfterRunning() = fixture { root ->
        val original = LinearExecutor.executionOptions
        MvelState.preambles["host"] = "% arara: host"
        File(root, "arararc.yaml").writeText("!config\npaths: ['.']\nloops: 3\npreambles: {local: '% arara: marker'}\n")
        markerRule(root)
        File(root, "run.tex").writeText("% arara: marker\n")
        val status = assertFailsWith<ProgramResult> { CLI().parse(listOf("-d", root.path, "run.tex")) }
        assertEquals(0, status.statusCode)
        assertEquals(original, LinearExecutor.executionOptions)
        assertEquals(mapOf("host" to "% arara: host"), MvelState.preambles)
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
}
