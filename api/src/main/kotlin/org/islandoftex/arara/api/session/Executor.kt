// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile

/**
 * A [ExecutionReport] is intended to present the result of a completed
 * execution.
 */
interface ExecutionReport {
    /**
     * When the execution started. This should be measured after all
     * initialization work (logging etc.) has been done to really measure
     * the execution with lowest overhead.
     */
    @ExperimentalTime
    val executionStarted: TimeMark

    /**
     * When the execution stopped. This should be measured before cleaning up
     * and making nice output to only measure the execution with the lowest
     * overhead.
     */
    @ExperimentalTime
    val executionStopped: TimeMark

    /**
     * The exit code reported by the execution. It will propagate the first
     * error exit code on [ExecutionOptions.haltOnErrors], the last error exit
     * code if [ExecutionOptions.haltOnErrors] is falsy and the success code
     * `0` if the run completes successfully.
     */
    val exitCode: Int
}

/**
 * An [Executor] is arara's core and performs the execution of directive
 * resolution and rule execution.
 */
interface Executor {
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
     * @param executionOptions The setup for all runs and the executor.
     */
    @ExperimentalTime
    fun execute(
        projects: List<Project>,
        executionOptions: ExecutionOptions
    ): ExecutionReport

    /**
     * Performs arara's main routine to run a file.
     *
     * @param file The file to run.
     */
    @ExperimentalTime
    fun execute(
        file: ProjectFile,
        executionOptions: ExecutionOptions
    ): ExecutionReport
}
