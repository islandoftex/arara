// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.session.ExecutionOptions
import org.islandoftex.arara.api.session.ExecutionReport
import org.islandoftex.arara.api.session.Executor
import org.islandoftex.arara.core.files.byPriority

object Executor : Executor {
    @ExperimentalTime
    override fun execute(projects: List<Project>, executionOptions: ExecutionOptions): ExecutionReport {
        // TODO: DAG resolution for parallelization
        // TODO: before execution hook
        val executionStarted = TimeSource.Monotonic.markNow()
        projects.forEach { project ->
            // TODO: before project hook
            project.files.byPriority.forEach {
                // TODO: before file hook
                val executionReport = execute(it, executionOptions)
                if (executionOptions.haltOnErrors && executionReport.exitCode != 0) {
                    TODO("stop")
                }
                // TODO: after file hook
            }
            // TODO: after project hook
        }
        val executionEnded = TimeSource.Monotonic.markNow()
        // TODO: after execution hook
        val exitCode = 0
        return ExecutionReport(executionStarted, executionEnded, exitCode)
    }

    @ExperimentalTime
    override fun execute(file: ProjectFile, executionOptions: ExecutionOptions): ExecutionReport {
        val executionStarted = TimeSource.Monotonic.markNow()
        val directives = file.fetchDirectives(executionOptions.parseOnlyHeader)
        // TODO
        val exitCode = 0
        return ExecutionReport(
                executionStarted, TimeSource.Monotonic.markNow(), exitCode
        )
    }
}
