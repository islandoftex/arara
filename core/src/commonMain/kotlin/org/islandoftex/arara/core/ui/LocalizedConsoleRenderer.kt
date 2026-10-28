// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.reporting.WatchSummary
import org.islandoftex.arara.api.ui.ConsoleRenderer
import org.islandoftex.arara.api.ui.ConsoleSummaries
import org.islandoftex.arara.api.ui.ConsoleTaskDetails
import org.islandoftex.arara.core.localization.LanguageController

/** Console defaults using the runtime-selected catalog, without binding the API to global runtime state. */
interface LocalizedConsoleRenderer : ConsoleRenderer, LocalizedTaskDetails, LocalizedSummaries

/** Localized command simulation text; concrete renderers own task formatting. */
interface LocalizedTaskDetails : ConsoleTaskDetails {
    /** Present a simulated command in the selected language. */
    override fun printDryRunCommand(command: String): Unit =
        printWrapped(LanguageController.messages.infoInterpreterDryRunModeSystemCommand(command))
}

/** Localized fallback for hosts that do not supply a watch table. */
interface LocalizedSummaries : ConsoleSummaries {
    /** Present completed watch cycles and the final status using the selected language. */
    override fun printWatchSummary(summary: WatchSummary) {
        val messages = LanguageController.messages
        printWrapped(messages.infoWatchSummary(summary.runs.size, summary.exitCode))
        summary.runs.forEach { run ->
            val status = if (run.exitCode == 0) messages.infoLabelOnSuccess else messages.infoLabelOnFailure
            printWrapped("${run.number}: ${run.projects.joinToString(", ")} · " +
                "$status (${run.exitCode}) · ${run.elapsed.inWholeMilliseconds} ms")
        }
    }
}
