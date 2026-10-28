// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.cli.console.ConsoleText
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.localization.LanguageController

/** Focused styled presentation component. */
internal class StyledTextRenderer(state: StyledRenderState) : StyledComponent(state), ConsoleText {
    override fun printBanner() {
        state.progressOutput.suspendProgress()
        layout.print(StyledTheme.logo(DisplayUtils.logoString))
        layout.print(muted(DisplayUtils.versionLine))
        terminal.println("")
    }

    override fun printFileInformation(file: ProjectFile) {
        state.progressOutput.suspendProgress()
        displayRolling = false
        displayLine = false
        displayResult = false
        displayException = false
        val name = file.path.fileName
        val metadata = DisplayUtils.byteSizeToString(file.sourcePath.fileSize) +
            " · " + file.sourcePath.lastModified
        val title = hyperlink(name, file.path.toJVMPath().toUri().toASCIIString())
        val fill = layout.width - layout.cells(name) - layout.cells(metadata) - StyledRenderMetrics.HEADER_SPACING
        if (fill > 0) {
            terminal.println(title + muted(" " + rule.repeat(fill) + " " + metadata))
        } else {
            layout.print(title)
            layout.print(muted(metadata))
        }
        terminal.println("")
    }

    override fun printException(text: String, details: String?) {
        state.progressOutput.stopProgress()
        state.details.closeOutputFrame()
        displayException = true

        if (displayResult)
            terminal.println("")
        if (displayLine && !isDryRunMode) {
            val error = terminal.theme.danger(failureGlyph() + " " +
                LanguageController.messages.infoLabelOnError)
            if (compactEntry && !isVerboseMode) terminal.println(error) else layout.print(error)
            terminal.println("")
        }
        displayLine = false
        compactEntry = false
        layout.print(terminal.theme.danger(text))
        if (details != null) {
            terminal.println("")
            displayDetailsLine()
            layout.print(muted(details))
        }
    }

    override fun printTime(seconds: Double) {
        state.progressOutput.suspendProgress()
        if (displayLine || displayException)
            terminal.println("")

        val time = DisplayUtils.elapsedSeconds(seconds)
        printWrapped(LanguageController.messages.infoDisplayExecutionTime(time))
    }

    private fun displayDetailsLine() {
        val label = layout.fit(LanguageController.messages.infoLabelOnDetails + " ", layout.width)
        terminal.println(muted(label + rule.repeat((layout.width - layout.cells(label)).coerceAtLeast(0))))
    }
}
