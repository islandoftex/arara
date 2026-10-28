// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.parameters.types.path
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.lua.LuaInterpreter

internal class ProjectResolver(
    private val workingDirectory: MPPPath?,
    private val configure: (org.islandoftex.arara.api.files.Project, ConfigurationSnapshot) -> Unit,
    private val applyFlags: () -> Unit
) {
    /** Specifications share a graph; loose inputs form a final anonymous project. */
    fun resolveProjects(reference: List<String>,
        configuration: ConfigurationSnapshot): List<org.islandoftex.arara.api.files.Project> {
        val workingDir = (workingDirectory ?: MPPPath(".")).normalize()
        val (specifications, inputs) = reference.partition { it.endsWith(".proj.lua") }
        val projects = specifications.flatMap { specification ->
            val path = workingDir.resolve(specification)
            LuaInterpreter(workingDir).parseProjectsFromLua(
                path.readText(), path.fileName.removeSuffix(".proj.lua"), path.toString()
            )
        }
        // Specification dependencies refer only to named specification projects.
        org.islandoftex.arara.core.dependencies.ProjectValidation.executionOrder(projects)
        return if (inputs.isEmpty()) projects else projects + resolveLooseInputs(
            inputs, workingDir, configuration, projects.map { it.name }.toSet()
        )
    }

    private fun resolveLooseInputs(
        inputs: List<String>,
        workingDir: MPPPath,
        configuration: ConfigurationSnapshot,
        dependencies: Set<String>
    ): org.islandoftex.arara.api.files.Project {
        val project = Project("Single files", workingDir, emptySet(), dependencies)
        return try {
            configure(project, configuration)
            val files = inputs.map { FileSearching.resolveFile(it, workingDir, LinearExecutor.executionOptions) }
            if (files.map { it.path.normalize() }.toSet().size != files.size) {
                throw AraraException("Loose inputs refer to the same file more than once.")
            }
            Project(project.name, workingDir, files.toSet(), dependencies)
        } finally {
            configuration.restore(configureLogging = false)
            applyFlags()
        }
    }

}
