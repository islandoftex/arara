// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS

class EnvironmentTest {
    @Test
    fun `return null on non-existent system variable`() {
        assertNull(Environment.getSystemPropertyOrNull("asdf"))
    }

    @Test
    fun `hold a fallback value on non-existent system variable`() {
        assertEquals("fallback", Environment.getSystemProperty("asdf", "fallback"))
    }

    @Test
    fun `have a non-null value for existent variable`() {
        assertContains(listOf("/", "\\"), Environment.getSystemPropertyOrNull("file.separator"))
    }

    @Test
    fun `have a non-fallback value for existent variable`() {
        assertContains(listOf("/", "\\"), Environment.getSystemProperty("file.separator", "fallback"))
    }

    @Test
    fun `find system utils`() {
        assertTrue(Environment.isOnPath("echo"))
    }

    @Test
    fun `not find utils with fantasy name`() {
        // hopefully no one will have such a command in the path…
        assertFalse(Environment.isOnPath("echoQuackForArara"))
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    fun `run successfully`() {
        val (exit, output) = Environment.executeSystemCommand(Command(listOf("true")))
        assertEquals(0, exit)
        assertEquals("", output)
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    fun `error with exit code 1`() {
        val (exit, output) = Environment.executeSystemCommand(Command(listOf("false")))
        assertEquals(1, exit)
        assertEquals("", output)
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    fun `error with timeout exception`() {
        val (exit, output) = Environment.executeSystemCommand(
            if (Environment.checkOS(Environment.SupportedOS.MACOS)) {
                Command(listOf("sleep", "1"))
            } else {
                Command(listOf("sleep", "1s"))
            },
            true,
            500.milliseconds
        )
        assertEquals(Environment.ERROR_EXIT_STATUS, exit)
        assertContains(output, TimeoutException::class.java.name)
    }
}
