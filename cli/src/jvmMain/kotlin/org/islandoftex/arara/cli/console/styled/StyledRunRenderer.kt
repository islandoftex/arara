// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.reporting.FileSummary
import org.islandoftex.arara.api.reporting.RunSummary
import org.islandoftex.arara.api.reporting.WatchSummary
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.ui.LocalizedSummaries

/** Focused styled presentation component. */
internal class StyledRunRenderer(state: StyledRenderState) : StyledComponent(state), LocalizedSummaries {
    override fun printProjectInformation(name: String, directory: String) {
        state.progressOutput.suspendProgress()
        layout.print("${LanguageController.messages.infoLabelProject} $name ($directory)")
        terminal.println("")
    }

    override fun printWatchSummary(summary: WatchSummary) {
        state.progressOutput.suspendProgress()
        StyledWatchRenderer(terminal, layout).print(summary)
    }

    override fun printRunSummary(summary: RunSummary) {
        state.progressOutput.suspendProgress()
        state.details.closeOutputFrame()
        StyledSummaryRenderer(terminal, layout).print(summary)
    }

    override fun printRunSummary(success: Boolean, files: List<Pair<String, Int?>>) = printRunSummary(
        RunSummary(success, isDryRunMode, files.map { (path, code) ->
            val file = MPPPath(path)
            FileSummary(file, "", file.parent, exitCode = code)
        })
    )
}
