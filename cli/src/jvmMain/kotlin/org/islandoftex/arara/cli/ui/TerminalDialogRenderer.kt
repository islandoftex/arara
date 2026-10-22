// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ui

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.ui.DialogRenderer
import java.io.BufferedReader
import java.io.PrintWriter

/** Terminal dialogs use host-owned streams and the current configured column width. */
open class TerminalDialogRenderer(
    private val input: BufferedReader = System.`in`.bufferedReader(),
    protected val output: PrintWriter = PrintWriter(System.out, true),
    private val columns: () -> Int = { Session.userInterfaceOptions.terminalOutputWidth },
    protected val beforeDialog: () -> Unit = {}
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
            val range = "0-${entries.size}"
            line("${LanguageController.messages.infoLabelDialogPrompt} ($range):")
            output.print("> ")
            output.flush()
            val raw = answer().trim()
            val chosen = raw.toIntOrNull()
            if (chosen != null && chosen in 0..entries.size) return chosen
        }
    }

    private fun answer(): String = input.readLine() ?: throw AraraException(
        LanguageController.messages.errorDialogInputClosed
    )

    protected open fun block(type: Int, title: String, text: String) {
        beforeDialog()
        line("-".repeat(width()))
        line(heading(type, title))
        line(text)
        line("-".repeat(width()))
    }

    protected fun heading(type: Int, title: String): String {
        val messages = LanguageController.messages
        val label = when (type) {
            ERROR -> messages.infoLabelOnError
            INFORMATION -> messages.infoLabelOnInfo
            WARNING -> messages.infoLabelOnWarning
            QUESTION -> messages.infoLabelOnQuestion
            else -> return title
        }
        return "[$label] $title"
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

    private companion object {
        const val ERROR = 1
        const val INFORMATION = 2
        const val WARNING = 3
        const val QUESTION = 4
    }

}
