// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.session.Session

class BuildDirectoryRegressionTest {
    private fun fixture(test: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara-build-cli").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        try {
            Session.clear()
            root.resolve("arararc.lua").writeText("return {paths={'.'}}")
            root.resolve("book.tex").writeText("% arara: probe\n")
            root.resolve("project.proj.lua").writeText("""
                return { name='book', files={['book.tex']={}}, build={directory='build/book'} }
            """.trimIndent())
            test(root)
        } finally {
            snapshot.restore()
            Session.clear()
            root.removeDirectory()
        }
    }

    private fun run(root: MPPPath, vararg flags: String): Int = assertFailsWith<ProgramResult> {
        CLI().parse(listOf("-d", root.toString()) + flags + "project.proj.lua")
    }.statusCode

    @Test
    fun bothEnginesReceiveStagedReferencesAndWorkspaceCommandsButSourceConfiguration() = fixture { root ->
        root.resolve("probe.lua").writeText("""
            return {identifier='probe', name='Probe', commands={{command=function(ctx)
              assert(ctx.reference.path == tostring(current_file()))
              assert(current_file():read_text():find('arara: probe'))
              current_file():resolve_sibling('result.txt'):write_text('lua')
              return true
            end}}}
        """.trimIndent())
        assertEquals(0, run(root))
        assertEquals("lua", root.resolve("build/book/result.txt").readText())
        assertFalse(root.resolve("result.txt").exists)
        root.resolve("probe.yaml").writeText("""
            !config
            identifier: probe
            name: Probe
            commands:
              - command: "@{ getSession().put('cwd', getCommand(['echo']).workingDirectory.toString()); return true; }"
            arguments: []
        """.trimIndent())
        assertEquals(0, run(root, "-e", "yaml"))
        assertEquals(root.resolve("build/book").toString(), Session["cwd"])
        assertEquals(0, run(root, "--clean-build"))
        assertFalse(root.resolve("build/book").exists)
        assertTrue(root.resolve("book.tex").exists)
    }

    @Test
    fun stagingWaitsForDependenciesAndRetainsInlineDirectives() = fixture { root ->
        root.resolve("producer").createDirectories()
        root.resolve("producer/seed.tex").writeText("% arara: generate\n")
        root.resolve("producer/arararc.lua").writeText("return {paths={'..'}}")
        root.resolve("generate.lua").writeText("""
            return {identifier='generate', name='Generate', commands={{command=function(ctx)
              current_file():resolve_sibling('../generated.tex'):write_text('generated input')
              return true
            end}}}
        """.trimIndent())
        root.resolve("probe.lua").writeText("""
            return {identifier='probe', name='Probe', commands={{command=function(ctx)
              assert(to_file(ctx.reference.path):read_text() == 'generated input')
              to_file(ctx.reference.path):resolve_sibling('result.txt'):write_text('compiled')
              return true
            end}}}
        """.trimIndent())
        root.resolve("project.proj.lua").writeText("""
            return {
              {name='producer', working_directory='producer', files={['seed.tex']={}}},
              {name='consumer', dependencies={'producer'},
               files={['generated.tex']={directives={'% arara: probe'}}},
               build={directory='build/consumer',inputs={}}}
            }
        """.trimIndent())
        assertEquals(0, run(root))
        assertEquals("compiled", root.resolve("build/consumer/result.txt").readText())
    }

    @Test
    fun dryRunsDoNotStageAndUnmanagedDirectoriesCannotBeCleaned() = fixture { root ->
        root.resolve("probe.lua").writeText("""
            return {identifier='probe', name='Probe', commands={{command=function(ctx) return true end}}}
        """.trimIndent())
        assertEquals(0, run(root, "--dry-run"))
        assertFalse(root.resolve("build/book").exists)
        root.resolve("build/book").createDirectories()
        root.resolve("build/book/keep.txt").writeText("keep")
        assertEquals(2, run(root, "--clean-build"))
        assertEquals("keep", root.resolve("build/book/keep.txt").readText())
    }
}
