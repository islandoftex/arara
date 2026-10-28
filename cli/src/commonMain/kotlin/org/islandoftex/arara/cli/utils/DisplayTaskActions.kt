// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.rules.DirectiveConditional

/** Task accounting and terminal presentation during rule execution. */
interface DisplayTaskActions {
    /** Print the application greeting through the active renderer. */
    fun printBanner()

    /** Print wrapped prose through the active renderer. */
    fun printWrapped(text: String)

    /** Present rule authors through the active renderer. */
    fun printAuthors(authors: List<String>)

    /** Present a directive conditional through the active renderer. */
    fun printConditional(conditional: DirectiveConditional)

    /** Count and log a task, then start its terminal presentation. */
    fun printEntry(name: String, task: String)

    /** Log a task result and finish its terminal presentation. */
    fun printEntryResult(value: Boolean)
}

private object DefaultDisplayTaskActions : DisplayTaskActions {
    private val renderer get() = DisplayUtils.renderer
    private val presentation get() = DisplayUtils.presentation

    /** Print the application greeting through the active renderer. */
    override fun printBanner() = renderer.printBanner()

    /** Print wrapped prose through the active renderer. */
    override fun printWrapped(text: String) = renderer.printWrapped(text)

    /** Present rule authors through the active renderer. */
    override fun printAuthors(authors: List<String>) = renderer.printAuthors(authors)

    /** Present a directive conditional through the active renderer. */
    override fun printConditional(conditional: DirectiveConditional) = renderer.printConditional(conditional)

    /** Count and log a task, then start its terminal presentation. */
    override fun printEntry(name: String, task: String) {
        presentation.task()
        RunLog.entry(name, task)
        renderer.printEntry(name, task)
    }

    /** Log a task result and finish its terminal presentation. */
    override fun printEntryResult(value: Boolean) {
        RunLog.taskResult(value)
        renderer.printEntryResult(value)
    }

}

internal val displayTaskActions: DisplayTaskActions get() = DefaultDisplayTaskActions
