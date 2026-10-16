// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.files.BuildDirectory
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile

/**
 * Implements the project model: a named set of files sharing a working
 * directory, possibly depending on other projects by name.
 */
data class Project(
    /**
     * The name of the project.
     */
    override val name: String,
    /**
     * The directory all paths of the project resolve against.
     */
    override val workingDirectory: MPPPath,
    /**
     * The files of the project, with their priorities.
     */
    override val files: Set<ProjectFile>,
    /**
     * The names of the projects this project depends on.
     */
    override val dependencies: Set<String> = setOf()
) : Project
