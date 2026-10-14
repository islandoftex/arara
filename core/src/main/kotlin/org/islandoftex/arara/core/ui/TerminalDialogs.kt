// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

/**
 * Implements the dialog facade: the surface the rule helpers of
 * both engines call, delegating to the dialog renderer the
 * application resolved at startup. The library ships no dialog
 * presentation of its own - the headless default states that no
 * support is configured, and an application with an interactive
 * surface wires its implementation - so the semantics of the
 * conversation live in the contract while the presentation belongs
 * to whoever actually has a surface to present on.
 */
object TerminalDialogs {
    /**
     * The dialog renderer the facade delegates to: no support by
     * default, replaced when the application provides a surface.
     */
    var renderer: DialogRenderer = HeadlessDialogRenderer

    /**
     * Shows the message.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    fun showMessage(type: Int, title: String, text: String) =
        renderer.showMessage(type, title, text)

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
    ): Int = renderer.showOptions(type, title, text, buttons)

    /**
     * Shows a message with a text input.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    fun showInput(type: Int, title: String, text: String): String =
        renderer.showInput(type, title, text)

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
    fun showList(
        type: Int,
        title: String,
        text: String,
        elements: Array<out Any>
    ): Int = renderer.showList(type, title, text, elements)
}
