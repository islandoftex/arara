// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.win32.StdCallLibrary

/** Queries borrowed standard handles without changing console modes, code pages or handle ownership. */
internal object WindowsTerminal {
    fun pipeName(descriptor: Int): String? = try {
        val kernel = Native.load("kernel32", Kernel32::class.java)
        pipeName(descriptor, kernel) { Native.load("ntdll", NtDll::class.java) }
    } catch (_: LinkageError) {
        null
    } catch (_: RuntimeException) {
        null
    }

    internal fun pipeName(descriptor: Int, kernel: Kernel32, nt: () -> NtDll): String? {
        val handle = kernel.GetStdHandle(descriptor)
        return if (handle == null || kernel.GetFileType(handle) != FILE_TYPE_PIPE) null else queryName(handle, nt())
    }

    private fun queryName(handle: Pointer, nt: NtDll): String? = Memory(NAME_BUFFER_SIZE.toLong()).use { name ->
        Memory((Native.POINTER_SIZE * 2).toLong()).use { status ->
            val result = nt.NtQueryInformationFile(
                handle, status, name, NAME_BUFFER_SIZE, FILE_NAME_INFORMATION
            )
            if (result != 0) null else decodeName(name)
        }
    }

    internal fun decodeName(name: Pointer): String? {
        val length = name.getInt(0)
        val valid = length in 1..(NAME_BUFFER_SIZE - Int.SIZE_BYTES) && length % Char.SIZE_BYTES == 0
        return if (valid) String(name.getByteArray(Int.SIZE_BYTES.toLong(), length), Charsets.UTF_16LE) else null
    }

    // JNA resolves these names against the native Windows exports.
    @Suppress("FunctionName")
    internal interface Kernel32 : StdCallLibrary {
        fun GetStdHandle(descriptor: Int): Pointer?
        fun GetFileType(handle: Pointer): Int
    }

    // JNA resolves these names against the native Windows exports.
    @Suppress("FunctionName")
    internal interface NtDll : StdCallLibrary {
        fun NtQueryInformationFile(handle: Pointer, status: Pointer, information: Pointer,
            length: Int, informationClass: Int): Int
    }

    private const val FILE_TYPE_PIPE = 3
    private const val FILE_NAME_INFORMATION = 9
    private const val NAME_BUFFER_SIZE = 65536
}
