// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.io.IOException
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.files.workspace.WorkspacePlan
import org.islandoftex.arara.core.files.workspace.WorkspaceStore
import org.islandoftex.arara.core.files.workspace.link
import org.islandoftex.arara.core.files.workspace.messages
import org.islandoftex.arara.core.files.workspace.physical
import org.islandoftex.arara.core.files.workspace.present
import org.islandoftex.arara.core.files.workspace.trackWorkspace

/** JVM staging uses native operations for links, binary copying and atomic state replacement. */
actual object BuildWorkspaces {
    actual fun validate(projects: List<Project>, observer: (BuildWorkspaceEvent) -> Unit) {
        val enabled = projects.filter { it.buildDirectory != null }
        for ((index, project) in enabled.withIndex()) trackWorkspace(
            project, BuildWorkspaceOperation.VALIDATE, observer
        ) {
            guarded(project) {
                validateLocation(project, projects, enabled.filterIndexed { other, _ -> other != index })
            }
        }
    }

    private fun validateLocation(project: Project, projects: List<Project>, others: List<Project>) {
        val root = project.executionDirectory.normalize()
        if (link(root) || projects.any { physical(it.workingDirectory).startsWith(physical(root)) }) invalid(root)
        val overlaps = others.any {
            physical(root).startsWith(physical(it.executionDirectory)) ||
                physical(it.executionDirectory).startsWith(physical(root))
        }
        if (overlaps) invalid(root)
    }

    private fun invalid(root: MPPPath): Nothing =
        throw AraraException(messages.errorBuildDirectoryInvalid(root.toString()))

    actual fun prepare(
        project: Project, excluded: List<MPPPath>, stage: Boolean, observer: (BuildWorkspaceEvent) -> Unit
    ): Project {
        if (project.buildDirectory == null) return project
        return if (!stage) guarded(project) {
            validate(listOf(project))
            mappedProject(project, WorkspacePlan(project, excluded))
        } else trackWorkspace(project, BuildWorkspaceOperation.PREPARE, observer) { activity ->
            guarded(project) {
                validate(listOf(project))
                val plan = WorkspacePlan(project, excluded)
                WorkspaceStore(project).sync(plan.collect(), activity::copiedFile)
                mappedProject(project, plan)
            }
        }
    }

    private fun mappedProject(project: Project, plan: WorkspacePlan): Project {
        val files = project.files.map { original ->
            object : ProjectFile by original {
                override val path = plan.mapped(original.sourcePath.normalize())
                override val sourcePath = original.sourcePath
            }
        }.toSet()
        return object : Project by project {
            override val files = files
        }
    }

    actual fun clean(project: Project, observer: (BuildWorkspaceEvent) -> Unit) {
        if (project.buildDirectory == null) return
        trackWorkspace(project, BuildWorkspaceOperation.CLEAN, observer) {
            validate(listOf(project))
            guarded(project) {
                val root = project.executionDirectory
                if (present(root)) {
                    WorkspaceStore(project).owned()
                    root.removeDirectory()
                }
            }
        }
    }

    private fun <T> guarded(project: Project, action: () -> T): T = try {
        action()
    } catch (exception: IOException) {
        throw AraraException(messages.errorBuildDirectoryInvalid(project.executionDirectory.toString()), exception)
    }
}
