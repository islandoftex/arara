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
    /**
     * Runs once, before the whole execution.
     */
    val executeBeforeExecution: () -> Unit = { },
    /**
     * Runs once, after the whole execution, with its report.
     */
    val executeAfterExecution: (ExecutionReport) -> Unit = { _ -> },
    /**
     * Runs before every project, with the project.
     */
    val executeBeforeProject: (Project) -> Unit = { _ -> },
    /**
     * Runs after every project, with the project.
     *
     * A project the run abandons mid-file - a failing file under
     * the error-halt option, or a halt request with a failing
     * status - never reaches this hook: the stop leaves the file
     * loop directly.
     */
    val executeAfterProject: (Project) -> Unit = { _ -> },
    /**
     * Runs before every file, with the file.
     */
    val executeBeforeFile: (ProjectFile) -> Unit = { _ -> },
    /**
     * Runs after every file, with the report of the run so far.
     *
     * A file the run stops at - a failing task under the error-halt
     * option, or a halt request with a failing status - never
     * reaches this hook: the stop returns before the closing hook
     * runs.
     */
    val executeAfterFile: (ExecutionReport) -> Unit = { _ -> },
    /**
     * Processes the directives of every file, returning the list to
     * execute; preambles and validation plug in here.
     */
    val processDirectives: (ProjectFile, List<Directive>) -> List<Directive> = { _, l -> l }
)
