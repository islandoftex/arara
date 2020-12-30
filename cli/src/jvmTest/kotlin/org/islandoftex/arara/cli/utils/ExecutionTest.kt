// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
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

@DoNotParallelize
class ExecutionTest : ShouldSpec({
    beforeSpec {
        DirectiveUtils.initializeDirectiveCore()

        LinearExecutor.hooks = ExecutorHooks(
                executeBeforeProject = { project ->
                    ConfigurationUtils.configFileForProject(project)?.let {
                        DisplayUtils.configurationFileName = it.toString()
                        ConfigurationUtils.load(it, project)
                    }
                },
                executeBeforeFile = {
                    DisplayUtils.printFileInformation(it)
                },
                processDirectives = { file, list ->
                    DirectiveUtils.process(file, list)
                }
        )
    }

    fun getPathForTest(name: String): String = "src/test/resources/executiontests/$name"
    fun outputForTest(
        testName: String,
        fileName: String = "$testName.tex"
    ): String {
        val sysout = System.out
        val output = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(output))
            val workingDirectory = MPPPath(getPathForTest(testName))
            LinearExecutor.execute(listOf(
                    Project("Test", workingDirectory,
                            setOf(FileSearching.resolveFile(fileName,
                                    workingDirectory,
                                    LinearExecutor.executionOptions)
                            )
                    )
            ))
            return output.toByteArray().toString(Charsets.UTF_8)
        } catch (ex: Exception) {
            throw ex
        } finally {
            System.setOut(sysout)
            output.close()
        }
    }

    should("be able to store variables sessions") {
        outputForTest("sessions") shouldContain "MeowQuack"
    }

    should("honor falsy existence test") {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        if (file.exists()) file.delete()
        val output = outputForTest("conditionals")
        output shouldContain "QuackOne"
        output shouldContain "QuackFour"
        output shouldNotContain "QuackTwo"
        output shouldNotContain "QuackThree"
    }
    should("honor truthy existence (falsy contains) test") {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        file.writeText("Meow")
        val output = outputForTest("conditionals")
        file.delete()
        output shouldContain "QuackOne"
        output shouldContain "QuackFour"
        output shouldContain "QuackTwo"
        output shouldNotContain "QuackThree"
    }
    should("honor truthy contains test") {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        file.writeText("Duck")
        val output = outputForTest("conditionals")
        file.delete()
        output shouldContain "QuackOne"
        output shouldContain "QuackFour"
        output shouldContain "QuackTwo"
        output shouldContain "QuackThree"
    }

    should("track changes") {
        val file = File(getPathForTest("changes") + "/arara.yaml")
        if (file.exists()) file.delete()
        outputForTest("changes") shouldContain "QuackOne"
        outputForTest("changes") shouldNotContain "QuackOne"
    }

    should("gracefully halt on halt rule") {
        val output = outputForTest("halt")
        output shouldContain "QuackOne"
        output shouldNotContain "QuackTwo"
    }
    should("forcefully halt on halt error rule") {
        val output = outputForTest("halt-error")
        output shouldContain "QuackOne"
        output shouldNotContain "QuackTwo"
    }

    should("fail on invalid config") {
        val exception = shouldThrow<AraraException> {
            outputForTest("invalid-config")
        }
        exception.message shouldContain "could not parse the configuration"
    }

    // TODO: uncomment tests (but not here because they do not test the
    // linear executor but FileSearching)
//    should("read foreign extension") {
//        val output = outputForTest("foreign-extension", "foreign-extension.my")
//        output shouldContain "QuackOne"
//    }
//    should("fail on unknown extension") {
//        shouldThrow<AraraException> {
//            outputForTest("foreign-extension", "foreign-extension.xy")
//        }
//    }

    should("accept empty pattern on known extension") {
        val output = outputForTest("known-extension")
        output shouldContain "QuackOne"
    }

    should("execute option-less directives") {
        val output = outputForTest("simple-directive")
        output shouldContain "The echoer"
        output shouldContain "SUCCESS"
    }
    should("execute directive with options") {
        val output = outputForTest("directive-with-options")
        output shouldContain "The echoer"
        output shouldContain "batchmode"
        output shouldContain "SUCCESS"
        output shouldNotContain "FAILURE"
    }

    should("interpret orb tags in directives") {
        val output = outputForTest("directive-with-mvel-options")
        output shouldContain "testdirective-with-mvel-optionsquack"
        output shouldContain "nothing"
    }
    should("not interpret orb tags in directives in safe mode") {
        val options = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions.from(options)
                .copy(executionMode = ExecutionMode.SAFE_RUN)
        val output = outputForTest("directive-with-mvel-options")
        output shouldNotContain "testdirective-with-mvel-optionsquack"
        output shouldContain "test@{getBasename(getOriginalReference())}quack"
        output shouldContain "nothing"
        LinearExecutor.executionOptions = options
    }

    should("process multiple files (files array)") {
        val output = outputForTest("multiple-files")
        output shouldContain "doc1.tex"
        output shouldContain "doc2.tex"
        output shouldNotContain "doc3.tex"
    }

    should("identify types correctly") {
        val output = outputForTest("typetests")
        output shouldContain "Bool test success"
        output shouldContain "String test success"
        output shouldContain "List test success"
        output shouldContain "Map test success"
    }
})
