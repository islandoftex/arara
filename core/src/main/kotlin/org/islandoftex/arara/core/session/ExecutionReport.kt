// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.time.TimeMark
import org.islandoftex.arara.api.session.ExecutionReport

/**
 * Implements the execution report model, holding the start and end
 * stamps of an execution and the resulting exit code.
 */
data class ExecutionReport(
    override val executionStarted: TimeMark,
    override val executionStopped: TimeMark,
    override val exitCode: Int
) : ExecutionReport
