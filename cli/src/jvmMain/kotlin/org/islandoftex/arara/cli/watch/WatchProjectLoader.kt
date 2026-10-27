// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.cli.configuration.ConfigurationFiles
import org.islandoftex.arara.core.dependencies.ProjectValidation
import org.islandoftex.arara.core.files.BuildWorkspaces
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/** Resolve configuration without opening log destinations or staging inputs during graph validation. */
internal class WatchProjectLoader(
    private val specifications: Set<MPPPath>, private val projects: () -> List<Project>,
    private val configure: (Project) -> Unit, private val restore: () -> Unit
) {
    fun load(): WatchGraph {
        val resolved = projects()
        val controls = specifications.toMutableSet()
        val rules = mutableSetOf<MPPPath>()
        val excluded = mutableSetOf<MPPPath>()
        try {
            resolved.forEach { project ->
                configure(project)
                controls += ConfigurationFiles.candidatesForProject(project)
                rules += LinearExecutor.executionOptions.rulePaths.map {
                    project.executionDirectory.resolve(it).normalize()
                }
                excluded += LinearExecutor.executionOptions.databaseName.normalize()
                excluded += Session.loggingOptions.logFile.normalize()
            }
        } finally {
            restore()
        }
        ProjectValidation.executionOrder(resolved)
        BuildWorkspaces.validate(resolved)
        // Validate wildcard syntax and collect native registrations before accepting the replacement graph.
        return WatchGraph(resolved, controls, rules, excluded).also { it.observe() }
    }
}
