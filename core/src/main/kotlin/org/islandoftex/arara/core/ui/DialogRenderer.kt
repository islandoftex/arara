// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

/**
 * Defines the contract of a dialog renderer: the interactive
 * conversation surface the rule helpers offer. The semantics belong
 * to the contract - the message kind codes rule authors write, the
 * numbered choices starting from one, the deliberate no-selection
 * of a dropdown yielding zero, the trimmed answer of an input - and
 * the presentation belongs to the host: an application wires the
 * implementation matching the surface it actually has. The library
 * assumes none by default, so a host that never wires one meets a
 * clear error instead of a silent guess.
 */
interface DialogRenderer {
    /**
     * Shows the message.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    fun showMessage(type: Int, title: String, text: String)

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
    fun showOptions(
        type: Int,
        title: String,
        text: String,
        buttons: Array<out Any>
    ): Int

    /**
     * Shows a message with a text input.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    fun showInput(type: Int, title: String, text: String): String

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
    fun showDropdown(
        type: Int,
        title: String,
        text: String,
        elements: Array<out Any>
    ): Int
}
