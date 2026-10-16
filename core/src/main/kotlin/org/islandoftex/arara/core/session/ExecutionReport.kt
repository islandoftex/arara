// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.time.TimeMark
import org.islandoftex.arara.api.session.ExecutionReport

/**
 * Implements the execution report model, holding the start and end
 * stamps of an execution and the resulting exit code.
 */
data class ExecutionReport(
    /**
     * The monotonic mark the execution started at.
     */
    override val executionStarted: TimeMark,
    /**
     * The monotonic mark the execution stopped at.
     */
    override val executionStopped: TimeMark,
    /**
     * The exit code the execution reports.
     */
    override val exitCode: Int
) : ExecutionReport
