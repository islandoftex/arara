// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.reporting.WatchRun
import org.islandoftex.arara.api.reporting.WatchSummary
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.EventLog
import org.islandoftex.arara.core.localization.LanguageController

internal class WatchReporter {
    fun stopping() {
        System.err.println(LanguageController.messages.infoWatchStopping)
    }

    fun waiting() = DisplayUtils.renderer.printWrapped(LanguageController.messages.infoWatchWaiting)

    fun starting(number: Int, projects: Set<String>) {
        DisplayUtils.renderer.printWrapped(
            LanguageController.messages.infoWatchStarting(number, projects.joinToString(", ")))
    }

    fun completed(run: WatchRun) {
        val fields = mapOf("cycle" to run.number, "projects" to run.projects, "exitCode" to run.exitCode,
            "durationMs" to run.elapsed.inWholeMilliseconds)
        if (run.exitCode == 0) EventLog.info("watch.cycle.completed", "Watch build ${run.number} completed", fields)
        else EventLog.error("watch.cycle.failed", "Watch build ${run.number} failed", fields)
    }

    fun failure(exception: AraraException) = DisplayUtils.printException(exception)

    fun invalid(exception: AraraException) {
        DisplayUtils.printException(exception)
        DisplayUtils.renderer.printWrapped(LanguageController.messages.infoWatchPaused)
    }

    fun problem(message: String) {
        DisplayUtils.renderer.printWrapped(LanguageController.messages.warningWatchInput(message))
        EventLog.warn("watch.input.unavailable", "Watch input unavailable: $message", mapOf("path" to message))
    }

    fun summary(runs: List<WatchRun>, code: Int) {
        DisplayUtils.renderer.printWatchSummary(WatchSummary(runs.toList(), code))
    }
}
