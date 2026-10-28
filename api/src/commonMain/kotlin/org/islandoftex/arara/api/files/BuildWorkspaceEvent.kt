// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import kotlin.time.Duration
import org.islandoftex.arara.api.files.Project

/** Workspace work reported separately from rule tasks. */
public enum class BuildWorkspaceOperation {
    /** Validate workspace locations before execution. */
    VALIDATE,
    /** Select, hash and stage project inputs. */
    PREPARE,
    /** Remove an owned workspace. */
    CLEAN
}

/** Invocation-local progress and measurements for a workspace operation. */
public sealed interface BuildWorkspaceEvent {
    /** Project owning this workspace. */
    public val project: Project
    /** Operation being reported. */
    public val operation: BuildWorkspaceOperation

    /** An operation has started; a matching [Finished] event retires its presentation. */
    public data class Started(
        /** Project owning this workspace. */
        override val project: Project,
        /** Operation being reported. */
        override val operation: BuildWorkspaceOperation
    ) : BuildWorkspaceEvent

    /** Completion, including partial work when an operation fails. */
    public data class Finished(
        /** Project owning this workspace. */
        override val project: Project,
        /** Operation being reported. */
        override val operation: BuildWorkspaceOperation,
        /** Whether the operation returned normally. */
        public val success: Boolean,
        /** Total monotonic time, including input selection, hashing, copying and state persistence. */
        public val elapsed: Duration,
        /** Regular files actually copied; unchanged files and symbolic links are not counted. */
        public val copiedFiles: Int
    ) : BuildWorkspaceEvent
}
