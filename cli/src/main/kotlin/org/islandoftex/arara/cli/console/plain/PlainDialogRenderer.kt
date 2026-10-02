// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.DialogRenderer
import org.islandoftex.arara.core.utils.wrap

/**
 * Implements the classic terminal dialog renderer: the block layout
 * the dialogs have printed since the terminal took over from the
 * window system - the separator frames, the tagged titles, the
 * numbered entries with their hanging indent, and the prompt that
 * re-asks on anything outside the offered range. The application
 * wires this implementation when it runs on a terminal; the layout
 * itself is frozen on purpose, as the rest of the plain console
 * is.
 *
 * @author Island of TeX
 */
@Suppress("MagicNumber")
object PlainDialogRenderer : DialogRenderer {
    /**
     * The message type codes of the dialog helpers, as rule authors
     * write them: an error, an information, a warning and a
     * question, with every other value rendering as a plain dialog.
     */
    private const val TYPE_ERROR = 1
    private const val TYPE_INFO = 2
    private const val TYPE_WARNING = 3
    private const val TYPE_QUESTION = 4

    /**
     * Shows the message.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    override fun showMessage(type: Int, title: String, text: String) {
        printBlock(type, title, text)
        println()
        println(separator())
    }

    /**
     * Shows a message with options presented as an array of buttons.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1; an
     * empty button list offers nothing, so it yields zero.
     */
    override fun showOptions(
        type: Int,
        title: String,
        text: String,
        buttons: Array<out Any>
    ): Int {
        if (buttons.isEmpty())
            return 0
        printBlock(type, title, text)
        println()
        printEntries(buttons.map { it.toString() })
        println()
        return readSelection(buttons.size, allowNone = false)
    }

    /**
     * Shows a message with a text input.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    override fun showInput(type: Int, title: String, text: String): String {
        printBlock(type, title, text)
        println()
        print("> ")
        return (readlnOrNull() ?: "").trim().also {
            println()
            println(separator())
        }
    }

    /**
     * Shows a message with options presented as a dropdown list of
     * elements; choosing nothing yields zero, as the window system
     * contract did.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1;
     * choosing nothing yields zero, and an empty element list offers
     * nothing, so it yields zero as well.
     */
    override fun showDropdown(
        type: Int,
        title: String,
        text: String,
        elements: Array<out Any>
    ): Int {
        if (elements.isEmpty())
            return 0
        printBlock(type, title, text)
        println()
        printEntries(elements.map { it.toString() })
        println()
        return readSelection(elements.size, allowNone = true)
    }

    /**
     * Interpret one typed line as a numbered choice. Returns the
     * chosen number, zero for a deliberate no-selection when
     * [allowNone] is set, or null when the input is outside the
     * offered range and the dialog has to ask again. The empty line
     * counts as the no-selection for a dialog that offers one, and
     * as an invalid answer for a dialog that does not.
     */
    internal fun parseSelection(
        raw: String,
        count: Int,
        allowNone: Boolean
    ): Int? {
        val trimmed = raw.trim()
        return when {
            trimmed.isEmpty() && allowNone -> 0
            trimmed.isEmpty() -> null
            else -> trimmed.toIntOrNull()?.let { value ->
                when {
                    value == 0 && allowNone -> 0
                    value in 1..count -> value
                    else -> null
                }
            }
        }
    }

    /**
     * Read numbered choices until one is valid, printing the prompt
     * before every attempt. The parentheses of the prompt carry only
     * the offered numbers, never words, so the prompt stays the same
     * in every language.
     */
    private fun readSelection(count: Int, allowNone: Boolean): Int {
        val prompt = LanguageController.messages.infoLabelDialogPrompt
        val range = if (allowNone) "0-$count" else "1-$count"
        while (true) {
            print("$prompt ($range): ")
            val raw = readlnOrNull()
                // a numbered dialog read from a closed input is a
                // misuse; the safe mode exists precisely to prevent
                // reaching a dialog headlessly. The prompt line
                // completes first, so the error that follows starts
                // on its own line
                ?: run {
                    println()
                    throw AraraException(
                        LanguageController.messages.errorDialogInputClosed
                    )
                }
            parseSelection(raw, count, allowNone)?.let { choice ->
                println()
                println(separator())
                return choice
            }
        }
    }

    /**
     * The tag of the dialog type, rendered before the title; the
     * plain dialog carries none, exactly as the window system era
     * gave it no icon.
     */
    private fun tagFor(type: Int): String? = when (type) {
        TYPE_ERROR -> LanguageController.messages.infoLabelOnError
        TYPE_INFO -> LanguageController.messages.infoLabelOnInfo
        TYPE_WARNING -> LanguageController.messages.infoLabelOnWarning
        TYPE_QUESTION -> LanguageController.messages.infoLabelOnQuestion
        else -> null
    }

    /**
     * Print the heading of a dialog: the separator line, the tagged
     * and wrapped title, and the wrapped body.
     */
    private fun printBlock(type: Int, title: String, text: String) {
        val messages = LanguageController.messages
        val width = Session.userInterfaceOptions.terminalOutputWidth
        println(separator())
        val heading = tagFor(type)?.let { "$it — $title" } ?: title
        println(heading.wrap(width))
        println()
        println(text.wrap(width))
    }

    /**
     * Print the numbered entries of a choice dialog, wrapped with a
     * hanging indent so continuation lines align under the text.
     */
    private fun printEntries(entries: List<String>) {
        val width = Session.userInterfaceOptions.terminalOutputWidth
        entries.forEachIndexed { index, entry ->
            val label = "${index + 1}) "
            val indent = " ".repeat(label.length)
            entry.wrap(width - label.length).lineSequence()
                .forEachIndexed { lineIndex, line ->
                    println(
                        (if (lineIndex == 0) label else indent) + line
                    )
                }
        }
    }

    /**
     * The separator line of a dialog block, matching the width every
     * other console line wraps at.
     */
    private fun separator(): String =
        "-".repeat(Session.userInterfaceOptions.terminalOutputWidth)
}
