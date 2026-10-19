// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.configuration

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.Project
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            logging: true
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
            assertEquals(MPPPath("cache.yaml"), options.databaseName)
            assertTrue(model.toLoggingOptions().enableLogging)
            assertEquals(MPPPath("nested/run.log"), model.toLoggingOptions().logFile)
            assertEquals(mapOf("sample" to "% arara: echo"), model.preambles)
            assertEquals("sample", model.defaultPreamble)
            assertFalse(model.prependPreambleIfDirectivesGiven)
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
