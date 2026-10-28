// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.reporting

import kotlin.time.Duration

/** One completed watch build; project results from previous cycles remain in the session graph. */
public data class WatchRun(
    /** One-based build sequence number. */
    public val number: Int,
    /** Projects selected for this cycle, including blocked dependents. */
    public val projects: Set<String>,
    /** This cycle's result; zero indicates success. */
    public val exitCode: Int,
    /** Wall-clock duration including input preparation. */
    public val elapsed: Duration
)

/** Watch history and the final result after graceful or forced shutdown. */
public data class WatchSummary(
    /** Completed cycles in chronological order. */
    public val runs: List<WatchRun>,
    /** Latest graph result, or interruption status after a second Ctrl+C. */
    public val exitCode: Int
)
