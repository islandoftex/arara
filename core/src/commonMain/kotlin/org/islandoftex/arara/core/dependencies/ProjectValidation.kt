// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.localization.LanguageController

/** Invocation-wide validation, completed before any execution hooks run. */
object ProjectValidation {
    /**
     * Validate project dependencies and unique file ownership, then return a stable execution order.
     * Throws [AraraException] for invalid names, missing dependencies, cycles or repeated file paths.
     */
    fun executionOrder(projects: List<Project>): List<Project> {
        val order = ProjectGraph().apply { addAll(projects) }.kahn()
        val owners = mutableMapOf<MPPPath, String>()
        projects.forEach { project ->
            project.files.forEach { file ->
                val path = file.path.normalize()
                val previous = owners.put(path, project.name)
                if (previous != null) {
                    throw AraraException(
                        LanguageController.messages.errorProjectFileOwnership(path.toString(), previous, project.name)
                    )
                }
            }
        }
        return order
    }
}
