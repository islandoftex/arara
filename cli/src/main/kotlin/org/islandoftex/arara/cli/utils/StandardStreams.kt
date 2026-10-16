// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.PrintStream

/** Encoding adaptation belongs to the standalone entry point, never to embedded CLI invocations. */
internal object StandardStreams {
    fun <T> withTerminalEncoding(
        osName: String = System.getProperty("os.name"),
        pipeName: (Int) -> String? = WindowsTerminal::pipeName,
        action: () -> T
    ): T {
        if (!osName.startsWith("Windows", ignoreCase = true)) return action()
        val output = System.out
        val error = System.err
        val adaptedOutput = adapt(output, pipeName(STD_OUTPUT_HANDLE))
        val adaptedError = adapt(error, pipeName(STD_ERROR_HANDLE))
        if (adaptedOutput === output && adaptedError === error) return action()
        try {
            System.setOut(adaptedOutput)
            System.setErr(adaptedError)
            return action()
        } finally {
            adaptedOutput.flush()
            adaptedError.flush()
            System.setOut(output)
            System.setErr(error)
        }
    }

    private fun adapt(stream: PrintStream, pipeName: String?): PrintStream =
        if (pipeName != null && isTerminalPipe(pipeName) && stream.charset() != Charsets.UTF_8) {
            // PrintStream forwards byte writes unchanged. Do not reopen descriptors or close host streams.
            object : PrintStream(stream, true, Charsets.UTF_8) {
                override fun close() = flush()
            }
        } else stream

    internal fun isTerminalPipe(name: String): Boolean =
        Regex("""\\(?:Device\\NamedPipe\\)?(?:msys|cygwin)-[0-9a-fA-F]+-pty[0-9]+-(?:from|to)-master""")
            .matches(name)

    private const val STD_OUTPUT_HANDLE = -11
    private const val STD_ERROR_HANDLE = -12
}
