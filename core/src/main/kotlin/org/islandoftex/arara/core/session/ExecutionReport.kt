// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.session.ExecutionReport
import kotlin.time.TimeMark

/**
 * Implements the execution report model, holding the start and end
 * stamps of an execution and the resulting exit code.
 */
data class ExecutionReport(
    override val executionStarted: TimeMark,
    override val executionStopped: TimeMark,
    override val exitCode: Int
) : ExecutionReport
