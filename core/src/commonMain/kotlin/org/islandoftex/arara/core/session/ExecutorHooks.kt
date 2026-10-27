// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.session.ExecutionReport
import org.islandoftex.arara.core.files.BuildWorkspaceEvent

/**
 * arara's core executor is configurable at some places by inserting hooks.
 * This is a collection of all hooks that are applicable.
 */
data class ExecutorHooks(
    /** Called after invocation validation, before any project runs. */
    val executeBeforeExecution: () -> Unit = { },
    /** Called with the aggregate report when execution returns normally, including nonzero statuses. */
    val executeAfterExecution: (ExecutionReport) -> Unit = { _ -> },
    /** Called after the current project is set, before its files run. */
    val executeBeforeProject: (Project) -> Unit = { _ -> },
    /** Called after the project file loop, including a stop on a nonzero status; skipped on exceptions. */
    val executeAfterProject: (Project) -> Unit = { _ -> },
    /** Called before the file becomes current and its directives are fetched. */
    val executeBeforeFile: (ProjectFile) -> Unit = { _ -> },
    /** Called with the file report after current-file state is cleared; skipped if file execution throws. */
    val executeAfterFile: (ExecutionReport) -> Unit = { _ -> },
    /** Transforms extracted directives before their execution; also receives the current project file. */
    val processDirectives: (ProjectFile, List<Directive>) -> List<Directive> = { _, l -> l },
    /** Workspace operations, including validation, preparation metrics and failures. */
    val buildWorkspace: (BuildWorkspaceEvent) -> Unit = { },
    /** Cooperative stop check between commands and files, used after forced watch interruption. */
    val shouldStopExecution: () -> Boolean = { false }
)
