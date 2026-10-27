// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.EventLog

internal class WatchReporter {
    fun stopping() {
        System.err.println("Stopping watch mode after the current build. Press Ctrl+C again to stop active tools.")
    }

    fun waiting() = DisplayUtils.renderer.printWrapped("Watching for changes. Press Ctrl+C to stop.")

    fun starting(number: Int, projects: Set<String>) {
        DisplayUtils.renderer.printWrapped("Watch build $number: ${projects.joinToString(", ")}")
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
        DisplayUtils.renderer.printWrapped("Watch configuration is invalid; builds are paused until it is corrected.")
    }

    fun problem(message: String) {
        DisplayUtils.renderer.printWrapped("Watch input unavailable: $message")
        EventLog.warn("watch.input.unavailable", "Watch input unavailable: $message", mapOf("path" to message))
    }

    fun summary(runs: List<WatchRun>, code: Int) {
        DisplayUtils.renderer.printWrapped("Watch session: ${runs.size} builds; exit code $code.")
        runs.forEach {
            DisplayUtils.renderer.printWrapped("${it.number}: ${it.projects.joinToString(", ")} — " +
                "${it.exitCode}, ${it.elapsed.inWholeMilliseconds} ms")
        }
    }
}
