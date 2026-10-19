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
        root.resolve("project.lua").writeText("""
            return { workingDirectory = 'sources', files = { ['a.custom.src'] = {} } }
        """.trimIndent())
        assertEquals(0, run(root, "project.lua"))
        assertEquals(true, Session["ran"])
    }
}
