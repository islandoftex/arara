// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjectsRegressionTest {
    private fun fixture(test: (MPPPath) -> Unit) {
        val root = MPPPath(Files.createTempDirectory("arara-projects").toString())
        val snapshot = ConfigurationSnapshot.capture()
        try {
            Session.clear()
            LinearExecutor.executionOptions = ExecutionOptions()
            test(root)
        } finally {
            snapshot.restore()
            Session.clear()
            root.removeDirectory()
        }
    }

    private fun run(root: MPPPath, vararg references: String): Int =
        assertFailsWith<ProgramResult> {
            CLI().parse(listOf("-d", root.toString()) + references)
        }.statusCode

    private fun marker(root: MPPPath, value: String = "ran") {
        root.resolve("marker.yaml").writeText("""
            !config
            identifier: marker
            name: Marker
            commands:
              - command: "@{ getSession().put('$value', true); return true; }"
            arguments: []
        """.trimIndent())
    }

    @Test
    fun implicitTypesUseTheProjectsLocalConfigurationAndRelativeDirectory() = fixture { root ->
        val sources = root.resolve("sources").apply { createDirectories() }
        sources.resolve("arararc.yaml").writeText("""
            !config
            paths: ['.']
            filetypes:
              - extension: custom.src
                pattern: '^@ +'
        """.trimIndent())
        marker(sources)
        sources.resolve("a.custom.src").writeText("@ arara: marker\n")
        root.resolve("project.proj.lua").writeText("""
            return { working_directory = 'sources', files = { ['a.custom.src'] = {} } }
        """.trimIndent())
        assertEquals(0, run(root, "project.proj.lua"))
        assertEquals(true, Session["ran"])
    }

    @Test
    fun ordinaryLuaInputsAreNeverExecutedAsSpecifications() = fixture { root ->
        root.resolve("arararc.yaml").writeText("""
            !config
            paths: ['.']
            filetypes:
              - extension: lua
                pattern: '^-- +'
        """.trimIndent())
        marker(root)
        root.resolve("ordinary.lua").writeText("-- arara: marker\nerror('must not execute')\n")
        assertEquals(0, run(root, "ordinary.lua"))
        assertEquals(true, Session["ran"])
    }

    @Test
    fun malformedSpecificationsDoNotFallBackToDirectiveScanning() = fixture { root ->
        root.resolve("arararc.yaml").writeText("""
            !config
            paths: ['.']
            filetypes:
              - extension: proj.lua
                pattern: '^% +'
        """.trimIndent())
        marker(root)
        root.resolve("invalid.proj.lua").writeText("% arara: marker\n")
        assertEquals(2, run(root, "invalid.proj.lua"))
        assertEquals(false, "ran" in Session)
    }

    @Test
    fun specificationsShareDependenciesButKeepTheirOwnConfiguration() = fixture { root ->
        val first = root.resolve("first").apply { createDirectories() }
        val second = root.resolve("second").apply { createDirectories() }
        for ((directory, prefix) in listOf(first to "@", second to "#")) {
            directory.resolve("arararc.yaml").writeText("""
                !config
                paths: ['.']
                filetypes:
                  - extension: custom.src
                    pattern: '^$prefix +'
            """.trimIndent())
            directory.resolve("a.custom.src").writeText("$prefix arara: marker\n")
        }
        marker(first, "first")
        second.resolve("marker.yaml").writeText("""
            !config
            identifier: marker
            name: Marker
            commands:
              - command: "@{ getSession().get('first'); getSession().put('second', true); return true; }"
            arguments: []
        """.trimIndent())
        root.resolve("first.proj.lua").writeText("""
            return { name = 'Z', working_directory = 'first', files = { ['a.custom.src'] = {} } }
        """.trimIndent())
        root.resolve("second.proj.lua").writeText("""
            return { name = 'A', working_directory = 'second', files = { ['a.custom.src'] = {} },
                     dependencies = { 'Z' } }
        """.trimIndent())
        assertEquals(0, run(root, "second.proj.lua", "first.proj.lua"))
        assertEquals(true, Session["first"])
        assertEquals(true, Session["second"])
    }

    @Test
    fun invalidCombinedGraphsAndOverlappingFilesFailBeforeRunningRules() = fixture { root ->
        root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
        marker(root)
        root.resolve("a.tex").writeText("% arara: marker\n")
        root.resolve("b.tex").writeText("% arara: marker\n")
        val cases = listOf(
            "name = 'Same', files = { ['a.tex'] = {} }" to
                "name = 'Same', files = { ['b.tex'] = {} }",
            "name = 'A', files = { ['a.tex'] = {} }, dependencies = { 'B' }" to
                "name = 'B', files = { ['b.tex'] = {} }, dependencies = { 'A' }",
            "name = 'A', files = { ['a.tex'] = {} }, dependencies = { 'Missing' }" to
                "name = 'B', files = { ['b.tex'] = {} }",
            "name = 'A', files = { ['a.tex'] = {} }" to
                "name = 'B', files = { ['./a.tex'] = { priority = 1 } }"
        )
        for ((first, second) in cases) {
            root.resolve("first.proj.lua").writeText("return { $first }")
            root.resolve("second.proj.lua").writeText("return { $second }")
            assertEquals(2, run(root, "first.proj.lua", "second.proj.lua"))
            assertEquals(false, "ran" in Session)
        }
    }

    @Test
    fun oneFileRunsMultipleDirectivesInDeclarationOrder() = fixture { root ->
        root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
        root.resolve("a.tex").writeText("% arara: missingrule\n")
        for (mode in listOf("without", "with")) {
            root.resolve("$mode.yaml").writeText("""
                !config
                identifier: $mode
                name: $mode
                commands:
                  - command: >
                      @{
                        old = getSession().contains('runs') ? getSession().get('runs') : '';
                        getSession().put('runs', old + '$mode;');
                        return true;
                      }
                arguments: []
            """.trimIndent())
        }
        root.resolve("project.proj.lua").writeText("""
            return { files = { ['a.tex'] = {
              directives = { '% arara: without', '% arara: with' }
            } } }
        """.trimIndent())
        assertEquals(0, run(root, "project.proj.lua"))
        assertEquals("without;with;", Session["runs"])
    }
}
