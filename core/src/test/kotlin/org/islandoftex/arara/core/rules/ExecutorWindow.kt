// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import java.nio.file.Files
import java.nio.file.Path
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile as APIProjectFile
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Drives the linear executor through a throwaway project so tests can
 * exercise the helpers that read the executor state. The observation
 * hook runs once per project file, in the exact lifecycle window in
 * which real directives execute, with both the current project and the
 * current file set - the same view a rule helper has during an actual
 * run. The files are directive-free, so the executor only walks its
 * lifecycle; the hook observes and records.
 */
internal fun executeWithinProject(
    files: List<Pair<String, Int>>,
    work: (directory: Path, file: APIProjectFile) -> Unit
) {
    val directory = Files.createTempDirectory("arara-helper-window")
    val projectFiles = files.map { (name, priority) ->
        val path = directory.resolve(name)
        Files.writeString(path, "no directives in this window\n")
        ProjectFile(MPPPath(path.toString()), FileType("tex", "^%"), priority)
    }
    val project = Project(
        "window", MPPPath(directory.toString()), projectFiles.toSet()
    )
    val originalHooks = LinearExecutor.hooks
    LinearExecutor.hooks = ExecutorHooks(
        processDirectives = { file, directives ->
            work(directory, file)
            directives
        }
    )
    try {
        LinearExecutor.executeProject(project)
    } finally {
        LinearExecutor.hooks = originalHooks
    }
}
