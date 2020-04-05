// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.model

import java.nio.file.Path
import org.islandoftex.arara.files.Project
import org.islandoftex.arara.files.ProjectFile

/**
 * A project in arara's sense is a collection of files that should be compiled
 * together. Files in a project are not dependent on each other.
 */
data class ProjectImpl(
    override val name: String,
    override val workingDirectory: Path,
    override val files: Set<ProjectFile>,
    override val dependencies: Set<String> = setOf()
) : Project {
    /**
     * Get the files of a project as absolute paths.
     */
    val absoluteFiles: Set<ProjectFile>
        get() = files.map {
            if (it.path.isAbsolute)
                it
            else
                ProjectFileImpl(
                        workingDirectory.resolve(it.path).toRealPath(),
                        it.fileType,
                        it.priority
                )
        }.toSet()

    /**
     * Get the project's files in order of compilation.
     */
    val filesByPriority: List<ProjectFile>
        get() = files.sortedBy {
            it.priority
        }
}
