// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.ui.DisplayTaskActions

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
