// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationUtils
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Verifies the handling of foreign file extensions: a file type provided
 * by the configuration file is fully executable, while an extension no
 * file type covers is rejected. These scenarios were originally sketched
 * as commented-out cases in [ExecutionTest], but they exercise the file
 * type wiring rather than the linear executor, so they live in their own
 * spec.
 *
 * Like the command line application, the configuration file is loaded
 * before the file references are resolved, as the file types it provides
 * are required for the resolution itself.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ForeignExtensionTest {
    @BeforeAll
    fun installHooks() {
        DirectiveUtils.initializeDirectiveCore()

        LinearExecutor.hooks = ExecutorHooks(
            executeBeforeFile = {
                it.printFileInformation()
            },
            processDirectives = { file, list ->
                DirectiveUtils.process(file, list)
            }
        )
    }

    private fun outputForTest(
        testName: String,
        fileName: String
    ): String {
        val sysout = System.out
        val output = ByteArrayOutputStream()
        return try {
            System.setOut(PrintStream(output))
            val workingDirectory = MPPPath(
                "src/test/resources/executiontests/$testName"
            )
            // load the configuration of the scenario first, mirroring the
            // command line application: the file types the configuration
            // provides must be known when the references are resolved
            val probe = Project("Test", workingDirectory, emptySet())
            ConfigurationUtils.configFileForProject(probe)?.let {
                DisplayUtils.configurationFileName = it.toString()
                ConfigurationUtils.load(it, probe)
            }
            LinearExecutor.execute(
                listOf(
                    Project(
                        "Test", workingDirectory,
                        setOf(
                            FileSearching.resolveFile(
                                fileName,
                                workingDirectory,
                                LinearExecutor.executionOptions
                            )
                        )
                    )
                )
            )
            output.toByteArray().toString(Charsets.UTF_8)
        } finally {
            System.setOut(sysout)
            output.close()
        }
    }

    @Test
    fun `reads a document with a foreign extension`() {
        // the configuration file registers the 'my' file type, so the
        // corresponding document is processed like any other
        val output = outputForTest("foreign-extension", "foreign-extension.my")
        assertTrue(
            output.contains("QuackOne"),
            "the document with the configured foreign extension was executed"
        )
    }

    @Test
    fun `fails on an unknown extension`() {
        // no file type covers 'xy', so the reference cannot be resolved
        assertFailsWith<AraraException>(
            "a document with an uncovered extension must be rejected"
        ) {
            outputForTest("foreign-extension", "foreign-extension.xy")
        }
    }
}
