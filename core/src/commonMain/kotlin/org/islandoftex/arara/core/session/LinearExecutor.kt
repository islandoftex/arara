// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.session.ExecutionReport
import org.islandoftex.arara.api.session.Executor
import org.islandoftex.arara.core.dependencies.ProjectGraph
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

object LinearExecutor : Executor {
    /**
     * Specify custom user hooks to run.
     */
    var hooks = ExecutorHooks()

    /**
     * The project this executor currently executes, if any. This will always
     * be set before the [ExecutorHooks.executeBeforeProject] hook is executed
     * and unset after the [ExecutorHooks.executeAfterProject] hook.
     */
    var currentProject: Project? = null
        private set

    /**
     * The file this executor currently works on, if any. This will always
     * be set after the [ExecutorHooks.executeBeforeFile] hook is executed
     * and unset before the [ExecutorHooks.executeAfterFile] hook.
     */
    var currentFile: ProjectFile? = null
        private set

    /**
     * The setup for all executions run by this executor. The execution options
     * should not change while executing one project.
     */
    @ExperimentalTime
    override var executionOptions: ExecutionOptions =
        org.islandoftex.arara.core.configuration
            .ExecutionOptions()
        set(value) {
            if (currentFile != null) {
                throw AraraException(
                    "Cannot change execution options while " +
                        "executing the file ${currentFile?.path}.",
                )
            }
            if (value.parallelExecution) {
                throw AraraException(
                    "This executor does not support " +
                        "parallel execution.",
                )
            }
            field = value
        }

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
    override fun execute(projects: List<Project>): ExecutionReport {
        val projectsInOrder = ProjectGraph().apply { addAll(projects) }.kahn()
        hooks.executeBeforeExecution()
        val executionStarted = TimeSource.Monotonic.markNow()
        var exitCode = 0
        for (project in projectsInOrder) {
            exitCode = executeProject(project)
            if (executionOptions.haltOnErrors && exitCode != 0) {
                break
            }
        }
        val executionEnded = TimeSource.Monotonic.markNow()
        val executionReport =
            ExecutionReport(executionStarted, executionEnded, exitCode)
        hooks.executeAfterExecution(executionReport)
        return executionReport
    }

    @OptIn(ExperimentalTime::class)
    internal fun executeProject(project: Project): Int {
        var exitCode = 0
        currentProject = project
        hooks.executeBeforeProject(project)
        for (file in project.files.sortedByDescending { it.priority }) {
            hooks.executeBeforeFile(file)
            val executionReport = execute(file)
            exitCode = executionReport.exitCode
            if (executionOptions.haltOnErrors && exitCode != 0) {
                return exitCode
            }
            hooks.executeAfterFile(executionReport)
        }
        hooks.executeAfterProject(project)
        currentProject = null
        return exitCode
    }

    /**
     * Performs arara's main routine to run a file.
     *
     * @param file The file to run.
     */
    @ExperimentalTime
    override fun execute(file: ProjectFile): ExecutionReport {
        currentFile = file
        val executionStarted = TimeSource.Monotonic.markNow()
        val directives =
            hooks.processDirectives(
                file,
                file.fetchDirectives(executionOptions.parseOnlyHeader),
            )
        var exitCode = 0
        for (directive in directives) {
            exitCode = directive.execute()
            if (executionOptions.haltOnErrors && exitCode != 0) {
                break
            }
        }
        currentFile = null
        return ExecutionReport(
            executionStarted,
            TimeSource.Monotonic.markNow(),
            exitCode,
        )
    }
}
