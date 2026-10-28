// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import org.islandoftex.arara.cli.console.ConsoleTasks
import org.islandoftex.arara.core.session.Session

/** Focused styled presentation component. */
internal class StyledTaskRenderer(state: StyledRenderState) : StyledComponent(state), ConsoleTasks {
    override fun printEntry(name: String, task: String) {
        state.progressOutput.suspendProgress()
        state.details.closeOutputFrame()
        compactEntry = false
        displayLine = true
        displayResult = false
        if (isDryRunMode) {
            buildDryRunEntry(name, task)
        } else if (isVerboseMode) {
            buildLongEntry(name, task)
        } else {
            buildShortEntry(name, task)
        }
    }

    private fun buildShortEntry(name: String, task: String) {
        val animated = canAnimate
        val limit = layout.width - if (animated) 1 else 0
        val wordWidth = maxOf(longestMatch, if (animated) LiveProgress.CLOCK_WIDTH else 0)
        val reserved = layout.cells(successGlyph()) + 1 + wordWidth
        val headWidth = limit - reserved - StyledRenderMetrics.ENTRY_SPACING
        compactEntry = headWidth >= StyledRenderMetrics.MINIMUM_HEAD_WIDTH
        val head = "${runningGlyph()} $name · $task"
        if (compactEntry) {
            val fitted = layout.fit(head, headWidth)
            val fill = limit - layout.cells(fitted) - reserved - StyledRenderMetrics.HEADER_SPACING
            terminal.print(fitted + " " + muted(leader.repeat(fill)) + " ")
            if (animated) {
                progress.start(limit - reserved, reserved, Session.userInterfaceOptions.locale.decimalSeparator)
            }
        } else {
            layout.print(head)
        }
    }

    private fun buildLongEntry(name: String, task: String) {
        if (displayRolling) {
            terminal.println("")
        } else {
            displayRolling = true
        }
        terminal.println(muted(displaySeparator()))
        layout.print("${runningGlyph()} $name · $task")
        terminal.println(muted(displaySeparator()))
    }

    private fun buildDryRunEntry(name: String, task: String) {
        if (displayRolling) {
            terminal.println("")
        } else {
            displayRolling = true
        }
        layout.print(muted("[DR] ${runningGlyph()} $name · $task"))
        terminal.println(muted(displaySeparator()))
    }

    override fun printEntryResult(value: Boolean) {
        state.progressOutput.stopProgress()
        state.details.closeOutputFrame()
        displayLine = false
        displayResult = true

        if (!isDryRunMode) {
            if (!isVerboseMode) {
                val styled = if (value) {
                    terminal.theme.success(
                        successGlyph() + " " + getResult(value)
                    )
                } else {
                    terminal.theme.danger(
                        failureGlyph() + " " + getResult(value)
                    )
                }
                if (compactEntry) terminal.println(styled) else layout.print(styled)
            } else {
                val result = layout.fit(getResult(value), layout.width)
                val aligned = rule.repeat((layout.width - layout.cells(result)).coerceAtLeast(0)) + result
                terminal.println("")
                terminal.println(if (value) terminal.theme.success(aligned) else terminal.theme.danger(aligned))
            }
        }
    }
}
