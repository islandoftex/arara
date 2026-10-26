// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.files.BuildDirectory
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile

/** A named collection of files, a working directory and dependencies on other projects. */
data class Project(
    /** Invocation-wide project identifier. */
    override val name: String,
    /** Base directory for project-relative paths and commands. */
    override val workingDirectory: MPPPath,
    /** Files owned by this project; invocation validation rejects repeated paths. */
    override val files: Set<ProjectFile>,
    /** Names of projects that must run before this project. */
    override val dependencies: Set<String> = setOf(),
    /** Optional input staging and execution workspace. */
    override val buildDirectory: BuildDirectory? = null
) : Project
