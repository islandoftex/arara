// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Isolate
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationUtils
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.cli.ruleset.RuleEngine
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Executes the end-to-end pipeline on the Lua rule engine, mirroring the
 * wiring of [ExecutionTest] (which runs on the default YAML engine): the
 * rule lookup, parsing and command evaluation all have to happen through
 * the Lua implementations.
 */
@Isolate
class LuaExecutionTest : ShouldSpec({
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
                it.printFileInformation()
            },
            processDirectives = { file, list ->
                DirectiveUtils.process(file, list)
            }
        )

        // run the whole spec on the Lua engine; the default (YAML) engine
        // is restored afterwards so the engine switch cannot leak
        RuleEngine.current = RuleEngine.LUA
    }
    afterSpec {
        RuleEngine.current = RuleEngine.YAML
    }

    fun getPathForTest(name: String): String =
        "src/test/resources/executiontests/$name"

    fun outputForTest(
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

    should("execute a Lua rule end-to-end on the Lua engine") {
        val output = outputForTest("lua-engine")
        output shouldContain "The echoer"
        output shouldContain "LuaEngineWorks"
        output shouldContain "SUCCESS"
        output shouldNotContain "FAILURE"
    }

    should("evaluate Lua directive conditionals on the Lua engine") {
        val output = outputForTest("lua-engine-conditionals")
        // unconditional and truthy conditions run...
        output shouldContain "One"
        output shouldContain "Three"
        output shouldContain "Four"
        // ...and the falsy one is skipped
        output shouldNotContain "Two"
    }

    should("reject MVEL conditional syntax on the Lua engine") {
        // '&&' does not parse in the Lua dialect; the failure has to
        // surface as an arara error instead of crashing the run
        val exception = shouldThrow<AraraException> {
            outputForTest("lua-engine-conditionals", "broken.tex")
        }
        exception.message shouldNotBe null
    }

    should("interpolate Lua orb tags in directive options") {
        val output = outputForTest("lua-engine-options")
        // the orb tag is evaluated at parse time, so the rule receives
        // the interpolated list containing the base name of the file
        output shouldContain "name: lua-engine-options"
        output shouldContain "SUCCESS"
    }

    should("not interpolate orb tags in safe mode on the Lua engine") {
        val options = LinearExecutor.executionOptions
        LinearExecutor.executionOptions =
            org.islandoftex.arara.core.configuration.ExecutionOptions
                .from(options)
                .copy(
                    executionMode =
                    org.islandoftex.arara.api.configuration.ExecutionMode
                        .SAFE_RUN
                )
        try {
            val output = outputForTest("lua-engine-options")
            // safe mode must never evaluate user expressions, so the
            // orb tag passes through literally
            output shouldContain "@{getBasename(getOriginalReference())}"
            output shouldNotContain "name: lua-engine-options"
        } finally {
            LinearExecutor.executionOptions = options
        }
    }
})
