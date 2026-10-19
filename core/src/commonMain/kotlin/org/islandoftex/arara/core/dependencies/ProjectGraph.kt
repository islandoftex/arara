// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Project

/**
 * A graph of projects. Implemented for convenience.
 */
internal class ProjectGraph : Graph<Project>({ it.name }) {
    /**
     * Add all projects and their dependencies into the graph.
     *
     * @param projects The iterable of projects to
     */
    fun addAll(projects: Iterable<Project>) {
        val ordered = projects.sortedBy { it.name }
        val byName = ordered.associateBy { it.name }
        if (byName.size != ordered.size) {
            val duplicates = ordered.groupBy { it.name }.filterValues { it.size > 1 }.keys.joinToString(", ")
            throw AraraException("Project names must be unique: $duplicates.")
        }
        ordered.forEach { addVertex(it) }
        ordered.forEach { project ->
            project.dependencies.sorted().forEach { dependency ->
                addEdge(
                    byName[dependency]
                        ?: throw AraraException("Project '${project.name}' has unknown dependency '$dependency'."),
                    project
                )
            }
        }
    }
}
