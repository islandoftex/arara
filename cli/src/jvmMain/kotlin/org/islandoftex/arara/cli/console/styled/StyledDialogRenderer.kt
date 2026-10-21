// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.cli.ui.TerminalDialogRenderer
import java.io.BufferedReader
import java.io.PrintWriter

/** Styled frames retain the terminal dialog's input validation and stream ownership. */
class StyledDialogRenderer(
    private val terminal: Terminal,
    input: BufferedReader = System.`in`.bufferedReader(),
    output: PrintWriter = PrintWriter(System.out, true)
) : TerminalDialogRenderer(input, output, { terminal.size.width.coerceAtLeast(1) }) {
    override fun block(type: Int, title: String, text: String) {
        val rule = if (terminal.terminalInfo.ansiLevel == AnsiLevel.NONE) "-" else "─"
        val titleLine = heading(type, title)
        val heading = when (type) {
            ERROR -> terminal.theme.danger(titleLine)
            WARNING -> terminal.theme.warning(titleLine)
            else -> terminal.theme.info(titleLine)
        }
        line(rule.repeat(width()))
        output.println(if (terminal.terminalInfo.ansiLevel == AnsiLevel.NONE) titleLine else heading)
        output.flush()
        line(text)
        line(rule.repeat(width()))
    }

    private companion object {
        const val ERROR = 1
        const val WARNING = 3
    }
}
