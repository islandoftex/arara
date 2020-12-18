// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

import kotlin.time.ExperimentalTime
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile

/**
 * An [Executor] is arara's core and performs the execution of directive
 * resolution and rule execution.
 */
public interface Executor {
    /**
     * The setup for all executions run by this executor. The execution options
     * should not change while executing one project.
     */
    @ExperimentalTime
    public var executionOptions: ExecutionOptions

    /**
     * Execute rules based on the projects. Should roughly implement the
     * following steps:
     *
     * 1. Resolve project dependencies and create an appropriate execution
     *    order. Pay attention to blocking when doing parallel execution.
     * 2. Run arara's main routines on each project. Within a project,
     *    follow the file priorities.
     * 3. Wait for all tasks to finish.
     *
     * @param projects The projects to act on.
     */
    @ExperimentalTime
    public fun execute(projects: List<Project>): ExecutionReport

    /**
     * Performs arara's main routine to run a file.
     *
     * @param file The file to run.
     */
    @ExperimentalTime
    public fun execute(file: ProjectFile): ExecutionReport
}
