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

/** JVM staging uses native operations for links, binary copying and atomic state replacement. */
actual object BuildWorkspaces {
    actual fun validate(projects: List<Project>) {
        val roots = projects.mapNotNull { project ->
            project.buildDirectory?.let {
                val root = project.executionDirectory.normalize()
                if (link(root) || physical(project.workingDirectory).startsWith(physical(root))) {
                    invalid(root)
                }
                root
            }
        }
        for ((index, root) in roots.withIndex()) {
            if (roots.drop(index + 1).any { physical(root).startsWith(physical(it)) ||
                    physical(it).startsWith(physical(root)) }) {
                invalid(root)
            }
            if (projects.any { physical(it.workingDirectory).startsWith(physical(root)) }) {
                invalid(root)
            }
        }
    }

    private fun invalid(root: MPPPath): Nothing =
        throw AraraException(messages.errorBuildDirectoryInvalid(root.toString()))

    actual fun prepare(project: Project, excluded: List<MPPPath>, stage: Boolean): Project {
        if (project.buildDirectory == null) return project
        return guarded(project) {
            validate(listOf(project))
            val plan = WorkspacePlan(project, excluded)
            if (stage) WorkspaceStore(project).sync(plan.collect())
            val files = project.files.map { original ->
                object : ProjectFile by original {
                    override val path = plan.mapped(original.sourcePath.normalize())
                    override val sourcePath = original.sourcePath
                }
            }.toSet()
            object : Project by project {
                override val files = files
            }
        }
    }

    actual fun clean(project: Project) {
        if (project.buildDirectory == null) return
        validate(listOf(project))
        guarded(project) {
            val root = project.executionDirectory
            if (present(root)) {
                WorkspaceStore(project).owned()
                root.removeDirectory()
            }
        }
    }

    private fun <T> guarded(project: Project, action: () -> T): T = try {
        action()
    } catch (exception: IOException) {
        throw AraraException(messages.errorBuildDirectoryInvalid(project.executionDirectory.toString()), exception)
    }
}
