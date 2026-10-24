// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import kotlin.time.TimeMark
import org.islandoftex.arara.api.session.ExecutionReport

/** Elapsed-time marks and final status for a file or a complete invocation. */
data class ExecutionReport(
    /** Monotonic mark captured before execution. */
    override val executionStarted: TimeMark,
    /** Monotonic mark captured after execution. */
    override val executionStopped: TimeMark,
    /** Final status; zero denotes success. */
    override val exitCode: Int
) : ExecutionReport
