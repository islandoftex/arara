// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import org.islandoftex.arara.cli.console.ConsoleOutput
import org.islandoftex.arara.cli.console.ConsoleSeparators
import org.islandoftex.arara.cli.utils.center
import org.islandoftex.arara.cli.utils.wrap
import org.islandoftex.arara.core.localization.LanguageController

/** Common text geometry and access to one renderer’s state. */
internal open class PlainComponent(protected val state: PlainRenderState) : ConsoleOutput, ConsoleSeparators {
    protected val longestMatch
        get() = state.longestMatch
    protected val shortenedLongestMatch
        get() = state.shortenedLongestMatch
    protected var displayLine
        get() = state.displayLine
        set(value) { state.displayLine = value }
    protected var displayResult
        get() = state.displayResult
        set(value) { state.displayResult = value }
    protected var displayRolling
        get() = state.displayRolling
        set(value) { state.displayRolling = value }
    protected var displayException
        get() = state.displayException
        set(value) { state.displayException = value }
    protected var displaySummary
        get() = state.displaySummary
        set(value) { state.displaySummary = value }
    protected val isDryRunMode
        get() = state.isDryRunMode
    protected val isVerboseMode
        get() = state.isVerboseMode
    protected val outputWidth
        get() = state.outputWidth

    /**
     * Gets the string representation of the provided boolean value.
     *
     * @param value The boolean value.
     * @return The string representation.
     */
    protected fun getResult(value: Boolean): String =
        if (value)
            LanguageController.messages.infoLabelOnSuccess
        else
            LanguageController.messages.infoLabelOnFailure

    /**
     * Displays the provided text wrapped nicely according to the default
     * terminal width.
     *
     * @param text The text to be displayed.
     */
    override fun printWrapped(text: String) = println(text.wrap(outputWidth))

    /**
     * Gets the output separator with the provided text.
     *
     * @param message The provided text.
     * @return A string containing the output separator with the provided text.
     */
    override fun displayOutputSeparator(message: String): String =
        " $message ".center(outputWidth, '-')

    /**
     * Gets the line separator.
     *
     * @return A string containing the line separator.
     */
    override fun displaySeparator(): String = "-".repeat(outputWidth)
}
