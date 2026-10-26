// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.session.ExecutionReport
import org.islandoftex.arara.api.session.Executor
import org.islandoftex.arara.core.dependencies.ProjectValidation
import org.islandoftex.arara.core.files.BuildWorkspaces

/**
 * Executes projects and their files sequentially through [ExecutorHooks].
 * Validation precedes execution; higher file priorities run first, with normalized paths breaking ties.
 */
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
     * The setup for all executions run by this executor. The setter rejects changes
     * while a file is executing and rejects parallel execution.
     */
    override var executionOptions: ExecutionOptions =
        org.islandoftex.arara.core.configuration.ExecutionOptions()
        set(value) {
                if (currentFile != null)
                    throw AraraException(
                        "Cannot change execution options while " +
                            "executing the file ${currentFile?.path}."
                    )
                if (value.parallelExecution)
                    throw AraraException(
                        "This executor does not support " +
                            "parallel execution."
                    )
                field = value
            }

    /**
     * Validate the invocation, then execute projects in dependency order and files by priority.
     * A later success does not erase an earlier failure. With halt-on-errors enabled,
     * the first unsuccessful file stops the invocation.
     *
     * @param projects The projects to act on.
     */
    override fun execute(projects: List<Project>): ExecutionReport {
        val projectsInOrder = ProjectValidation.executionOrder(projects)
        BuildWorkspaces.validate(projectsInOrder)
        hooks.executeBeforeExecution()
        val executionStarted = TimeSource.Monotonic.markNow()
        var exitCode = 0
        val excluded = projects.filter { it.buildDirectory != null }.map { it.executionDirectory }
        for (project in projectsInOrder) {
            val projectCode = executeProject(project, excluded)
            if (projectCode != 0) exitCode = projectCode
            if (executionOptions.haltOnErrors && exitCode != 0)
                break
        }
        val executionEnded = TimeSource.Monotonic.markNow()
        val executionReport = ExecutionReport(executionStarted, executionEnded, exitCode)
        hooks.executeAfterExecution(executionReport)
        return executionReport
    }

    internal fun executeProject(
        project: Project,
        excluded: List<MPPPath> =
            if (project.buildDirectory == null) emptyList() else listOf(project.executionDirectory)
    ): Int {
        var exitCode = 0
        currentProject = project
        try {
            hooks.executeBeforeProject(project)
            val prepared = BuildWorkspaces.prepare(
                project, excluded, stage = executionOptions.executionMode != ExecutionMode.DRY_RUN
            )
            currentProject = prepared
            for (file in prepared.files.sortedWith(
                compareByDescending<org.islandoftex.arara.api.files.ProjectFile> { it.priority }
                    .thenBy { it.sourcePath.normalize().toString() }
            )) {
                hooks.executeBeforeFile(file)
                val report = execute(file)
                if (report.exitCode != 0) exitCode = report.exitCode
                hooks.executeAfterFile(report)
                if (executionOptions.haltOnErrors && report.exitCode != 0) break
            }
            hooks.executeAfterProject(prepared)
            return exitCode
        } finally {
            currentProject = null
        }
    }

    /**
     * Performs arara's main routine to run a file.
     *
     * @param file The file to run.
     */
    override fun execute(file: ProjectFile): ExecutionReport {
        currentFile = file
        HaltRequest.clear()
        val executionStarted = TimeSource.Monotonic.markNow()
        try {
            val directives = hooks.processDirectives(
                file,
                file.fetchDirectives(executionOptions.parseOnlyHeader)
            )
            val exitCode = executeDirectives(directives)
            return ExecutionReport(executionStarted, TimeSource.Monotonic.markNow(), exitCode)
        } finally {
            currentFile = null
            HaltRequest.clear()
        }
    }

    private fun executeDirectives(directives: List<org.islandoftex.arara.api.rules.Directive>): Int {
        var exitCode = 0
        for (directive in directives) {
            val code = directive.execute()
            if (code != 0) exitCode = code
            val haltCode = HaltRequest.exitCode
            if (haltCode != null || (executionOptions.haltOnErrors && code != 0)) {
                if (haltCode != null && haltCode != 0) exitCode = haltCode
                break
            }
        }
        return exitCode
    }

}
