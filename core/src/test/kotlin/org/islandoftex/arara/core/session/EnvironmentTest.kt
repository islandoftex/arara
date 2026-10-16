// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * Verifies the operating system interaction: system property access,
 * path lookups and command execution. The command execution scenarios
 * are skipped on Windows via JUnit assumptions.
 */
class EnvironmentTest {
    @Test
    fun `returns null on a non-existent system variable`() {
        assertNull(
            Environment.getSystemPropertyOrNull("asdf"),
            "an unknown system property resolves to null"
        )
    }

    @Test
    fun `holds a fallback value on a non-existent system variable`() {
        assertEquals(
            "fallback",
            Environment.getSystemProperty("asdf", "fallback"),
            "the fallback is used for an unknown system property"
        )
    }

    @Test
    fun `has a non-null value for an existent variable`() {
        assertTrue(
            listOf("/", "\\").contains(
                Environment.getSystemPropertyOrNull("file.separator")
            ),
            "the known file separator property resolves"
        )
    }

    @Test
    fun `has a non-fallback value for an existent variable`() {
        assertTrue(
            listOf("/", "\\").contains(
                Environment.getSystemProperty("file.separator", "fallback")
            ),
            "the known file separator property does not use the fallback"
        )
    }

    @Test
    fun `finds system utilities`() {
        // echo is a shell built-in on Windows with no executable of
        // its own on the system path, so the lookup uses the command
        // interpreter there, which always sits on the path
        val utility =
            if (Environment.checkOS(Environment.SupportedOS.WINDOWS)) "cmd"
            else "echo"
        assertTrue(
            Environment.isOnPath(utility),
            "the $utility utility is on the system path"
        )
    }

    @Test
    fun `does not find utilities with a fantasy name`() {
        // hopefully no one will have such a command in the path…
        assertFalse(
            Environment.isOnPath("echoQuackForArara"),
            "a made-up command is not on the system path"
        )
    }

    @Test
    fun `runs a successful command`() {
        assumeTrue(
            !Environment.checkOS(Environment.SupportedOS.WINDOWS),
            "the test requires a non-Windows operating system"
        )
        val result = assertIs<SystemCommandResult.Completed>(
            Environment.executeSystemCommand(Command(listOf("true"))),
            "a successful command completes"
        )
        assertEquals(0, result.exitCode, "exit code of the true command")
        assertEquals("", result.output, "the true command produces no output")
    }

    @Test
    fun `errors with exit code one`() {
        assumeTrue(
            !Environment.checkOS(Environment.SupportedOS.WINDOWS),
            "the test requires a non-Windows operating system"
        )
        val result = assertIs<SystemCommandResult.Completed>(
            Environment.executeSystemCommand(Command(listOf("false"))),
            "an unsuccessful command still completes"
        )
        assertEquals(1, result.exitCode, "exit code of the false command")
        assertEquals("", result.output, "the false command produces no output")
    }

    @Test
    fun `errors with a timeout exception`() {
        assumeTrue(
            !Environment.checkOS(Environment.SupportedOS.WINDOWS),
            "the test requires a non-Windows operating system"
        )
        val result = Environment.executeSystemCommand(
            if (Environment.checkOS(Environment.SupportedOS.MACOS)) {
                Command(listOf("sleep", "1"))
            } else {
                Command(listOf("sleep", "1s"))
            },
            true,
            500.milliseconds
        )
        val failure = assertIs<SystemCommandResult.Failed>(
            result,
            "a command exceeding the timeout fails"
        )
        assertIs<TimeoutException>(
            failure.exception,
            "the failure carries the underlying timeout"
        )
    }

    @Test
    fun `a failed command keeps the output it printed`() {
        assumeTrue(
            !Environment.checkOS(Environment.SupportedOS.WINDOWS),
            "the test requires a non-Windows operating system"
        )
        val result = Environment.executeSystemCommand(
            Command(listOf("sh", "-c", "echo quack-fail; sleep 1")),
            true,
            500.milliseconds
        )
        val failure = assertIs<SystemCommandResult.Failed>(
            result,
            "a command exceeding the timeout fails"
        )
        assertIs<TimeoutException>(
            failure.exception,
            "the failure carries the underlying timeout"
        )
        assertTrue(
            "quack-fail" in failure.output,
            "the output printed before the failure is kept"
        )
    }
}
