// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import org.islandoftex.arara.cli.console.ConsoleSummaries
import org.islandoftex.arara.cli.console.RunSummary
import org.islandoftex.arara.core.localization.LanguageController

/** Focused plain presentation component. */
internal class PlainSummaryRenderer(state: PlainRenderState) : PlainComponent(state), ConsoleSummaries {
    override fun printRunSummary(summary: RunSummary) {
        if (summary.files.isEmpty()) return
        val messages = LanguageController.messages
        val tasks = summary.files.sumOf { it.tasks }
        val reached = summary.files.count { it.exitCode != null }
        if (displayException) println()
        printWrapped(when {
            summary.dryRun -> messages.infoDisplayDryRun(tasks)
            summary.success -> messages.infoDisplayTotalSuccess(reached, tasks)
            else -> messages.infoDisplayTotalFailure(reached, tasks)
        })
        if (summary.dryRun && !summary.success) printWrapped(messages.infoLabelOnFailure)
        val pending = summary.files.filter { it.exitCode == null }
        if (pending.isNotEmpty()) {
            printWrapped("${messages.infoConsoleUnprocessed}: ${pending.joinToString(", ") { it.path.toString() }}")
        }
        displaySummary = true
    }

    override fun printProjectInformation(name: String, directory: String, multipleProjects: Boolean) {
        if (multipleProjects) {
            val project = if (name.isBlank()) directory else "$name ($directory)"
            printWrapped("${LanguageController.messages.infoLabelProject} $project")
            println()
        }
    }
}
