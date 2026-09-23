// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationUtils
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.cli.ruleset.RuleEngine
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Executes the end-to-end pipeline on the Lua rule engine, mirroring the
 * wiring of [ExecutionTest] (which runs on the default YAML engine): the
 * rule lookup, parsing and command evaluation all have to happen through
 * the Lua implementations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LuaExecutionTest {
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

        // run the whole spec on the Lua engine; the default (YAML) engine
        // is restored afterwards so the engine switch cannot leak
        RuleEngine.current = RuleEngine.LUA
    }

    @AfterAll
    fun tearDown() {
        RuleEngine.current = RuleEngine.YAML
    }

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
            val workingDirectory = MPPPath(
                "src/test/resources/executiontests/$testName"
            )
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
    fun `executes a Lua rule end to end on the Lua engine`() {
        val output = outputForTest("lua-engine")
        assertTrue(output.contains("The echoer"), "the rule entry is displayed")
        assertTrue(output.contains("LuaEngineWorks"), "the echoed marker")
        assertTrue(output.contains("SUCCESS"), "the entry result")
        assertFalse(output.contains("FAILURE"), "no failure is reported")
    }

    @Test
    fun `evaluates Lua directive conditionals on the Lua engine`() {
        val output = outputForTest("lua-engine-conditionals")
        // unconditional and truthy conditions run...
        assertTrue(output.contains("One"), "the unconditional directive ran")
        assertTrue(output.contains("Three"), "the negated condition ran")
        assertTrue(output.contains("Four"), "the compound condition ran")
        // ...and the falsy one is skipped
        assertFalse(
            output.contains("Two"),
            "the directive with a falsy condition is skipped"
        )
    }

    @Test
    fun `rejects MVEL conditional syntax on the Lua engine`() {
        // '&&' does not parse in the Lua dialect; the failure has to
        // surface as an arara error instead of crashing the run
        val exception = assertFailsWith<AraraException>(
            "MVEL conditional syntax must not parse in the Lua dialect"
        ) {
            outputForTest("lua-engine-conditionals", "broken.tex")
        }
        assertTrue(
            exception.message != null,
            "the rejection carries a diagnostic message"
        )
    }

    @Test
    fun `interpolates Lua orb tags in directive options`() {
        val output = outputForTest("lua-engine-options")
        // the orb tag is evaluated at parse time, so the rule receives
        // the interpolated list containing the base name of the file
        assertTrue(
            output.contains("name: lua-engine-options"),
            "the orb tag was interpolated with the file base name"
        )
        assertTrue(output.contains("SUCCESS"), "the entry result")
    }

    @Test
    fun `does not interpolate orb tags in safe mode on the Lua engine`() {
        val options = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions.from(options)
            .copy(executionMode = ExecutionMode.SAFE_RUN)
        try {
            val output = outputForTest("lua-engine-options")
            // safe mode must never evaluate user expressions, so the
            // orb tag passes through literally
            assertTrue(
                output.contains("@{getBasename(getOriginalReference())}"),
                "the orb tag passes through literally in safe mode"
            )
            assertFalse(
                output.contains("name: lua-engine-options"),
                "the orb tag was not interpolated in safe mode"
            )
        } finally {
            LinearExecutor.executionOptions = options
        }
    }

    @Test
    fun `uses the session from Lua rules end to end`() {
        try {
            val output = outputForTest("lua-engine-sessions")
            assertTrue(
                output.contains("Quack"),
                "the first command stores the session value"
            )
            assertTrue(
                output.contains("MeowQuack"),
                "the second command reads the stored value"
            )
            assertFalse(output.contains("FAILURE"), "no failure is reported")
        } finally {
            Session.remove("quack")
        }
    }

    @Test
    fun `reports wrongly typed conditional helper calls end to end`() {
        val exception = assertFailsWith<AraraException>(
            "a wrongly typed helper argument must fail the run"
        ) {
            outputForTest("lua-engine-conditionals", "badtype.tex")
        }
        assertTrue(
            exception.message?.contains("could not compile the expression") == true,
            "the primary message is the localized evaluation error"
        )
        val chain = generateSequence<Throwable>(exception) { it.cause }
            .mapNotNull { it.message }
            .joinToString(" ")
        assertTrue(
            chain.contains("bad argument #1 to 'exists'"),
            "the bad argument detail is available in the cause chain"
        )
    }
}
