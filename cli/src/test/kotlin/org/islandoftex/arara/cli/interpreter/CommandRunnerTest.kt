// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the command runner: exit codes report through, failures map
 * to their localized errors, and the duration pairing survives.
 */
class CommandRunnerTest {
    private val originalOptions = LinearExecutor.executionOptions

    @Test
    fun `runs commands and reports their outcome`() {
        val directory = Files.createTempDirectory("arara-interpreter")
        val java = ProcessHandle.current().info().command().orElse("java")
        val success = CommandRunner.run(
            RuleMethods.getCommandWithWorkingDirectory(directory.toString(), java, "-version")
        )
        assertEquals(
            0,
            success.first,
            "a successful command reports its exit code"
        )
        val failure = CommandRunner.run(
            RuleMethods.getCommandWithWorkingDirectory(
                directory.toString(), java, "-wrongflag-quack"
            )
        )
        assertEquals(
            1,
            failure.first,
            "a failing command reports its exit code"
        )
        assertTrue(
            failure.second.isNotBlank(),
            "the failing command's captured output travels with the code"
        )
        assertFailsWith<AraraException> {
            CommandRunner.run(
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
                CommandRunner.run(
                    RuleMethods.getCommandWithWorkingDirectory(directory.toString(), java)
                )
            }
        } finally {
            LinearExecutor.executionOptions = originalOptions
        }
    }

    @Test
    fun `reports the missing command on the path with the IO error`() {
        // a rule calling a tool that does not exist is the classic
        // user-facing failure; the localized IO error carries the
        // hint about the command not being on the path. The command
        // is built directly because the getCommand helper is bound
        // to a live project, which a bare test does not have
        val original = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions()
        try {
            val exception = assertFailsWith<AraraException>(
                "a nonexistent command fails with the IO error"
            ) {
                CommandRunner.run(
                    org.islandoftex.arara.core.session.Command(
                        listOf("arara-command-that-does-not-exist-anywhere")
                    )
                )
            }
            assertTrue(
                exception.message?.contains("IO error") == true,
                "the localized IO error carries: ${exception.message}"
            )
        } finally {
            LinearExecutor.executionOptions = original
        }
    }
}
