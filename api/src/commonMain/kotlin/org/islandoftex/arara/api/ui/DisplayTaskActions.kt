// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.ui

import org.islandoftex.arara.api.rules.DirectiveConditional

/** Task accounting and terminal presentation during rule execution. */
public interface DisplayTaskActions {
    /** Print the application greeting through the active renderer. */
    public fun printBanner()

    /** Print wrapped prose through the active renderer. */
    public fun printWrapped(text: String)

    /** Present rule authors through the active renderer. */
    public fun printAuthors(authors: List<String>)

    /** Present a directive conditional through the active renderer. */
    public fun printConditional(conditional: DirectiveConditional)

    /** Count and log a task, then start its terminal presentation. */
    public fun printEntry(name: String, task: String)

    /** Log a task result and finish its terminal presentation. */
    public fun printEntryResult(value: Boolean)
}
