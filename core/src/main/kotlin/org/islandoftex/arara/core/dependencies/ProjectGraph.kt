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
        projects.forEach { project ->
            project.dependencies.forEach { dependency ->
                addEdge(project, projects.find { it.name == dependency }
                        ?: throw AraraException("Stray project dependencies"))
            }
        }
    }
}
