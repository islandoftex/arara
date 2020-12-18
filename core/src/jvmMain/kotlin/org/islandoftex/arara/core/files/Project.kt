// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Path
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile

data class Project(
    override val name: String,
    override val workingDirectory: Path,
    override val files: Set<ProjectFile>,
    override val dependencies: Set<String> = setOf()
) : Project
