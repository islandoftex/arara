/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.utils

import com.github.cereda.arara.configuration.ConfigurationController
import javax.swing.JOptionPane
import javax.swing.UIManager

/**
 * Implements utilitary methods for displaying messages.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object MessageUtils {
    // holds the default width for the
    // message body, in pixels
    private const val WIDTH = 250

    // let's start the UI manager and set
    // the default look and feel to be as
    // close as possible to the system
    init {
        // get the current look and feel
        var laf = ConfigurationController["ui.lookandfeel"] as String

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
        return if (value > 0) value else WIDTH
    }

    /**
     * Shows the message.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    fun showMessage(width: Int, type: Int,
                    title: String, text: String) {
        // effectively shows the message based
        // on the provided parameters
        JOptionPane.showMessageDialog(null,
                String.format(
                        "<html><body style=\"width:%dpx\">%s</body></html>",
                        normalizeMessageWidth(width),
                        text),
                title,
                normalizeIconType(type)
        )
    }

    /**
     * Shows the message. It relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    fun showMessage(type: Int, title: String, text: String) {
        showMessage(WIDTH, type, title, text)
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
    fun showOptions(width: Int, type: Int, title: String,
                    text: String, vararg buttons: Any): Int {
        // returns the index of the selected button,
        // zero if nothing is selected
        return JOptionPane.showOptionDialog(null,
                String.format(
                        "<html><body style=\"width:%dpx\">%s</body></html>",
                        normalizeMessageWidth(width),
                        text),
                title,
                JOptionPane.DEFAULT_OPTION,
                normalizeIconType(type), null,
                buttons,
                buttons[0]
        ) + 1
    }

    /**
     * Shows a message with options presented as an array of buttons. It relies
     * on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    fun showOptions(type: Int, title: String,
                    text: String, vararg buttons: Any): Int {
        return showOptions(WIDTH, type, title, text, *buttons)
    }

    /**
     * Shows a message with a text input.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    fun showInput(width: Int, type: Int,
                  title: String, text: String): String {
        // get the string from the
        // input text, if any
        val input = JOptionPane.showInputDialog(null,
                String.format(
                        "<html><body style=\"width:%dpx\">%s</body></html>",
                        normalizeMessageWidth(width),
                        text),
                title,
                normalizeIconType(type))

        // if the input is not null, that is,
        // the user actually typed something
        // return the trimmed string otherwise
        // an empty string
        return input?.trim() ?: ""
    }

    /**
     * Shows a message with a text input. It relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    fun showInput(type: Int, title: String, text: String): String {
        return showInput(WIDTH, type, title, text)
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
    fun showDropdown(width: Int, type: Int, title: String,
                     text: String, vararg elements: Any): Int {
        // show the dropdown list and get
        // the selected object, if any
        val index = JOptionPane.showInputDialog(null,
                String.format(
                        "<html><body style=\"width:%dpx\">%s</body></html>",
                        normalizeMessageWidth(width),
                        text),
                title,
                normalizeIconType(type), null,
                elements,
                elements[0])

        // if it's not a null object, let's
        // find the corresponding index
        if (index != null) {
            // iterate through the array of elements
            for (i in elements.indices) {
                // if the element is found, simply
                // return the index plus 1, as zero
                // corresponds to no selection at all
                if (elements[i] == index) {
                    return i + 1
                }
            }
        }

        // nothing was selected,
        // simply return zero
        return 0
    }

    /**
     * Shows a message with options presented as a dropdown list of elements. It
     * relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    fun showDropdown(type: Int, title: String,
                     text: String, vararg elements: Any): Int {
        return showDropdown(WIDTH, type, title, text, *elements)
    }
}
