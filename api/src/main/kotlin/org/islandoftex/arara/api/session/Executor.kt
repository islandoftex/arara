// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import org.islandoftex.arara.api.files.Project

interface ExecutionReport {
    @ExperimentalTime
    val executionStarted: TimeMark

    @ExperimentalTime
    val executionStopped: TimeMark

    val exitCode: Int
}

interface Executor {
    @ExperimentalTime
    fun execute(
        projects: List<Project>,
        executionOptions: ExecutionOptions
    ): ExecutionReport
}
