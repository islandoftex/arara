// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.files.ProjectFile

/**
 * Project model for arara
 */
class DSLProject(private val name: String) {
    private val files = mutableListOf<Pair<Path, Int>>()
    private var workingDirectory = FileHandling.normalize(Paths.get(""))
    private val dependencyList = mutableSetOf<String>()

    fun file(name: String, priority: Int = ProjectFile.DEFAULT_PRIORITY) =
            files.add(Paths.get(name) to priority)

    fun file(file: File, priority: Int = ProjectFile.DEFAULT_PRIORITY) =
            files.add(file.toPath() to priority)

    fun workingDirectory(name: String) {
        workingDirectory = FileHandling.normalize(Paths.get(name))
    }

    fun workingDirectory(file: File) {
        workingDirectory = FileHandling.normalize(file.toPath())
    }

    fun dependsOn(vararg dependencies: String) = dependencyList.addAll(dependencies)

    override fun toString(): String {
        return "$name: $workingDirectory, $files, $dependencyList"
    }

    fun toProject(): Project {
        TODO("implement project files")
    }
}
