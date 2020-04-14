// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import java.io.File
import java.nio.file.Paths
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.files.FileHandling

/**
 * A project model class to capture DSL methods within.
 */
class DSLProject(private val name: String) {
    private val files = mutableSetOf<org.islandoftex.arara.api.files.ProjectFile>()
    private var workingDirectory = FileHandling.normalize(Paths.get(""))
    private val dependencyList = mutableSetOf<String>()

    /**
     * Add a file by name.
     *
     * @param name The file's name. Will be resolved against the working
     *   directory in the end.
     * @param priority The file's priority within the project. If you do not
     *   want to set this, choose the configuration builder variant of this
     *   method.
     */
    fun file(name: String, priority: Int) = file(name) { this.priority = priority }

    /**
     * Add a file by name.
     *
     * @param name The file's name. Will be resolved against the working
     *   directory in the end.
     * @param configure Configure the [DSLProjectFile] object. You may add
     *   directives manually or specify the priority.
     */
    fun file(name: String, configure: DSLProjectFile.() -> Unit = {}) =
            files.add(
                    DSLProjectFile(Paths.get(name))
                            .apply(configure).toProjectFile()
            )

    /**
     * Add a file.
     *
     * @param file The file object. Will be resolved against the working
     *   directory in the end.
     * @param priority The file's priority within the project. If you do not
     *   want to set this, choose the configuration builder variant of this
     *   method.
     */
    fun file(file: File, priority: Int) = file(file) { this.priority = priority }

    /**
     * Add a file.
     *
     * @param file The file object. Will be resolved against the working
     *   directory in the end.
     * @param configure Configure the [DSLProjectFile] object. You may add
     *   directives manually or specify the priority.
     */
    fun file(file: File, configure: DSLProjectFile.() -> Unit) =
            files.add(
                    DSLProjectFile(file.toPath())
                            .apply(configure).toProjectFile()
            )

    /**
     * Set the project's working directory.
     *
     * @param name The name of the working directory. Will be resolved against
     *   the application's working directory.
     */
    fun workingDirectory(name: String) {
        workingDirectory = FileHandling.normalize(Paths.get(name))
    }

    /**
     * Set the project's working directory.
     *
     * @param file The file object of the working directory. Will be resolved
     *   against the application's working directory.
     */
    fun workingDirectory(file: File) {
        workingDirectory = FileHandling.normalize(file.toPath())
    }

    /**
     * Add dependencies to the project.
     *
     * Multiple calls are accumulating dependencies.
     *
     * @param dependencies The names of other projects this project depends on.
     *   Order does not matter, arara uses graph resolution.
     */
    fun dependsOn(vararg dependencies: String) = dependencyList.addAll(dependencies)

    /**
     * String representation of the project.
     */
    override fun toString(): String {
        return "DSLProject(name=$name, workingDirectory=$workingDirectory, " +
                "files=$files, dependencies=$dependencyList)"
    }

    /**
     * Turn this DSL object into arara's core object.
     *
     * @return A [Project] resembling the user's configuration.
     */
    internal fun toProject(): Project = org.islandoftex.arara.core.files.Project(
            name, workingDirectory, files, dependencyList
    )
}
