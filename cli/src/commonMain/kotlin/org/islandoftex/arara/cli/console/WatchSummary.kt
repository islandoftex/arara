// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import kotlin.time.Duration

/** One completed watch build; project results from previous cycles remain in the session graph. */
data class WatchRun(
    /** One-based build sequence number. */
    val number: Int,
    /** Projects selected for this cycle, including blocked dependents. */
    val projects: Set<String>,
    /** This cycle's result; zero indicates success. */
    val exitCode: Int,
    /** Wall-clock duration including input preparation. */
    val elapsed: Duration
)

/** Watch history and the final result after graceful or forced shutdown. */
data class WatchSummary(
    /** Completed cycles in chronological order. */
    val runs: List<WatchRun>,
    /** Latest graph result, or interruption status after a second Ctrl+C. */
    val exitCode: Int
)
