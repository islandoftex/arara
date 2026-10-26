// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project

/** Staging and safe deletion of isolated project build directories. */
expect object BuildWorkspaces {
    /** Reject overlapping workspaces before executing any project. */
    fun validate(projects: List<Project>, observer: (BuildWorkspaceEvent) -> Unit = { })

    /** Stage inputs after dependencies finish, or only map paths when [stage] is false. */
    fun prepare(
        project: Project, excluded: List<MPPPath>, stage: Boolean = true,
        observer: (BuildWorkspaceEvent) -> Unit = { }
    ): Project

    /** Remove a workspace only when its ownership marker matches the project. */
    fun clean(project: Project, observer: (BuildWorkspaceEvent) -> Unit = { })
}
