// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import java.io.File
import java.nio.file.Paths
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.files.FileHandling

/**
 * Project model for arara
 */
class DSLProject(private val name: String) {
    private val files = mutableSetOf<org.islandoftex.arara.api.files.ProjectFile>()
    private var workingDirectory = FileHandling.normalize(Paths.get(""))
    private val dependencyList = mutableSetOf<String>()

    fun file(name: String, priority: Int) = file(name) { this.priority = priority }
    fun file(name: String, configure: DSLProjectFile.() -> Unit = {}) =
            files.add(
                    DSLProjectFile(Paths.get(name))
                            .apply(configure).toProjectFile()
            )

    fun file(file: File, priority: Int) = file(file) { this.priority = priority }
    fun file(file: File, configure: DSLProjectFile.() -> Unit) =
            files.add(
                    DSLProjectFile(file.toPath())
                            .apply(configure).toProjectFile()
            )

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

    fun toProject(): Project = org.islandoftex.arara.core.files.Project(
            name, workingDirectory, files, dependencyList
    )
}
