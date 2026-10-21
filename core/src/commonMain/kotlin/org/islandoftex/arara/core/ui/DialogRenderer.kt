// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

/** Host-provided interactive dialogs. Choice indices start at one; cancellation returns zero. */
interface DialogRenderer {
    /** Display a message. */
    fun showMessage(type: Int, title: String, text: String)

    /** Ask for a button selection, returning its one-based index or zero. */
    fun showOptions(type: Int, title: String, text: String, buttons: Array<out Any>): Int

    /** Ask for trimmed text; cancellation returns an empty string. */
    fun showInput(type: Int, title: String, text: String): String

    /** Ask for an element selection, returning its one-based index or zero. */
    fun showList(type: Int, title: String, text: String, elements: Array<out Any>): Int

}

/** Dialog capability installed by the host, independent of rule-helper initialization. */
object Dialogs {
    var renderer: DialogRenderer = HeadlessDialogRenderer
}
