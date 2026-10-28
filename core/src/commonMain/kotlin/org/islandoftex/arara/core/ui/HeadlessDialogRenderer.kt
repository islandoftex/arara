// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.ui.DialogRenderer
import org.islandoftex.arara.core.localization.LanguageController

/** The library never assumes its host has a graphical or terminal interface. */
object HeadlessDialogRenderer : DialogRenderer {
    private fun unavailable(): Nothing = throw AraraException(
        LanguageController.messages.errorDialogsNotConfigured
    )

    /** Reject interaction until the host supplies a dialog renderer. */
    override fun showMessage(type: Int, title: String, text: String): Unit = unavailable()
    /** Reject interaction until the host supplies a dialog renderer. */
    override fun showOptions(type: Int, title: String, text: String, buttons: Array<out Any>): Int =
        unavailable()
    /** Reject interaction until the host supplies a dialog renderer. */
    override fun showInput(type: Int, title: String, text: String): String = unavailable()
    /** Reject interaction until the host supplies a dialog renderer. */
    override fun showList(type: Int, title: String, text: String, elements: Array<out Any>): Int =
        unavailable()
}
