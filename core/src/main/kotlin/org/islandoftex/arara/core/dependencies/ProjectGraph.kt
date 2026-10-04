// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.localization.LanguageController

/**
 * A graph of the projects of one run: the projects are the
 * vertices and their dependencies the edges, and the topological
 * walk resolves the execution order.
 */
internal class ProjectGraph : Graph<Project>() {
    /**
     * Add all projects and their dependencies into the graph.
     *
     * @param projects The iterable of projects to add to the graph.
     */
    fun addAll(projects: Iterable<Project>) {
        projects.forEach { project ->
            // separate vertex creation to ensure insertion if dependencies
            // are empty
            addVertex(project)
            project.dependencies.forEach { dependency ->
                addEdge(
                    projects.find { it.name == dependency }
                        ?: throw AraraException(
                            LanguageController.messages
                                .errorProjectStrayDependency(dependency)
                        ),
                    project
                )
            }
        }
    }
}
