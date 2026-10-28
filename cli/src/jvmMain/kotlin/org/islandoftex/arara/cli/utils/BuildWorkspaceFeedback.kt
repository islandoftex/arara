// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.util.Locale
import kotlin.time.DurationUnit
import org.islandoftex.arara.api.files.BuildWorkspaceEvent
import org.islandoftex.arara.api.files.BuildWorkspaceOperation
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session

/** Workspace status is separate from rule tasks and their execution summary. */
internal fun reportBuildWorkspace(event: BuildWorkspaceEvent) {
    val directory = event.project.executionDirectory.toString()
    val operation = event.operation.name.lowercase()
    val fields = mutableMapOf<String, Any>(
        "project" to event.project.name, "directory" to directory, "operation" to operation)
    val messages = LanguageController.messages
    when (event) {
        is BuildWorkspaceEvent.Started -> {
            EventLog.info("build_directory.started", "Build directory $operation started: $directory", fields)
            val text = when (event.operation) {
                BuildWorkspaceOperation.VALIDATE -> messages.infoBuildDirectoryValidating(directory)
                BuildWorkspaceOperation.PREPARE -> messages.infoBuildDirectoryPreparing(directory)
                BuildWorkspaceOperation.CLEAN -> messages.infoBuildDirectoryCleaning(directory)
            }
            DisplayUtils.renderer.startBuildDirectory(text)
        }
        is BuildWorkspaceEvent.Finished -> {
            fields["durationMs"] = event.elapsed.inWholeMilliseconds
            fields["copiedFiles"] = event.copiedFiles
            fields["success"] = event.success
            val outcome = if (event.success) "completed" else "failed"
            val message = "Build directory $operation $outcome: $directory; " +
                "copied ${event.copiedFiles} input files in ${event.elapsed.inWholeMilliseconds} ms"
            if (event.success) EventLog.info("build_directory.completed", message, fields)
            else EventLog.error("build_directory.failed", message, fields)
            DisplayUtils.renderer.finishBuildDirectory(workspaceResult(event))
        }
    }
}

private fun workspaceResult(event: BuildWorkspaceEvent.Finished): String {
    val messages = LanguageController.messages
    if (!event.success) return messages.errorBuildDirectoryOperationFailed
    return when (event.operation) {
        BuildWorkspaceOperation.VALIDATE -> messages.infoBuildDirectoryValidated
        BuildWorkspaceOperation.CLEAN -> messages.infoBuildDirectoryCleaned
        BuildWorkspaceOperation.PREPARE -> {
            val seconds = String.format(Locale.ROOT, "%.3f", event.elapsed.toDouble(DurationUnit.SECONDS))
                .replace('.', Session.userInterfaceOptions.locale.decimalSeparator)
            messages.infoBuildDirectoryPrepared(event.copiedFiles, seconds)
        }
    }
}
