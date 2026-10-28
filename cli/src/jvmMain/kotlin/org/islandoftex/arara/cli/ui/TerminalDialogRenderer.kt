// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ui

import java.io.BufferedReader
import java.io.PrintWriter
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.DialogRenderer

/** Terminal dialogs use host-owned streams and the current configured column width. */
open class TerminalDialogRenderer(
    input: BufferedReader = System.`in`.bufferedReader(),
    protected val output: PrintWriter = PrintWriter(System.out, true),
    private val columns: () -> Int = { Session.userInterfaceOptions.terminalOutputWidth },
    protected val beforeDialog: () -> Unit = {}
) : DialogRenderer {
    private val answers = DialogAnswers(input)

    /** Print the message without reading a response. */
    override fun showMessage(type: Int, title: String, text: String) {
        block(type, title, text)
    }

    /** Print a prompt and return one input line; end-of-input raises a localized error. */
    override fun showInput(type: Int, title: String, text: String): String {
        block(type, title, text)
        output.print("> ")
        output.flush()
        return answers.read().trim()
    }

    /** Return a one-based button choice, or zero for cancellation or an empty choice list. */
    override fun showOptions(type: Int, title: String, text: String, buttons: Array<out Any>): Int =
        answers.selection(buttons, { selectionBlock(type, title, text, buttons) }, ::prompt)

    /** Return a one-based list choice, or zero for cancellation or an empty list. */
    override fun showList(type: Int, title: String, text: String, elements: Array<out Any>): Int =
        answers.selection(elements, { selectionBlock(type, title, text, elements) }, ::prompt)

    /** Present choices before the shared selection loop reads an answer. */
    protected open fun selectionBlock(type: Int, title: String, text: String, entries: Array<out Any>) {
        block(type, title, text)
        entries.forEachIndexed { index, value -> line("${index + 1}. $value") }
    }

    /** Render the inclusive selection range without changing accepted values. */
    protected open fun prompt(range: String) {
        line("${LanguageController.messages.infoLabelDialogPrompt} ($range):")
        output.print("> ")
        output.flush()
    }

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

    protected open fun line(text: String) {
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

private class DialogAnswers(private val input: BufferedReader) {
    fun read(): String = input.readLine() ?: throw AraraException(
        LanguageController.messages.errorDialogInputClosed
    )

    fun selection(entries: Array<out Any>, present: () -> Unit, prompt: (String) -> Unit): Int {
        if (entries.isEmpty()) return 0
        present()
        val size = entries.size
        while (true) {
            prompt("0-$size")
            val chosen = read().trim().toIntOrNull()
            if (chosen != null && chosen in 0..size) return chosen
        }
    }
}
