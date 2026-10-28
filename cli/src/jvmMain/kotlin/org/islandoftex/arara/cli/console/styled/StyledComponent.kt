// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.TextStyle
import org.islandoftex.arara.cli.console.ConsoleOutput
import org.islandoftex.arara.cli.console.ConsoleSeparators
import org.islandoftex.arara.core.localization.LanguageController

/** Common text geometry and access to one renderer’s state. */
internal open class StyledComponent(protected val state: StyledRenderState) : ConsoleOutput, ConsoleSeparators {
    protected val terminal
        get() = state.terminal
    protected val longestMatch
        get() = state.longestMatch
    protected val layout
        get() = state.layout
    protected val progress
        get() = state.progress
    protected var workspaceFeedback
        get() = state.workspaceFeedback
        set(value) { state.workspaceFeedback = value }
    protected var compactEntry
        get() = state.compactEntry
        set(value) { state.compactEntry = value }
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
    protected var outputFrameOpen
        get() = state.outputFrameOpen
        set(value) { state.outputFrameOpen = value }
    protected val isDryRunMode
        get() = state.isDryRunMode
    protected val isVerboseMode
        get() = state.isVerboseMode
    protected val isDecorated
        get() = state.isDecorated
    protected val canAnimate
        get() = state.canAnimate
    protected val cursorAvailable
        get() = state.cursorAvailable
    protected val rule
        get() = state.rule
    protected val leader
        get() = state.leader
    protected val muted
        get() = state.muted

    protected fun getResult(value: Boolean): String =
        if (value)
            LanguageController.messages.infoLabelOnSuccess
        else
            LanguageController.messages.infoLabelOnFailure

    override fun printWrapped(text: String) {
        state.progressOutput.suspendProgress()
        layout.print(text)
    }

    override fun displayOutputSeparator(message: String): String {
        val text = layout.fit(" $message ", layout.width)
        val remaining = (layout.width - layout.cells(text)).coerceAtLeast(0)
        return rule.repeat(remaining / 2) + text + rule.repeat(remaining - remaining / 2)
    }

    override fun displaySeparator(): String = rule.repeat(layout.width)

    protected fun runningGlyph(): String =
        if (isDecorated) StyledTheme.runningGlyph else StyledTheme.runningFallback

    protected fun successGlyph(): String =
        if (isDecorated) StyledTheme.successGlyph else StyledTheme.successFallback

    protected fun failureGlyph(): String =
        if (isDecorated) StyledTheme.failureGlyph else StyledTheme.failureFallback

    protected fun hyperlink(text: String, url: String): String =
        if (terminal.terminalInfo.ansiHyperLinks) TextStyle(hyperlink = url)(text) else text
}
