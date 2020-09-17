// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.time.TimeMark
import org.islandoftex.arara.api.session.ExecutionReport

data class ExecutionReport(
    override val executionStarted: TimeMark,
    override val executionStopped: TimeMark,
    override val exitCode: Int
) : ExecutionReport
