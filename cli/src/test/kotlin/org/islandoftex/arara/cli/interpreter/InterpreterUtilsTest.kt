// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.ruleset.RuleFormat
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the interpreter utilities: which conditionals deserve a
 * prior evaluation per execution mode, how system command failures
 * map to the localized errors, and how rule paths resolve in both
 * the absolute and the relative shape.
 */
class InterpreterUtilsTest {
    private val originalOptions = LinearExecutor.executionOptions

    private fun conditional(type: DirectiveConditionalType): DirectiveConditional =
        object : DirectiveConditional {
            override val type = type
            override val condition = "found('log', 'x')"
        }

    @Test
    fun `evaluates the priors per conditional type`() {
        LinearExecutor.executionOptions = ExecutionOptions.from(originalOptions)
            .copy(executionMode = ExecutionMode.DRY_RUN)
        try {
            assertFalse(
                InterpreterUtils.runPriorEvaluation(conditional(DirectiveConditionalType.IF)),
                "dry runs skip the prior evaluation"
            )
        } finally {
            LinearExecutor.executionOptions = originalOptions
        }
        assertTrue(InterpreterUtils.runPriorEvaluation(conditional(DirectiveConditionalType.IF)))
        assertTrue(InterpreterUtils.runPriorEvaluation(conditional(DirectiveConditionalType.WHILE)))
        assertTrue(InterpreterUtils.runPriorEvaluation(conditional(DirectiveConditionalType.UNLESS)))
        assertFalse(InterpreterUtils.runPriorEvaluation(conditional(DirectiveConditionalType.UNTIL)))
        assertFalse(InterpreterUtils.runPriorEvaluation(conditional(DirectiveConditionalType.NONE)))
    }

    @Test
    fun `runs commands and reports their outcome`() {
        val directory = Files.createTempDirectory("arara-interpreter")
        val java = ProcessHandle.current().info().command().orElse("java")
        assertEquals(
            0,
            InterpreterUtils.run(
                RuleMethods.getCommandWithWorkingDirectory(directory.toString(), java, "-version")
            ),
            "a successful command reports its exit code"
        )
        assertEquals(
            1,
            InterpreterUtils.run(
                RuleMethods.getCommandWithWorkingDirectory(
                    directory.toString(), java, "-wrongflag-quack"
                )
            ),
            "a failing command reports its exit code"
        )
        assertFailsWith<AraraException> {
            InterpreterUtils.run(
                RuleMethods.getCommandWithWorkingDirectory(
                    directory.toString(), "arara-not-a-real-command-quack"
                )
            )
        }
    }

    @Test
    fun `maps command timeouts to the localized error`() {
        val directory = Files.createTempDirectory("arara-interpreter-timeout")
        LinearExecutor.executionOptions = ExecutionOptions.from(originalOptions)
            .copy(timeoutValue = 5.milliseconds)
        try {
            val java = ProcessHandle.current().info().command().orElse("java")
            assertFailsWith<AraraException> {
                InterpreterUtils.run(
                    RuleMethods.getCommandWithWorkingDirectory(directory.toString(), java)
                )
            }
        } finally {
            LinearExecutor.executionOptions = originalOptions
        }
    }

    @Test
    fun `constructs rule paths in both shapes`() {
        val workingDirectory = Files.createTempDirectory("arara-rule-paths")
        val relative = InterpreterUtils.construct(
            MPPPath("rules"), "pdflatex", RuleFormat.LUA,
            MPPPath(workingDirectory.toString())
        )
        assertTrue(
            relative.toString().endsWith("pdflatex.lua"),
            "relative rule paths resolve under the working directory"
        )
        val absolute = InterpreterUtils.construct(
            MPPPath(workingDirectory.toString()), "pdflatex", RuleFormat.LUA,
            MPPPath(workingDirectory.toString())
        )
        assertTrue(
            absolute.toString().endsWith("pdflatex.lua"),
            "absolute rule paths resolve on their own"
        )
    }
}
