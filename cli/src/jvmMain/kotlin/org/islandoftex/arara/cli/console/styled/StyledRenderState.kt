// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.TimeSource
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/** State shared only by the components of one styled renderer. */
internal class StyledRenderState(val terminal: Terminal, clock: TimeSource,
    schedulerFactory: () -> ProgressScheduler) {
    val longestMatch: Int
        get() = maxOf(
            layout.cells(LanguageController.messages.infoLabelOnSuccess),
            layout.cells(LanguageController.messages.infoLabelOnFailure),
            layout.cells(LanguageController.messages.infoLabelOnError)
        )

    val layout = StyledLayout(terminal)
    val progress = LiveProgress(terminal, layout, clock, schedulerFactory)
    var workspaceFeedback = false
    var compactEntry = false
    var displayLine = false
    var displayResult = false
    var displayRolling = false
    var displayException = false
    var outputFrameOpen = false

    val isDryRunMode: Boolean
        get() = LinearExecutor.executionOptions.executionMode ==
            ExecutionMode.DRY_RUN

    val isVerboseMode: Boolean
        get() = LinearExecutor.executionOptions.verbose

    val isDecorated: Boolean =
        terminal.terminalInfo.ansiLevel != AnsiLevel.NONE

    val canAnimate: Boolean
        get() = Session.userInterfaceOptions.enableAnimations && isDecorated &&
            terminal.terminalInfo.outputInteractive && cursorAvailable

    // Mordant 3.1's supportsAnsiCursor flag is false on ordinary terminals.
    // Its cursor API returns no movements when cursor control is unavailable.
    val cursorAvailable: Boolean
        get() = terminal.cursor.getMoves { startOfLine() }.isNotEmpty()

    val rule: String
        get() = if (isDecorated) StyledTheme.ruleGlyph else StyledTheme.ruleFallback

    val leader: String
        get() = if (isDecorated) StyledTheme.leaderGlyph else StyledTheme.leaderFallback

    val muted get() = terminal.theme.muted

    val details = StyledDetailsRenderer(this)
    val progressOutput = StyledProgressRenderer(this)
}

internal object StyledRenderMetrics {
    const val FAILURE_TAIL_LINES = 12
    const val HEADER_SPACING = 2
    const val ENTRY_SPACING = 3
    const val MINIMUM_HEAD_WIDTH = 8
    const val FRAME_SPACING = 3
}
