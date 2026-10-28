// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.ui.DialogRenderer
import org.islandoftex.arara.core.ui.Dialogs

/** Interactive responses used by directive conditionals. */
object ConditionalDialogHelpers {
    private val dialogs: DialogRenderer get() = Dialogs.renderer

    /**
     * Shows a message with options presented as an array of buttons.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    fun showOptions(
        type: Int,
        title: String,
        text: String,
        buttons: Array<out Any>
    ): Int = dialogs.showOptions(type, title, text, buttons)

    /**
     * Shows a message with a text input.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    fun showInput(
        type: Int,
        title: String,
        text: String
    ): String = dialogs.showInput(type, title, text)

    /**
     * Shows a message with options presented as a numbered list of elements.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    fun showList(
        type: Int,
        title: String,
        text: String,
        elements: Array<out Any>
    ): Int = dialogs.showList(type, title, text, elements)
}
