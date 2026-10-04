// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Panel
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.console.plain.PlainDialogRenderer
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.ui.DialogRenderer
import org.islandoftex.arara.core.utils.wrap

/**
 * Implements the styled dialog renderer: the conversation a rule
 * starts, framed in the box the interruption deserves - the border
 * tinted by the message kind, the severity glyph beside the tagged
 * title, the choices numbered in the accent with their hanging
 * indent, and the prompt with the bold range. The rule author's
 * text renders untouched inside. A stream that cannot carry the
 * box falls back to the classic terminal dialogs themselves, so
 * the degraded form is the plain one by construction: the sibling
 * renders it, not a copy of it, and the reading semantics are
 * shared, never re-derived.
 *
 * @author Island of TeX
 */
@Suppress("MagicNumber", "TooManyFunctions")
class StyledDialogRenderer(private val terminal: Terminal) : DialogRenderer {
    /**
     * The message type codes of the dialog helpers, as rule authors
     * write them: an error, an information, a warning and a
     * question, with every other value rendering as a plain dialog.
     */
    private val typeError = 1
    private val typeInfo = 2
    private val typeWarning = 3
    private val typeQuestion = 4

    /**
     * The borders and the padding of the box, reserved from the
     * wrapping measure of the content.
     */
    private val boxChromeWidth = 4

    /**
     * Whether the terminal carries the box; a plain stream keeps the
     * conversation in the classic terminal dialogs.
     */
    private val isDecorated: Boolean =
        terminal.terminalInfo.ansiLevel != AnsiLevel.NONE

    /**
     * Shows the message.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    override fun showMessage(type: Int, title: String, text: String) {
        if (!isDecorated) {
            PlainDialogRenderer.showMessage(type, title, text)
            return
        }
        terminal.println(panel(type, title, text), width = dialogWidth())
        terminal.println("")
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
        return if (isDecorated) {
            terminal.println(panel(type, title, text, buttons.map { it.toString() }), width = dialogWidth())
            terminal.println("")
            readSelection(buttons.size, allowNone = false)
        } else {
            PlainDialogRenderer.showOptions(type, title, text, buttons)
        }
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
        if (!isDecorated) {
            return PlainDialogRenderer.showInput(type, title, text)
        }
        terminal.println(panel(type, title, text), width = dialogWidth())
        terminal.println("")
        terminal.print(terminal.theme.info("> "))
        return (terminal.readLineOrNull(false) ?: "").trim().also {
            // the conversation ends with air: the prompt line
            // completes, and a blank separates the answer from
            // whatever the run continues with
            terminal.println("")
            terminal.println("")
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
        return if (isDecorated) {
            terminal.println(panel(type, title, text, elements.map { it.toString() }), width = dialogWidth())
            terminal.println("")
            readSelection(elements.size, allowNone = true)
        } else {
            PlainDialogRenderer.showDropdown(type, title, text, elements)
        }
    }

    /**
     * Builds the dialog box: the severity glyph with the tagged
     * title, the rule author's text wrapped inside, and the numbered
     * choices with their hanging indent when the conversation offers
     * them.
     */
    private fun panel(
        type: Int,
        title: String,
        text: String,
        entries: List<String> = emptyList()
    ): Panel {
        val interior = wrappingWidth()
        // the heading carries the severity of the whole box - bold,
        // in the same tint as the border - so the conversation
        // speaks one severity language with the run itself
        val heading = tagFor(type)
            ?.let {
                (styleFor(type) + TextStyle(bold = true))(
                    "${glyphFor(type)} $it — $title"
                )
            }
            ?: title
        val lines = mutableListOf(heading, "")
        lines += text.wrap(interior).lineSequence()
        if (entries.isNotEmpty()) {
            lines += ""
            entries.forEachIndexed { index, entry ->
                val label = "${index + 1}) "
                val numbered = terminal.theme.info(label)
                entry.wrap(interior - label.length).lineSequence()
                    .forEachIndexed { lineIndex, line ->
                        lines += if (lineIndex == 0) {
                            numbered + line
                        } else {
                            " ".repeat(label.length) + line
                        }
                    }
            }
        }
        return Panel(
            lines.joinToString("\n"),
            expand = true,
            borderStyle = styleFor(type)
        )
    }

    /**
     * Read numbered choices until one is valid, printing the prompt
     * with the bold range before every attempt; the interpretation
     * of a typed line is the shared semantics of the terminal
     * dialogs.
     */
    private fun readSelection(count: Int, allowNone: Boolean): Int {
        val prompt = LanguageController.messages.infoLabelDialogPrompt
        val range = if (allowNone) "0-$count" else "1-$count"
        while (true) {
            terminal.print("$prompt (" + TextStyle(bold = true)(range) + "): ")
            val raw = terminal.readLineOrNull(false)
                // a numbered dialog read from a closed input is a
                // misuse; the safe mode exists precisely to prevent
                // reaching a dialog headlessly. The prompt line
                // completes first, so the error that follows starts
                // on its own line
                ?: run {
                    terminal.println("")
                    throw AraraException(
                        LanguageController.messages.errorDialogInputClosed
                    )
                }
            PlainDialogRenderer.parseSelection(raw, count, allowNone)?.let {
                // the conversation ends with air: the prompt line
                // completes, and a blank separates the choice from
                // whatever the run continues with
                terminal.println("")
                terminal.println("")
                return it
            }
        }
    }

    /**
     * The wrapping measure of the box interior: the readable prose
     * measure minus the borders and the padding of the frame.
     */
    private fun wrappingWidth(): Int =
        terminal.size.width.coerceAtMost(StyledTheme.proseMaximumWidth) -
            boxChromeWidth

    /**
     * The width the dialog box renders at: the presentation's
     * measure, so a conversation occupies the same page the run
     * does.
     */
    private fun dialogWidth(): Int =
        wrappingWidth() + boxChromeWidth

    /**
     * The tag of the dialog type, rendered before the title; the
     * plain dialog carries none.
     */
    private fun tagFor(type: Int): String? = when (type) {
        typeError -> LanguageController.messages.infoLabelOnError
        typeInfo -> LanguageController.messages.infoLabelOnInfo
        typeWarning -> LanguageController.messages.infoLabelOnWarning
        typeQuestion -> LanguageController.messages.infoLabelOnQuestion
        else -> null
    }

    /**
     * The severity glyph of the dialog type. The box renders on
     * the decorated path alone - an undecorated stream receives
     * the classic plain dialog instead - so the glyphs need no
     * fallback of their own here.
     */
    private fun glyphFor(type: Int): String = when (type) {
        typeError -> StyledTheme.failureGlyph
        typeWarning -> StyledTheme.warningGlyph
        typeInfo -> StyledTheme.infoGlyph
        else -> "?"
    }

    /**
     * The border tint of the dialog box, following the message
     * kind.
     */
    private fun styleFor(type: Int): TextStyle = when (type) {
        typeError -> terminal.theme.danger
        typeWarning -> terminal.theme.warning
        typeInfo -> terminal.theme.info
        typeQuestion -> StyledTheme.logo
        else -> terminal.theme.muted
    }
}
