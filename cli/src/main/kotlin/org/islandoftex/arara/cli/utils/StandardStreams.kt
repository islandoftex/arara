// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.sun.jna.Library
import com.sun.jna.Native
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * Adapts the standard streams for the terminal the run speaks
 * to. The JVM encodes them with the platform's legacy codepage
 * on Windows, so a terminal expecting UTF-8 receives the
 * question mark substitution for every glyph the codepage cannot
 * carry - the check, the cross, the braille frames - while the
 * same bytes render everywhere else. The Unix ecosystems on
 * Windows - the Cygwin and MSYS families - standardized on
 * UTF-8 and mark themselves with a terminal claim in the
 * environment, so the adaptation keys on that signature alone:
 * a Windows stream behind a terminal claim. A pipe keeps the
 * pure rewrap; a console - the modern mintty bridges native
 * programs onto one - additionally borrows the console's
 * output codepage and returns it at exit, because console
 * state outlives the process. Every refusal leaves the streams
 * exactly as the JVM built them.
 */
object StandardStreams {
    /**
     * The output codepage a UTF-8 stream needs: the platform's
     * own UTF-8 identifier, the codepage every modern Windows
     * terminal speaks natively.
     */
    private const val UTF8_CODEPAGE = 65001

    /**
     * Whether the standard streams should speak UTF-8: the
     * signature of a Unix-style terminal claim mediating a
     * Windows stream, pipe or console alike. Both conditions
     * must hold, and the failure of either leaves the streams
     * exactly as the JVM built them.
     *
     * @param osName The operating system name, as the JVM
     * reports it.
     * @param hasPty Whether the environment carries a Unix-style
     * terminal claim.
     * @return Whether the streams should speak UTF-8.
     */
    internal fun shouldSpeakUTF8(osName: String, hasPty: Boolean): Boolean =
        osName.lowercase().contains("win") && hasPty

    /**
     * Adapts the standard streams for the terminal the run
     * speaks to: UTF-8 behind a Windows terminal claim,
     * untouched everywhere else. The call belongs before
     * anything prints, so every downstream writer - the
     * terminal library, the help, the renderers - sees the
     * adapted streams alone. A console-attached stream borrows
     * the console's output codepage first; the borrow is
     * returned by a shutdown hook, and a refused borrow - any
     * platform refusal, any call failure - changes nothing at
     * all: the failure mode of the adaptation is the status
     * quo, never a broken startup.
     */
    fun adaptForTerminal() {
        if (!shouldSpeakUTF8(
                System.getProperty("os.name"),
                System.getenv("TERM") != null
            )
        ) {
            return
        }
        if (System.console() != null) {
            val previousCodepage = borrowConsoleCodepage() ?: return
            Runtime.getRuntime().addShutdownHook(
                Thread { restoreConsoleCodepage(previousCodepage) }
            )
        }
        System.setOut(utf8Stream(FileDescriptor.out))
        System.setErr(utf8Stream(FileDescriptor.err))
    }

    /**
     * Borrows the console's output codepage for the UTF-8 one.
     *
     * @return The codepage the console read before the borrow,
     * for the exit to return, or null when the platform
     * refuses - and nothing has changed.
     */
    private fun borrowConsoleCodepage(): Int? =
        runCatching {
            val kernel = Native.load("kernel32", Kernel32::class.java)
            val previous = kernel.GetConsoleOutputCP()
            if (previous == 0 || kernel.SetConsoleOutputCP(UTF8_CODEPAGE) == 0) {
                null
            } else {
                previous
            }
        }.getOrNull()

    /**
     * Returns the console's output codepage at the end of the
     * run. The return is best effort: a hard kill skips the
     * hook and leaves the console reading UTF-8, which is the
     * direction the platform itself is taking.
     *
     * @param codepage The codepage the console read before the
     * borrow.
     */
    private fun restoreConsoleCodepage(codepage: Int) {
        runCatching {
            Native.load("kernel32", Kernel32::class.java)
                .SetConsoleOutputCP(codepage)
        }
    }

    /**
     * The kernel's console codepage surface, reached through
     * the native access library the terminal detection already
     * rides.
     */
    @Suppress("FunctionName")
    private interface Kernel32 : Library {
        /**
         * Queries the output codepage the console reads.
         *
         * @return The codepage, or zero when the query fails.
         */
        fun GetConsoleOutputCP(): Int

        /**
         * Sets the output codepage the console reads.
         *
         * @param codepage The codepage to set.
         * @return One on success, zero on failure.
         */
        fun SetConsoleOutputCP(codepage: Int): Int
    }

    /**
     * Builds the UTF-8 stream over the descriptor, flushing on
     * newlines the way the standard streams always have.
     *
     * @param descriptor The descriptor the stream writes to.
     * @return The UTF-8 print stream.
     */
    private fun utf8Stream(descriptor: FileDescriptor): PrintStream =
        PrintStream(FileOutputStream(descriptor), true, Charsets.UTF_8)
}
