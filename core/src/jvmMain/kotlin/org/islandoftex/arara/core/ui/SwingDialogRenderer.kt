// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.configuration.UserInterfaceOptions

/** Adapts the existing Swing implementation without changing its public overloads. */
class SwingDialogRenderer(private val options: () -> UserInterfaceOptions) : DialogRenderer {
    private val dialogs: GUIDialogs get() = GUIDialogs(options())
    override val defaultWidth: Int get() = DialogRenderer.DEFAULT_WIDTH

    override fun showMessage(width: Int, type: Int, title: String, text: String): Unit =
        dialogs.showMessage(width, type, title, text)
    override fun showOptions(width: Int, type: Int, title: String, text: String, buttons: Array<out Any>): Int =
        dialogs.showOptions(width, type, title, text, buttons)
    override fun showInput(width: Int, type: Int, title: String, text: String): String =
        dialogs.showInput(width, type, title, text)
    override fun showDropdown(width: Int, type: Int, title: String, text: String, elements: Array<out Any>): Int =
        dialogs.showDropdown(width, type, title, text, elements)
}
