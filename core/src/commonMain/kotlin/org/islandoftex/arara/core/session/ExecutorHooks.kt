// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.session.ExecutionReport

/**
 * arara's core executor is configurable at some places by inserting hooks.
 * This is a collection of all hooks that are applicable.
 */
data class ExecutorHooks(
    val executeBeforeExecution: () -> Unit = { },
    val executeAfterExecution: (ExecutionReport) -> Unit = { _ -> },
    val executeBeforeProject: (Project) -> Unit = { _ -> },
    val executeAfterProject: (Project) -> Unit = { _ -> },
    val executeBeforeFile: (ProjectFile) -> Unit = { _ -> },
    val executeAfterFile: (ExecutionReport) -> Unit = { _ -> },
    val processDirectives: (
        ProjectFile,
        List<Directive>,
    ) -> List<Directive> = { _, l -> l },
)
