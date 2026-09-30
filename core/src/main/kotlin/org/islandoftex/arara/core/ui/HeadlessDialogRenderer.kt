// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements the default dialog renderer: no dialog support at all.
 * A library run makes no assumption about the surfaces its host
 * offers - a terminal, a window system, a web request - so the
 * default states the truth when a rule reaches a dialog: support
 * has to be provided by the application embedding arara. An
 * application with an interactive surface replaces this renderer
 * with its own implementation at startup.
 *
 * @author Island of TeX
 */
object HeadlessDialogRenderer : DialogRenderer {
    /**
     * Raises the localized error stating that no dialog support is
     * configured.
     */
    private fun unsupported(): Nothing =
        throw AraraException(
            LanguageController.messages.errorDialogsNotConfigured
        )

    /**
     * Shows the message.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    override fun showMessage(type: Int, title: String, text: String) =
        unsupported()

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
    ): Int = unsupported()

    /**
     * Shows a message with a text input.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    override fun showInput(type: Int, title: String, text: String): String =
        unsupported()

    /**
     * Shows a message with options presented as a dropdown list of
     * elements.
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
    ): Int = unsupported()
}
