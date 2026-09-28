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
     */
    val executeAfterProject: (Project) -> Unit = { _ -> },
    /**
     * Runs before every file, with the file.
     */
    val executeBeforeFile: (ProjectFile) -> Unit = { _ -> },
    /**
     * Runs after every file, with the report of the run so far.
     */
    val executeAfterFile: (ExecutionReport) -> Unit = { _ -> },
    /**
     * Processes the directives of every file, returning the list to
     * execute; preambles and validation plug in here.
     */
    val processDirectives: (ProjectFile, List<Directive>) -> List<Directive> = { _, l -> l }
)
