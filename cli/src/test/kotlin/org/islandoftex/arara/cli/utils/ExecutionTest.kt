// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationUtils
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Runs the end-to-end execution scenarios on the default YAML engine:
 * each scenario is a fixture directory under the execution tests holding
 * its own configuration and rules, and the captured terminal output is
 * asserted against the expected markers. The executor hooks install
 * the command line application's wiring once per class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExecutionTest {
    @BeforeAll
    fun setUp() {
        DirectiveUtils.initializeDirectiveCore()

        LinearExecutor.hooks = ExecutorHooks(
            executeBeforeProject = { project ->
                ConfigurationUtils.configFileForProject(project)?.let {
                    DisplayUtils.configurationFileName = it.toString()
                    ConfigurationUtils.load(it, project)
                }
            },
            executeBeforeFile = {
                it.printFileInformation()
            },
            processDirectives = { file, list ->
                DirectiveUtils.process(file, list)
            }
        )
    }

    private fun getPathForTest(name: String): String =
        "src/test/resources/executiontests/$name"

    /**
     * Runs the scenario through the linear executor and returns the
     * captured terminal output, restoring the standard output stream
     * no matter how the run ends.
     */
    private fun outputForTest(
        testName: String,
        fileName: String = "$testName.tex"
    ): String {
        val sysout = System.out
        val output = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(output))
            val workingDirectory = MPPPath(getPathForTest(testName))
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
            return output.toByteArray().toString(Charsets.UTF_8)
        } finally {
            System.setOut(sysout)
            output.close()
        }
    }

    @Test
    fun `is able to store variables in sessions`() {
        val output = outputForTest("sessions")
        assertTrue(
            output.contains("MeowQuack"),
            "the session value is visible to the rule"
        )
    }

    @Test
    fun `honors the falsy existence test`() {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        if (file.exists()) file.delete()
        val output = outputForTest("conditionals")
        assertTrue(output.contains("QuackOne"), "the unconditional directive ran")
        assertTrue(output.contains("QuackFour"), "the always true directive ran")
        assertFalse(
            output.contains("QuackTwo"),
            "the directive with a falsy existence condition is skipped"
        )
        assertFalse(
            output.contains("QuackThree"),
            "the directive with a falsy contains condition is skipped"
        )
    }

    @Test
    fun `honors the truthy existence and falsy contains test`() {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        file.writeText("Meow")
        val output = outputForTest("conditionals")
        file.delete()
        assertTrue(output.contains("QuackOne"), "the unconditional directive ran")
        assertTrue(output.contains("QuackFour"), "the always true directive ran")
        assertTrue(
            output.contains("QuackTwo"),
            "the directive with a truthy existence condition ran"
        )
        assertFalse(
            output.contains("QuackThree"),
            "the directive with a falsy contains condition is skipped"
        )
    }

    @Test
    fun `honors the truthy contains test`() {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        file.writeText("Duck")
        val output = outputForTest("conditionals")
        file.delete()
        assertTrue(output.contains("QuackOne"), "the unconditional directive ran")
        assertTrue(output.contains("QuackFour"), "the always true directive ran")
        assertTrue(output.contains("QuackTwo"), "the existence directive ran")
        assertTrue(
            output.contains("QuackThree"),
            "the directive with a truthy contains condition ran"
        )
    }

    @Test
    fun `tracks changes`() {
        val file = File(getPathForTest("changes") + "/arara.yaml")
        if (file.exists()) file.delete()
        assertTrue(
            outputForTest("changes").contains("QuackOne"),
            "the first run executes the directive"
        )
        assertFalse(
            outputForTest("changes").contains("QuackOne"),
            "the second run skips the unchanged file"
        )
    }

    @Test
    fun `gracefully halts on the halt rule`() {
        // a fresh session keeps the scenario independent from any halt
        // keys left behind by other specs or scenarios
        Session.clear()
        val output = outputForTest("halt")
        assertTrue(output.contains("QuackOne"), "the pre-halt directive ran")
        assertFalse(
            output.contains("QuackTwo"),
            "the post-halt directive is skipped"
        )
    }

    @Test
    fun `forcefully halts on the halt error rule`() {
        // a fresh session keeps the scenario independent from any halt
        // keys left behind by other specs or scenarios
        Session.clear()
        val output = outputForTest("halt-error")
        assertTrue(output.contains("QuackOne"), "the pre-halt directive ran")
        assertFalse(
            output.contains("QuackTwo"),
            "the post-halt directive is skipped"
        )
    }

    @Test
    fun `fails on an invalid config`() {
        val exception = assertFailsWith<AraraException>(
            "a broken configuration file must fail the run"
        ) {
            outputForTest("invalid-config")
        }
        assertTrue(
            exception.message?.contains("could not parse the configuration") == true,
            "the error names the configuration problem"
        )
    }

    @Test
    fun `accepts an empty pattern on a known extension`() {
        val output = outputForTest("known-extension")
        assertTrue(
            output.contains("QuackOne"),
            "the rule executes with an empty pattern for a known extension"
        )
    }

    @Test
    fun `executes option-less directives`() {
        val output = outputForTest("simple-directive")
        assertTrue(
            output.contains("The echoer"),
            "the rule entry is displayed"
        )
        assertTrue(output.contains("SUCCESS"), "the entry result")
    }

    @Test
    fun `executes a directive with options`() {
        val output = outputForTest("directive-with-options")
        assertTrue(output.contains("The echoer"), "the rule entry is displayed")
        assertTrue(output.contains("batchmode"), "the evaluated option flag")
        assertTrue(output.contains("SUCCESS"), "the entry result")
        assertFalse(output.contains("FAILURE"), "no failure is reported")
    }

    @Test
    fun `interprets orb tags in directives`() {
        val output = outputForTest("directive-with-mvel-options")
        assertTrue(
            output.contains("testdirective-with-mvel-optionsquack"),
            "the orb tag was interpolated with the file base name"
        )
        assertTrue(output.contains("nothing"), "the second directive ran")
    }

    @Test
    fun `does not interpret orb tags in directives in safe mode`() {
        val options = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions.from(options)
            .copy(executionMode = ExecutionMode.SAFE_RUN)
        try {
            val output = outputForTest("directive-with-mvel-options")
            assertFalse(
                output.contains("testdirective-with-mvel-optionsquack"),
                "the orb tag was not interpolated in safe mode"
            )
            assertTrue(
                output.contains("test@{getBasename(getOriginalReference())}quack"),
                "the orb tag passes through literally in safe mode"
            )
            assertTrue(output.contains("nothing"), "the second directive ran")
        } finally {
            // restore no matter whether the assertions held, so a failure
            // cannot leak the safe mode into the subsequent scenarios
            LinearExecutor.executionOptions = options
        }
    }

    @Test
    fun `processes multiple files given a files array`() {
        val output = outputForTest("multiple-files")
        assertTrue(output.contains("doc1.tex"), "the first file was processed")
        assertTrue(output.contains("doc2.tex"), "the second file was processed")
        assertFalse(
            output.contains("doc3.tex"),
            "the file outside the array was not processed"
        )
    }

    @Test
    fun `identifies types correctly`() {
        val output = outputForTest("typetests")
        assertTrue(output.contains("Bool test success"), "the boolean test")
        assertTrue(output.contains("String test success"), "the string test")
        assertTrue(output.contains("List test success"), "the list test")
        assertTrue(output.contains("Map test success"), "the map test")
    }

    @Test
    fun `skips comments in header mode`() {
        val output = outputForTest("header-comments")
        val occurrences =
            (output.length - output.replace("QuackOne", "").length) /
                "QuackOne".length
        assertEquals(
            3, occurrences,
            "the directives inside the header comments are not executed"
        )
    }
}
