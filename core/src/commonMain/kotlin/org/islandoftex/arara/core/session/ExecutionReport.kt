// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.session.ExecutionReport
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark

@OptIn(kotlin.time.ExperimentalTime::class)
data class ExecutionReport
constructor(
    override val executionStarted: TimeMark,
    override val executionStopped: TimeMark,
    override val exitCode: Int
) : ExecutionReport
