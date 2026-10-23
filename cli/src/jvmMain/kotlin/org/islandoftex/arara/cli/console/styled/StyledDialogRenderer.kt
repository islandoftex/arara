// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.cli.ui.TerminalDialogRenderer
import org.islandoftex.arara.core.localization.LanguageController
import java.io.BufferedReader
import java.io.PrintWriter

/** Decorates dialogs while retaining shared input validation and host-owned streams. */
class StyledDialogRenderer(
    private val terminal: Terminal,
    input: BufferedReader = System.`in`.bufferedReader(),
    output: PrintWriter = PrintWriter(System.out, true),
    beforeDialog: () -> Unit = {}
) : TerminalDialogRenderer(input, output, { terminal.size.width.coerceAtLeast(1) }, beforeDialog) {
    private val layout = StyledLayout(terminal)
    private val decorated = terminal.terminalInfo.ansiLevel != AnsiLevel.NONE &&
        System.out.charset().newEncoder().canEncode("╭─│╰")

    override fun block(type: Int, title: String, text: String) {
        if (boxed) panel(type, title, text, emptyArray()) else super.block(type, title, text)
    }

    override fun selectionBlock(type: Int, title: String, text: String, entries: Array<out Any>) {
        if (boxed) panel(type, title, text, entries) else super.selectionBlock(type, title, text, entries)
    }

    override fun prompt(range: String) {
        if (!decorated) {
            super.prompt(range)
        } else {
            line("${LanguageController.messages.infoLabelDialogPrompt} (${TextStyle(bold = true)(range)}):")
            output.print("> ")
            output.flush()
        }
    }

    override fun line(text: String) {
        if (!decorated) super.line(text) else {
            output.println(layout.render(text, dialogWidth))
            output.flush()
        }
    }

    private val dialogWidth: Int get() = width().coerceAtMost(StyledTheme.proseMaximumWidth)
    private val boxed: Boolean get() = decorated && dialogWidth >= MINIMUM_BOX_WIDTH

    private fun panel(type: Int, title: String, text: String, entries: Array<out Any>) {
        beforeDialog()
        val style = style(type)
        val inner = dialogWidth - BOX_CHROME
        output.println(style("╭" + "─".repeat(dialogWidth - BORDER_COLUMNS) + "╮"))
        val glyph = when (type) {
            ERROR -> StyledTheme.failureGlyph + " "
            INFORMATION -> "ℹ "
            WARNING -> "⚠ "
            QUESTION -> "? "
            else -> ""
        }
        val header = (style + TextStyle(bold = true))(glyph + heading(type, title))
        val content = mutableListOf(layout.render(header, inner))
        content.add("")
        content.add(layout.render(text, inner))
        if (entries.isNotEmpty()) content.add("")
        entries.forEachIndexed { index, value ->
            val label = "${index + 1}. "
            if (inner - label.length < StyledTheme.minimumValueWidth) {
                content.add(layout.render(terminal.theme.info(label), inner))
                content.add(layout.render(value.toString(), inner))
            } else {
                val wrapped = layout.render(value.toString(), inner - label.length).lines()
                content.add(wrapped.mapIndexed { line, part ->
                    (if (line == 0) terminal.theme.info(label) else " ".repeat(label.length)) + part
                }.joinToString("\n"))
            }
        }
        content.flatMap(String::lines).forEach { row ->
            output.println(style("│") + " " + row + " ".repeat((inner - layout.cells(row)).coerceAtLeast(0)) +
                " " + style("│"))
        }
        output.println(style("╰" + "─".repeat(dialogWidth - BORDER_COLUMNS) + "╯"))
        output.flush()
    }

    private fun style(type: Int): TextStyle = when (type) {
        ERROR -> terminal.theme.danger
        INFORMATION -> terminal.theme.info
        WARNING -> terminal.theme.warning
        QUESTION -> StyledTheme.logo
        else -> terminal.theme.muted
    }

    private companion object {
        const val ERROR = 1
        const val INFORMATION = 2
        const val WARNING = 3
        const val QUESTION = 4
        const val MINIMUM_BOX_WIDTH = 8
        const val BOX_CHROME = 4
        const val BORDER_COLUMNS = 2
    }
}
