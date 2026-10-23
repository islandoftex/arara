// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class StandardStreamsTest {
    @Test
    fun terminalPipeNamesAreSpecific() {
        listOf("\\msys-123abc-pty0-to-master", "\\cygwin-123abc-pty12-from-master",
            "\\Device\\NamedPipe\\msys-123abc-pty1-to-master").forEach {
            assertTrue(StandardStreams.isTerminalPipe(it), it)
        }
        listOf("", "\\ordinary-pipe", "\\msys-123-pty-to-master", "\\msys-123-pty0-to-master-extra",
            "\\msys-123-pty0-to-master.txt", "C:\\msys-123-pty0-to-master").forEach {
            assertFalse(StandardStreams.isTerminalPipe(it), it)
        }
    }

    @Test
    fun nonWindowsHostsNeverLoadNativeWindowsCode() {
        listOf("Linux", "Darwin", "Mac OS X").forEach { os ->
            val output = System.out
            assertEquals(42, StandardStreams.withTerminalEncoding(os, { error("Native probe on $os") }) {
                assertSame(output, System.out)
                42
            })
        }
    }

    @Test
    fun adaptsEachTerminalStreamIndependentlyAndRestoresOnFailure() {
        val originalOutput = System.out
        val originalError = System.err
        val bytes = ByteArrayOutputStream()
        val output = PrintStream(bytes, true, Charsets.US_ASCII)
        val error = PrintStream(ByteArrayOutputStream(), true, Charsets.US_ASCII)
        try {
            System.setOut(output)
            System.setErr(error)
            assertFailsWith<IllegalStateException> {
                StandardStreams.withTerminalEncoding("Windows 11", {
                    if (it == -11) "\\msys-123abc-pty0-to-master" else "\\ordinary-pipe"
                }) {
                    assertEquals(Charsets.UTF_8, System.out.charset())
                    assertSame(error, System.err)
                    System.out.print("ação λ 🦜")
                    System.out.close()
                    error("failure")
                }
            }
            assertSame(output, System.out)
            assertSame(error, System.err)
            assertEquals("ação λ 🦜", bytes.toString(Charsets.UTF_8))
            output.print(" still open")
            assertTrue(bytes.toString(Charsets.UTF_8).endsWith(" still open"))
        } finally {
            System.setOut(originalOutput)
            System.setErr(originalError)
        }
    }

    @Test
    fun keepsNativeConsoleRedirectedAndUtf8Streams() {
        val originalOutput = System.out
        val originalError = System.err
        try {
            val output = PrintStream(ByteArrayOutputStream(), true, Charsets.UTF_8)
            val error = PrintStream(ByteArrayOutputStream(), true, Charsets.US_ASCII)
            System.setOut(output)
            System.setErr(error)
            StandardStreams.withTerminalEncoding("Windows 11", { null }) {
                assertSame(output, System.out)
                assertSame(error, System.err)
            }
            StandardStreams.withTerminalEncoding("Windows 11", { "\\cygwin-123-pty0-to-master" }) {
                assertSame(output, System.out)
                assertEquals(Charsets.UTF_8, System.err.charset())
            }
            assertSame(error, System.err)
        } finally {
            System.setOut(originalOutput)
            System.setErr(originalError)
        }
    }
}
