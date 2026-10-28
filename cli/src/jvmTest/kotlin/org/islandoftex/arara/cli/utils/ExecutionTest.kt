// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.session.ExecutorHooks
import org.islandoftex.arara.cli.configuration.ConfigurationFiles
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.LinearExecutor
import org.junit.jupiter.api.parallel.Isolated

@Isolated
class ExecutionTest {
    private val originalHooks = LinearExecutor.hooks
    private val originalOptions = LinearExecutor.executionOptions
    private val originalDirectiveHooks = Directives.hooks
    private val originalConfigurationName = DisplayUtils.configurationFileName

    @AfterTest
    fun restoreState() {
        LinearExecutor.hooks = originalHooks
        LinearExecutor.executionOptions = originalOptions
        Directives.hooks = originalDirectiveHooks
        DisplayUtils.configurationFileName = originalConfigurationName
    }

    @BeforeTest
    fun prepareTest() {
        DirectiveUtils.initializeDirectiveCore()
        LinearExecutor.executionOptions = ExecutionOptions()

        LinearExecutor.hooks = ExecutorHooks(
            executeBeforeProject = { project ->
                ConfigurationFiles.configFileForProject(project)?.let {
                    DisplayUtils.configurationFileName = it.toString()
                    ConfigurationFiles.load(it, project)
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

    private fun getPathForTest(name: String): String = "src/jvmTest/resources/executiontests/$name"
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
    fun `be able to store variables sessions`() {
        assertContains(outputForTest("sessions"), "MeowQuack")
    }

    @Test
    fun `honor falsy existence test`() {
        val file = MPPPath(getPathForTest("conditionals")).resolve("conditionals.quack")
        Files.deleteIfExists(file.toJVMPath())
        val output = outputForTest("conditionals")
        assertContains(output, "QuackOne")
        assertContains(output, "QuackFour")
        assertFalse(output.contains("QuackTwo"))
        assertFalse(output.contains("QuackThree"))
    }

    @Test
    fun `honor truthy existence (falsy contains) test`() {
        val file = MPPPath(getPathForTest("conditionals")).resolve("conditionals.quack")
        file.writeText("Meow")
        val output = outputForTest("conditionals")
        Files.deleteIfExists(file.toJVMPath())
        assertContains(output, "QuackOne")
        assertContains(output, "QuackFour")
        assertContains(output, "QuackTwo")
        assertFalse(output.contains("QuackThree"))
    }

    @Test
    fun `honor truthy contains test`() {
        val file = MPPPath(getPathForTest("conditionals")).resolve("conditionals.quack")
        file.writeText("Duck")
        val output = outputForTest("conditionals")
        Files.deleteIfExists(file.toJVMPath())
        assertContains(output, "QuackOne")
        assertContains(output, "QuackFour")
        assertContains(output, "QuackTwo")
        assertContains(output, "QuackThree")
    }

    @Test
    fun `track changes`() {
        val cache = MPPPath(getPathForTest("changes")).resolve("arara.yaml")
        Files.deleteIfExists(cache.toJVMPath())
        try {
            assertContains(outputForTest("changes"), "QuackOne")
            assertFalse(outputForTest("changes").contains("QuackOne"))
        } finally {
            Files.deleteIfExists(cache.toJVMPath())
        }
    }

    @Test
    fun `gracefully halt on halt rule`() {
        val output = outputForTest("halt")
        assertContains(output, "QuackOne")
        assertFalse(output.contains("QuackTwo"))
    }

    @Test
    fun `forcefully halt on halt error rule`() {
        val output = outputForTest("halt-error")
        assertContains(output, "QuackOne")
        assertFalse(output.contains("QuackTwo"))
    }

    @Test
    fun `fail on invalid config`() {
        val exception = assertFailsWith<AraraException> {
            outputForTest("invalid-config")
        }
        assertContains(exception.message.orEmpty(), "could not parse the configuration")
    }

    @Test
    fun `accept empty pattern on known extension`() {
        val output = outputForTest("known-extension")
        assertContains(output, "QuackOne")
    }

    @Test
    fun `execute option-less directives`() {
        val output = outputForTest("simple-directive")
        assertContains(output, "The echoer")
        assertContains(output, "SUCCESS")
    }

    @Test
    fun `execute directive with options`() {
        val output = outputForTest("directive-with-options")
        assertContains(output, "The echoer")
        assertContains(output, "batchmode")
        assertContains(output, "SUCCESS")
        assertFalse(output.contains("FAILURE"))
    }

    @Test
    fun `interpret orb tags in directives`() {
        val output = outputForTest("directive-with-mvel-options")
        assertContains(output, "testdirective-with-mvel-optionsquack")
        assertContains(output, "nothing")
    }

    @Test
    fun `not interpret orb tags in directives in safe mode`() {
        val options = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions.from(options)
            .copy(executionMode = ExecutionMode.SAFE_RUN)
        val output = outputForTest("directive-with-mvel-options")
        assertFalse(output.contains("testdirective-with-mvel-optionsquack"))
        assertContains(output, "test@{getBasename(getOriginalReference())}quack")
        assertContains(output, "nothing")
        LinearExecutor.executionOptions = options
    }

    @Test
    fun `process multiple files (files array)`() {
        val output = outputForTest("multiple-files")
        assertContains(output, "doc1.tex")
        assertContains(output, "doc2.tex")
        assertFalse(output.contains("doc3.tex"))
    }

    @Test
    fun `identify types correctly`() {
        val output = outputForTest("typetests")
        assertContains(output, "Bool test success")
        assertContains(output, "String test success")
        assertContains(output, "List test success")
        assertContains(output, "Map test success")
    }

    @Test
    fun `skip comments in header mode`() {
        val output = outputForTest("header-comments")
        assertEquals(3, (output.length - output.replace("QuackOne", "").length) / "QuackOne".length)
    }
}
