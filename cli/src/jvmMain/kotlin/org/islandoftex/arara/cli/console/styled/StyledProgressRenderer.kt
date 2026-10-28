// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import org.islandoftex.arara.api.ui.ConsoleProgress
import org.islandoftex.arara.core.session.Session

/** Focused styled presentation component. */
internal class StyledProgressRenderer(state: StyledRenderState) : StyledComponent(state), ConsoleProgress {
    override fun startBuildDirectory(message: String) {
        state.progressOutput.suspendProgress()
        state.details.closeOutputFrame()
        compactEntry = false
        displayLine = false
        layout.print(message)
        val columns = LiveProgress.CLOCK_WIDTH + StyledRenderMetrics.HEADER_SPACING
        if (canAnimate && !isDryRunMode && layout.width > columns) {
            workspaceFeedback = true
            progress.start(0, columns, Session.userInterfaceOptions.locale.decimalSeparator)
        }
    }

    override fun finishBuildDirectory(message: String) {
        state.progressOutput.suspendProgress()
        layout.print(message)
    }

    internal fun stopProgress() {
        val hasPendingLine = (displayLine && compactEntry) || workspaceFeedback
        if (progress.stop() && hasPendingLine) {
            terminal.println("")
            compactEntry = false
        }
        workspaceFeedback = false
    }

    override fun suspendProgress() {
        state.progressOutput.stopProgress()
        if (displayLine && compactEntry) {
            terminal.println("")
            compactEntry = false
        }
    }

    override fun close() {
        state.progressOutput.suspendProgress()
        state.details.closeOutputFrame()
        progress.close()
    }
}
