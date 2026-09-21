// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

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
        assertTrue(
            Environment.isOnPath("echo"),
            "the echo utility is on the system path"
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
        val (exit, output) = Environment.executeSystemCommand(Command(listOf("true")))
        assertEquals(0, exit, "exit code of the true command")
        assertEquals("", output, "the true command produces no output")
    }

    @Test
    fun `errors with exit code one`() {
        assumeTrue(
            !Environment.checkOS(Environment.SupportedOS.WINDOWS),
            "the test requires a non-Windows operating system"
        )
        val (exit, output) = Environment.executeSystemCommand(Command(listOf("false")))
        assertEquals(1, exit, "exit code of the false command")
        assertEquals("", output, "the false command produces no output")
    }

    @Test
    fun `errors with a timeout exception`() {
        assumeTrue(
            !Environment.checkOS(Environment.SupportedOS.WINDOWS),
            "the test requires a non-Windows operating system"
        )
        val (exit, output) = Environment.executeSystemCommand(
            if (Environment.checkOS(Environment.SupportedOS.MACOS)) {
                Command(listOf("sleep", "1"))
            } else {
                Command(listOf("sleep", "1s"))
            },
            true,
            500.milliseconds
        )
        assertEquals(
            Environment.ERROR_EXIT_STATUS, exit,
            "exit code reporting a command execution failure"
        )
        assertTrue(
            output.contains(TimeoutException::class.java.name),
            "the timeout failure names the underlying exception"
        )
    }
}
