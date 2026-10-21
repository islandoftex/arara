// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.AraraException

/** The library never assumes its host has a graphical or terminal interface. */
object HeadlessDialogRenderer : DialogRenderer {
    private fun unavailable(): Nothing = throw AraraException(
        "Interactive dialogs are unavailable: the host has not configured a dialog renderer."
    )

    override fun showMessage(type: Int, title: String, text: String): Unit = unavailable()
    override fun showOptions(type: Int, title: String, text: String, buttons: Array<out Any>): Int =
        unavailable()
    override fun showInput(type: Int, title: String, text: String): String = unavailable()
    override fun showList(type: Int, title: String, text: String, elements: Array<out Any>): Int =
        unavailable()
}
