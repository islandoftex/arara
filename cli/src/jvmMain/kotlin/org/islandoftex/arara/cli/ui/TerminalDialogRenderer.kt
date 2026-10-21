// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ui

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.DialogRenderer
import java.io.BufferedReader
import java.io.PrintWriter

/** Terminal dialogs use host-owned streams and the current configured column width. */
open class TerminalDialogRenderer(
    private val input: BufferedReader = System.`in`.bufferedReader(),
    protected val output: PrintWriter = PrintWriter(System.out, true),
    private val columns: () -> Int = { Session.userInterfaceOptions.terminalOutputWidth }
) : DialogRenderer {
    override fun showMessage(type: Int, title: String, text: String) {
        block(type, title, text)
    }

    override fun showInput(type: Int, title: String, text: String): String {
        block(type, title, text)
        output.print("> ")
        output.flush()
        return answer().trim()
    }

    override fun showOptions(type: Int, title: String, text: String, buttons: Array<out Any>): Int =
        selection(type, title, text, buttons)

    override fun showList(type: Int, title: String, text: String, elements: Array<out Any>): Int =
        selection(type, title, text, elements)

    private fun selection(type: Int, title: String, text: String, entries: Array<out Any>): Int {
        if (entries.isEmpty()) return 0
        block(type, title, text)
        entries.forEachIndexed { index, value -> line("${index + 1}. $value") }
        while (true) {
            line("Choose 1-${entries.size}, or 0 to cancel:")
            output.print("> ")
            output.flush()
            val chosen = answer().trim().toIntOrNull()
            if (chosen != null && chosen in 0..entries.size) return chosen
            line("Enter a number from 0 to ${entries.size}.")
        }
    }

    private fun answer(): String = input.readLine() ?: throw AraraException(
        "Interactive dialog input is closed; provide input or use rules without dialogs."
    )

    protected open fun block(type: Int, title: String, text: String) {
        val kinds = listOf("PLAIN", "ERROR", "INFORMATION", "WARNING", "QUESTION")
        val kind = kinds.getOrNull(type) ?: kinds.first()
        line("-".repeat(width()))
        line("[$kind] $title")
        line(text)
        line("-".repeat(width()))
    }

    protected fun width(): Int = columns().coerceAtLeast(1)

    protected fun line(text: String) {
        for (original in text.lines()) {
            var remainder = original
            while (remainder.length > width()) {
                val space = remainder.lastIndexOf(' ', width())
                val end = if (space > 0) space else width()
                output.println(remainder.substring(0, end))
                remainder = remainder.substring(end).trimStart()
            }
            output.println(remainder)
        }
        output.flush()
    }
}
