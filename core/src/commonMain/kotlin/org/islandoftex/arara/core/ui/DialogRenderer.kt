// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

/** Host-provided interactive dialogs. Choice indices start at one; cancellation returns zero. */
interface DialogRenderer {
    /** Legacy width used when a rule does not supply one. */
    val defaultWidth: Int get() = DEFAULT_WIDTH

    /** Display a message. */
    fun showMessage(width: Int = defaultWidth, type: Int, title: String, text: String)

    /** Ask for a button selection, returning its one-based index or zero. */
    fun showOptions(width: Int = defaultWidth, type: Int, title: String, text: String, buttons: Array<out Any>): Int

    /** Ask for trimmed text; cancellation returns an empty string. */
    fun showInput(width: Int = defaultWidth, type: Int, title: String, text: String): String

    /** Ask for an element selection, returning its one-based index or zero. */
    fun showDropdown(width: Int = defaultWidth, type: Int, title: String, text: String, elements: Array<out Any>): Int

    companion object {
        const val DEFAULT_WIDTH: Int = 250
    }
}

/** Dialog capability installed by the host, independent of rule-helper initialization. */
object Dialogs {
    var renderer: DialogRenderer = HeadlessDialogRenderer
}
