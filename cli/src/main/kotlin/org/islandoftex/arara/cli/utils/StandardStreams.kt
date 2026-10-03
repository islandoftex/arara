// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * Adapts the standard streams for the terminal the run speaks
 * to. The JVM encodes them with the platform's legacy codepage
 * on Windows, so a pty expecting UTF-8 receives the question
 * mark substitution for every glyph the codepage cannot carry -
 * the check, the cross, the braille frames - while the same
 * bytes render everywhere else. The Unix ecosystems on Windows
 * - the Cygwin and MSYS families - standardized on UTF-8 and
 * mark themselves with a terminal claim in the environment, so
 * the adaptation keys on that signature alone: a Windows pipe
 * behind a terminal claim. The console-attached streams keep
 * the codepage the console reads, and every other platform
 * never enters.
 *
 * @author Island of TeX
 */
object StandardStreams {
    /**
     * Whether the standard streams should speak UTF-8: the
     * signature of a Unix-style terminal claim mediating a
     * Windows pipe. All three conditions must hold - the
     * platform, the claim, the pipe - and the failure of any
     * one leaves the streams exactly as the JVM built them.
     *
     * @param osName The operating system name, as the JVM
     * reports it.
     * @param hasPty Whether the environment carries a Unix-style
     * terminal claim.
     * @param hasConsole Whether the standard streams attach to
     * an interactive console.
     * @return Whether the streams should speak UTF-8.
     */
    internal fun shouldSpeakUTF8(
        osName: String,
        hasPty: Boolean,
        hasConsole: Boolean
    ): Boolean =
        osName.lowercase().contains("win") && hasPty && !hasConsole

    /**
     * Adapts the standard streams for the terminal the run
     * speaks to: UTF-8 behind a Windows pty claim, untouched
     * everywhere else. The call belongs before anything prints,
     * so every downstream writer - the terminal library, the
     * help, the renderers - sees the adapted streams alone.
     */
    fun adaptForTerminal() {
        if (shouldSpeakUTF8(
                System.getProperty("os.name"),
                System.getenv("TERM") != null,
                System.console() != null
            )
        ) {
            System.setOut(utf8Stream(FileDescriptor.out))
            System.setErr(utf8Stream(FileDescriptor.err))
        }
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
