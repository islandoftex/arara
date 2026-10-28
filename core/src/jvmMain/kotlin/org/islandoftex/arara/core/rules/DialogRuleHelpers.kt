// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.core.ui.DialogRenderer
import org.islandoftex.arara.core.ui.Dialogs

/** Message presentation for rule interpreters. */
object DialogRuleHelpers {
    private val dialogs: DialogRenderer get() = Dialogs.renderer

    /**
     * Shows the message.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    fun showMessage(
        type: Int,
        title: String,
        text: String
    ) = dialogs.showMessage(type, title, text)
}
