// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.LocalizedTaskDetails

/** Focused styled presentation component. */
internal class StyledDetailsRenderer(state: StyledRenderState) : StyledComponent(state), LocalizedTaskDetails {
    override fun printAuthors(authors: List<String>) {
        val line = if (authors.size == 1)
            LanguageController.messages.infoLabelAuthor
        else
            LanguageController.messages.infoLabelAuthors
        val text = if (authors.isEmpty())
            LanguageController.messages.infoLabelNoAuthors
        else
            authors.joinToString(", ") { it.trim() }
        state.details.printField(line, text)
    }

    override fun printConditional(conditional: DirectiveConditional) {
        if (conditional.type !== DirectiveConditionalType.NONE) {
            state.details.printField(LanguageController.messages.infoLabelConditional,
                "(" + conditional.type + ") " + conditional.condition)
        }
    }

    override fun printTaskDetails(command: String, conditional: DirectiveConditional, authors: List<String>) {
        if (isVerboseMode && !isDryRunMode) {
            state.details.closeOutputFrame()
            terminal.println("")
            state.details.printField(LanguageController.messages.infoLabelSystemCommand, command)
            printConditional(conditional)
            printAuthors(authors)
            terminal.println(muted(state.details.frameRule()))
            outputFrameOpen = true
        }
    }

    override fun printDryRunCommand(command: String) =
        state.details.printField(LanguageController.messages.infoLabelSystemCommand, command)

    internal fun printField(label: String, value: String) {
        state.progressOutput.suspendProgress()
        val labelWidth = layout.cells(label)
        val footprint = maxOf(labelWidth + 1, StyledTheme.fieldGutterWidth)
        if (layout.width - footprint < StyledTheme.minimumValueWidth) {
            layout.print(muted(label))
            layout.print(value)
        } else {
            layout.render(value, layout.width - footprint).lineSequence().forEachIndexed { index, line ->
                val prefix = if (index == 0) muted(label + " ".repeat(footprint - labelWidth))
                else " ".repeat(footprint)
                terminal.println(prefix + line)
            }
        }
    }

    internal fun frameRule(): String {
        val glyph = if (isDecorated) StyledTheme.frameGlyph else StyledTheme.frameFallback
        return "$glyph  ".repeat(layout.width / StyledRenderMetrics.FRAME_SPACING + 1).take(layout.width).trimEnd()
    }

    internal fun closeOutputFrame() {
        if (outputFrameOpen) {
            terminal.println("")
            terminal.println(muted(state.details.frameRule()))
            outputFrameOpen = false
        }
    }

    override fun printSkippedDirective(identifier: String) {
        printWrapped("- $identifier · ${LanguageController.messages.infoConsoleSkipped}")
    }

    override fun printCommandFailure(output: String) {
        state.progressOutput.suspendProgress()
        if (!isVerboseMode && output.isNotBlank()) {
            val lines = output.trimEnd().lineSequence().toList()
            terminal.println("")
            terminal.println(terminal.theme.danger(displaySeparator()))
            if (lines.size > StyledRenderMetrics.FAILURE_TAIL_LINES) {
                layout.print("...")
                if (Session.loggingOptions.format != LogFormat.NONE && Session.loggingOptions.level == LogLevel.TRACE) {
                    layout.print(muted(LanguageController.messages.infoDisplayFullOutputInLog))
                }
            }
            lines.takeLast(StyledRenderMetrics.FAILURE_TAIL_LINES).forEach(layout::print)
            terminal.println(terminal.theme.danger(displaySeparator()))
        }
    }
}
