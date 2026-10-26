// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files.workspace

import kotlin.time.TimeSource
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.files.BuildWorkspaceEvent
import org.islandoftex.arara.core.files.BuildWorkspaceOperation

internal class WorkspaceActivity {
    var copiedFiles = 0
        private set

    fun copiedFile() { copiedFiles++ }
}

internal fun <T> trackWorkspace(
    project: Project, operation: BuildWorkspaceOperation, observer: (BuildWorkspaceEvent) -> Unit,
    action: (WorkspaceActivity) -> T
): T {
    val started = TimeSource.Monotonic.markNow()
    val activity = WorkspaceActivity()
    observer(BuildWorkspaceEvent.Started(project, operation))
    var success = false
    try {
        val result = action(activity)
        success = true
        return result
    } finally {
        observer(BuildWorkspaceEvent.Finished(project, operation, success, started.elapsedNow(), activity.copiedFiles))
    }
}
