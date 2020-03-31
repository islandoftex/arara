// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.project

import java.io.File

/**
 * A project in arara's sense is a collection of files that should be compiled
 * together. Files in a project are not dependent on each other.
 */
data class Project(
    val name: String,
    val workingDirectory: File,
    val files: List<ProjectFile>,
    val dependencies: Set<String> = setOf()
) {
    internal val absoluteFiles = files.map {
        if (it.isAbsolute)
            it
        else
            it.copy(path = workingDirectory.toPath().resolve(it).toAbsolutePath())
    }
}
