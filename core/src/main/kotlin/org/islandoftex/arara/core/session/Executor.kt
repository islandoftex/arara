// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.session.ExecutionOptions
import org.islandoftex.arara.api.session.ExecutionReport
import org.islandoftex.arara.api.session.Executor
import org.islandoftex.arara.core.files.byPriority

object Executor : Executor {
    // TODO: move hooks to class
    var executeBeforeExecution: () -> Unit = {}
    var executeAfterExecution: (ExecutionReport) -> Unit = { _ -> }
    var executeBeforeProject: (Project) -> Unit = { _ -> }
    var executeAfterProject: (Project) -> Unit = { _ -> }
    var executeBeforeFile: (ProjectFile) -> Unit = { _ -> }
    var executeAfterFile: (ExecutionReport) -> Unit = { _ -> }
    var processDirectives: (List<Directive>) -> List<Directive> = { l -> l }

    @ExperimentalTime
    override fun execute(
        projects: List<Project>,
        executionOptions: ExecutionOptions
    ): ExecutionReport {
        // TODO: DAG resolution for parallelization
        executeBeforeExecution()
        val executionStarted = TimeSource.Monotonic.markNow()
        var exitCode = 0
        for (project in projects) {
            exitCode = executeProject(project, executionOptions)
            if (executionOptions.haltOnErrors && exitCode != 0)
                break
        }
        val executionEnded = TimeSource.Monotonic.markNow()
        val executionReport = ExecutionReport(executionStarted, executionEnded, exitCode)
        executeAfterExecution(executionReport)
        return executionReport
    }

    @ExperimentalTime
    internal fun executeProject(
        project: Project,
        executionOptions: ExecutionOptions
    ): Int {
        var exitCode = 0
        executeBeforeProject(project)
        for (file in project.files.byPriority) {
            executeBeforeFile(file)
            val executionReport = execute(file, executionOptions)
            exitCode = executionReport.exitCode
            if (executionOptions.haltOnErrors && exitCode != 0)
                return exitCode
            executeAfterFile(executionReport)
        }
        executeAfterProject(project)
        return exitCode
    }

    @ExperimentalTime
    override fun execute(
        file: ProjectFile,
        executionOptions: ExecutionOptions
    ): ExecutionReport {
        val executionStarted = TimeSource.Monotonic.markNow()
        val directives = processDirectives(
                file.fetchDirectives(executionOptions.parseOnlyHeader)
        )
        var exitCode = 0
        for (directive in directives) {
            exitCode = directive.execute()
            if (executionOptions.haltOnErrors) {
                break
            }
        }
        return ExecutionReport(
                executionStarted, TimeSource.Monotonic.markNow(), exitCode
        )
    }
}
