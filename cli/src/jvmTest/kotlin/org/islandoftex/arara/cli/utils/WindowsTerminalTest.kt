// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.win32.StdCallLibrary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue

class WindowsTerminalTest {
    private class Handles(private val type: Int, private val handle: Pointer? = Pointer(1)) : WindowsTerminal.Kernel32 {
        override fun GetStdHandle(descriptor: Int): Pointer? = handle
        override fun GetFileType(handle: Pointer): Int = type
    }

    private class Names(private val name: String, private val status: Int = 0,
        private val byteCount: Int = name.toByteArray(Charsets.UTF_16LE).size) : WindowsTerminal.NtDll {
        override fun NtQueryInformationFile(handle: Pointer, status: Pointer, information: Pointer,
            length: Int, informationClass: Int): Int {
            assertEquals(9, informationClass)
            val bytes = name.toByteArray(Charsets.UTF_16LE)
            information.setInt(0, byteCount)
            information.write(4, bytes, 0, bytes.size)
            return this.status
        }
    }

    @Test
    fun onlyQueriesPipeHandles() {
        listOf(0, 1, 2).forEach { type ->
            assertNull(WindowsTerminal.pipeName(-11, Handles(type)) { error("Not a pipe") })
        }
        assertNull(WindowsTerminal.pipeName(-12, Handles(3, null)) { error("Missing handle") })
    }

    @Test
    fun decodesBoundedUtf16NamesAndRejectsFailedQueries() {
        val name = "\\msys-123abc-pty0-to-master"
        assertEquals(name, WindowsTerminal.pipeName(-11, Handles(3)) { Names(name) })
        assertNull(WindowsTerminal.pipeName(-11, Handles(3)) { Names(name, status = -1) })
        listOf(-1, 0, 1, 65534, Int.MAX_VALUE).forEach { length ->
            assertNull(WindowsTerminal.pipeName(-11, Handles(3)) { Names(name, byteCount = length) })
        }
    }

    @Test
    fun unavailableNativeHandlesLeaveStreamsUnchanged() {
        assertNull(WindowsTerminal.pipeName(Int.MIN_VALUE))
    }

    @Test
    fun queriesRealWindowsNamedPipe() {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"), "Requires Windows named pipes")
        val kernel = Native.load("kernel32", Pipes::class.java)
        val name = "msys-${ProcessHandle.current().pid().toString(16)}-pty0-to-master"
        // An outbound byte-mode pipe; this handle is owned by the test, not by the JVM.
        val pipe = kernel.CreateNamedPipeW(WString("\\\\.\\pipe\\$name"), 2, 0, 1, 4096, 4096, 0, null)
        assertTrue(Pointer.nativeValue(pipe) != -1L, "CreateNamedPipeW failed: ${Native.getLastError()}")
        try {
            val handles = object : WindowsTerminal.Kernel32 {
                override fun GetStdHandle(descriptor: Int): Pointer = pipe
                override fun GetFileType(handle: Pointer): Int = kernel.GetFileType(handle)
            }
            val actual = WindowsTerminal.pipeName(-11, handles) {
                Native.load("ntdll", WindowsTerminal.NtDll::class.java)
            }
            assertEquals("\\$name", actual)
            assertTrue(StandardStreams.isTerminalPipe(assertNotNull(actual)))
        } finally {
            assertTrue(kernel.CloseHandle(pipe) != 0)
        }
    }

    @Suppress("FunctionName")
    interface Pipes : StdCallLibrary {
        fun CreateNamedPipeW(name: WString, access: Int, mode: Int, instances: Int, outputSize: Int,
            inputSize: Int, timeout: Int, security: Pointer?): Pointer
        fun GetFileType(handle: Pointer): Int
        fun CloseHandle(handle: Pointer): Int
    }
}
