// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import javax.swing.JOptionPane
import javax.swing.UIManager

/**
 * Implements utilitary methods for displaying messages.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class GUIDialogs
@JvmOverloads constructor(
    uiOptions: UserInterfaceOptions,
    /**
     * The default width for the message body, in pixels.
     */
    val defaultWidth: Int = 250,
) {
    init {
        applyUIOptions(uiOptions)
    }

    /**
     * Set the system's look and feel.
     *
     * @param userInterfaceOptions The user interface options containing the
     *   new look and feel.
     */
    private fun applyUIOptions(userInterfaceOptions: UserInterfaceOptions) {
        // get the current look and feel
        var laf = userInterfaceOptions.swingLookAndFeel

        // check if one is actually set
        if (laf != "none") {
            // use a special keyword to indicate
            // the use of a system look and feel
            if (laf == "system") {
                laf = UIManager.getSystemLookAndFeelClassName()
            }

            // let's try it, in case it fails,
            // rely to the default look and feel
            try {
                // get the system look and feel name
                // and try to set it as default
                UIManager.setLookAndFeel(laf)
            } catch (_: Exception) {
                // quack, quack, quack
            }
        }
    }

    /**
     * Normalizes the icon type to one of the five available icons.
     * @param value An integer value.
     * @return The normalized integer value.
     */
    @Suppress("MagicNumber")
    private fun normalizeIconType(value: Int): Int {
        // do the normalization according to the available
        // icons in the underlying message implementation
        return when (value) {
            1 -> JOptionPane.ERROR_MESSAGE
            2 -> JOptionPane.INFORMATION_MESSAGE
            3 -> JOptionPane.WARNING_MESSAGE
            4 -> JOptionPane.QUESTION_MESSAGE
            else -> JOptionPane.PLAIN_MESSAGE
        }
    }

    /**
     * Normalizes the message width, so only valid nonzero values are accepted.
     * @param value An integer value corresponding to the message width.
     * @return The normalized width.
     */
    private fun normalizeMessageWidth(value: Int): Int {
        return if (value > 0) value else defaultWidth
    }

    /**
     * Shows the message.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    @JvmOverloads
    fun showMessage(
        width: Int = defaultWidth,
        type: Int,
        title: String,
        text: String,
    ) {
        // effectively shows the message based
        // on the provided parameters
        JOptionPane.showMessageDialog(
            null,
            "<html><body style=\"width:%dpx\">$text</body></html>".format(
                normalizeMessageWidth(width),
            ),
            title,
            normalizeIconType(type),
        )
    }

    /**
     * Shows a message with options presented as an array of buttons.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    @JvmOverloads
    fun showOptions(
        width: Int = defaultWidth,
        type: Int,
        title: String,
        text: String,
        buttons: Array<out Any>,
    ): Int {
        // returns the index of the selected button,
        // zero if nothing is selected
        return JOptionPane.showOptionDialog(
            null,
            "<html><body style=\"width:%dpx\">$text</body></html>".format(
                normalizeMessageWidth(width),
            ),
            title,
            JOptionPane.DEFAULT_OPTION,
            normalizeIconType(type), null,
            buttons,
            buttons[0],
        ) + 1
    }

    /**
     * Shows a message with a text input.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    @JvmOverloads
    fun showInput(
        width: Int = defaultWidth,
        type: Int,
        title: String,
        text: String,
    ): String {
        // get the string from the
        // input text, if any
        val input = JOptionPane.showInputDialog(
            null,
            "<html><body style=\"width:%dpx\">$text</body></html>".format(
                normalizeMessageWidth(width),
            ),
            title,
            normalizeIconType(type),
        )

        // if the input is not null, that is,
        // the user actually typed something
        // return the trimmed string otherwise
        // an empty string
        return input?.trim() ?: ""
    }

    /**
     * Shows a message with options presented as a dropdown list of elements.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    @JvmOverloads
    fun showDropdown(
        width: Int = defaultWidth,
        type: Int,
        title: String,
        text: String,
        elements: Array<out Any>,
    ): Int {
        // show the dropdown list and get the selected object, if any
        val index = JOptionPane.showInputDialog(
            null,
            "<html><body style=\"width:%dpx\">$text</body></html>".format(
                normalizeMessageWidth(width),
            ),
            title,
            normalizeIconType(type),
            null,
            elements,
            elements[0],
        )

        // if it's not a null object, let's find the corresponding index
        if (index != null) {
            elements.forEachIndexed { i, value ->
                // if the element is found, simply
                // return the index plus 1, as zero
                // corresponds to no selection at all
                if (value == index) {
                    return i + 1
                }
            }
        }

        // nothing was selected, simply return zero
        return 0
    }
}
