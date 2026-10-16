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
internal class ProjectGraph : Graph<Project>({ it.name }) {
    /**
     * Add all projects and their dependencies into the graph.
     *
     * @param projects The iterable of projects to add to the graph.
     */
    fun addAll(projects: Iterable<Project>) {
        val ordered = projects.sortedBy { it.name }
        val byName = ordered.associateBy { it.name }
        if (byName.size != ordered.size) {
            val duplicates = ordered.groupBy { it.name }.filterValues { it.size > 1 }.keys.joinToString(", ")
            throw AraraException(LanguageController.messages.errorProjectDuplicateNames(duplicates))
        }
        ordered.forEach { addVertex(it) }
        ordered.forEach { project ->
            project.dependencies.sorted().forEach { dependency ->
                addEdge(
                    projects.find { it.name == dependency }
                        ?: throw AraraException(
                            // the enumeration shape joins the merge
                            // validation's, so both sites render the
                            // verdict the same way
                            LanguageController.messages
                                .errorProjectStrayDependency("($dependency)")
                        ),
                    project
                )
            }
        }
    }
}
