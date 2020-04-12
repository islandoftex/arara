// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.session.ExecutionReport
import org.islandoftex.arara.api.session.Executor
import org.islandoftex.arara.core.files.byPriority

/**
 * arara's core executor is configurable at some places by inserting hooks.
 * This is a collection of all hooks that are applicable.
 */
data class ExecutorHooks(
    val executeBeforeExecution: () -> Unit = {
        Session.updateEnvironmentVariables()
    },
    val executeAfterExecution: (ExecutionReport) -> Unit = { _ -> },
    val executeBeforeProject: (Project) -> Unit = { _ -> },
    val executeAfterProject: (Project) -> Unit = { _ -> },
    val executeBeforeFile: (ProjectFile) -> Unit = { _ -> },
    val executeAfterFile: (ExecutionReport) -> Unit = { _ -> },
    val processDirectives: (List<Directive>) -> List<Directive> = { l -> l }
)

object Executor : Executor {
    var hooks = ExecutorHooks()

    @ExperimentalTime
    override fun execute(
        projects: List<Project>,
        executionOptions: ExecutionOptions
    ): ExecutionReport {
        // TODO: DAG resolution for parallelization
        hooks.executeBeforeExecution()
        val executionStarted = TimeSource.Monotonic.markNow()
        var exitCode = 0
        for (project in projects) {
            exitCode = executeProject(project, executionOptions)
            if (executionOptions.haltOnErrors && exitCode != 0)
                break
        }
        val executionEnded = TimeSource.Monotonic.markNow()
        val executionReport = ExecutionReport(executionStarted, executionEnded, exitCode)
        hooks.executeAfterExecution(executionReport)
        return executionReport
    }

    @ExperimentalTime
    internal fun executeProject(
        project: Project,
        executionOptions: ExecutionOptions
    ): Int {
        var exitCode = 0
        hooks.executeBeforeProject(project)
        for (file in project.files.byPriority) {
            hooks.executeBeforeFile(file)
            val executionReport = execute(file, executionOptions)
            exitCode = executionReport.exitCode
            if (executionOptions.haltOnErrors && exitCode != 0)
                return exitCode
            hooks.executeAfterFile(executionReport)
        }
        hooks.executeAfterProject(project)
        return exitCode
    }

    @ExperimentalTime
    override fun execute(
        file: ProjectFile,
        executionOptions: ExecutionOptions
    ): ExecutionReport {
        val executionStarted = TimeSource.Monotonic.markNow()
        val directives = hooks.processDirectives(
                file.fetchDirectives(executionOptions.parseOnlyHeader)
        )
        var exitCode = 0
        for (directive in directives) {
            exitCode = directive.execute()
            if (executionOptions.haltOnErrors && exitCode != 0) {
                break
            }
        }
        return ExecutionReport(
                executionStarted, TimeSource.Monotonic.markNow(), exitCode
        )
    }
}
