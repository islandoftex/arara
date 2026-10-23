// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.configuration

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.files.Project

class YamlLocalConfigurationTest {
    private fun withConfiguration(text: String, check: (LocalConfiguration) -> Unit) {
        val directory = Files.createTempDirectory("arara yaml configuration ").toMPPPath()
        try {
            val file = directory.resolve("arararc.yaml")
            file.writeText(text.trimIndent())
            check(YamlLocalConfiguration.load(file))
        } finally {
            directory.removeDirectory()
        }
    }

    @Test
    fun yamlOptionsAndPreamblesKeepTheirBehavior() {
        withConfiguration("""
            !config
            paths: ['@{application.workingDirectory}/custom']
            filetypes: [{ extension: texw, pattern: '^%!' }]
            loops: 3
            verbose: true
            logformat: text
            header: false
            dbname: nested/cache.yaml
            logname: nested/run.log
            preambles: { sample: '% arara: echo' }
            defaultPreamble: sample
            prependPreambleIfDirectivesGiven: false
        """) { model ->
            val project = Project("Sample", MPPPath("/sample"), emptySet())
            val options = model.toExecutionOptions(project, ExecutionOptions())
            assertEquals(project.workingDirectory.resolve("custom"), options.rulePaths.first())
            assertEquals("texw", options.fileTypes.first().extension)
            assertEquals(3, options.maxLoops)
            assertTrue(options.verbose)
            assertFalse(options.parseOnlyHeader)
            assertEquals(project.workingDirectory.resolve("nested/cache.yaml"), options.databaseName)
            assertEquals(LogFormat.TEXT, model.toLoggingOptions().format)
            assertEquals(MPPPath("nested/run.log"), model.toLoggingOptions().logFile)
            assertEquals(mapOf("sample" to "% arara: echo"), model.preambles)
            assertEquals("sample", model.defaultPreamble)
            assertFalse(model.prependPreambleIfDirectivesGiven)
        }
    }

    @Test
    fun quotedPathsAndLiteralPreamblesKeepTheirText() {
        withConfiguration("""
            !config
            logname: 'false'
            preambles:
              primary: |
                % arara: echo: { options: ['true', '001'] }
                % arara: clean
            defaultPreamble: primary
        """) { model ->
            assertEquals(MPPPath("false"), model.toLoggingOptions().logFile)
            assertEquals("% arara: echo: { options: ['true', '001'] }\n% arara: clean\n",
                model.preambles.getValue("primary"))
        }
    }

    @Test
    fun malformedYamlAndInvalidLoopCountsStillFail() {
        assertFailsWith<AraraException> { withConfiguration("loops: 3") { } }
        assertFailsWith<AraraException> { withConfiguration("!config\nunknown: true") { } }
        withConfiguration("!config\nloops: 0") { model ->
            assertFailsWith<AraraException> {
                model.toExecutionOptions(Project("Sample", MPPPath("."), emptySet()))
            }
        }
    }
}
