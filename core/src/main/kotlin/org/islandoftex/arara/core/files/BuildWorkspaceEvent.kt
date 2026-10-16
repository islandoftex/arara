// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import kotlin.time.Duration
import org.islandoftex.arara.api.files.Project

/** Workspace work reported separately from rule tasks. */
enum class BuildWorkspaceOperation {
    /** Validate workspace locations before execution. */
    VALIDATE,
    /** Select, hash and stage project inputs. */
    PREPARE,
    /** Remove an owned workspace. */
    CLEAN
}

/** Invocation-local progress and measurements for a workspace operation. */
sealed interface BuildWorkspaceEvent {
    /** Project owning this workspace. */
    val project: Project
    /** Operation being reported. */
    val operation: BuildWorkspaceOperation

    /** An operation has started; a matching [Finished] event retires its presentation. */
    data class Started(
        /** Project owning this workspace. */
        override val project: Project,
        /** Operation being reported. */
        override val operation: BuildWorkspaceOperation
    ) : BuildWorkspaceEvent

    /** Completion, including partial work when an operation fails. */
    data class Finished(
        /** Project owning this workspace. */
        override val project: Project,
        /** Operation being reported. */
        override val operation: BuildWorkspaceOperation,
        /** Whether the operation returned normally. */
        val success: Boolean,
        /** Total monotonic time, including input selection, hashing, copying and state persistence. */
        val elapsed: Duration,
        /** Regular files actually copied; unchanged files and symbolic links are not counted. */
        val copiedFiles: Int
    ) : BuildWorkspaceEvent
}
