// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.utils.formatString
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project

/** Invocation-wide validation, completed before any execution hooks run. */
object ProjectValidation {
    fun executionOrder(projects: List<Project>): List<Project> {
        val order = ProjectGraph().apply { addAll(projects) }.kahn()
        val owners = mutableMapOf<MPPPath, String>()
        projects.forEach { project ->
            project.files.forEach { file ->
                val path = file.path.normalize()
                val previous = owners.put(path, project.name)
                if (previous != null) {
                    throw AraraException(
                        LanguageController.messages.ERROR_PROJECT_FILE_OWNERSHIP
                            .formatString(path.toString(), previous, project.name)
                    )
                }
            }
        }
        return order
    }
}
