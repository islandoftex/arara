// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.ui

/** Host-provided interactive dialogs. Choice indices start at one; cancellation returns zero. */
public interface DialogRenderer {
    /** Display a message. */
    public fun showMessage(type: Int, title: String, text: String)

    /** Ask for a button selection, returning its one-based index or zero. */
    public fun showOptions(type: Int, title: String, text: String, buttons: Array<out Any>): Int

    /** Ask for trimmed text; cancellation returns an empty string. */
    public fun showInput(type: Int, title: String, text: String): String

    /** Ask for an element selection, returning its one-based index or zero. */
    public fun showList(type: Int, title: String, text: String, elements: Array<out Any>): Int

}
