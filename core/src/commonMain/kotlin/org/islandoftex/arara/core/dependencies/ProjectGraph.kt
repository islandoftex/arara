// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Project

/**
 * A graph of projects. Implemented for convenience.
 */
internal class ProjectGraph : Graph<Project>() {
    /**
     * Add all projects and their dependencies into the graph.
     *
     * @param projects The iterable of projects to
     */
    fun addAll(projects: Iterable<Project>) {
        val ordered = projects.sortedBy { it.name }
        val byName = ordered.associateBy { it.name }
        if (byName.size != ordered.size) throw AraraException("Project names must be unique.")
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
