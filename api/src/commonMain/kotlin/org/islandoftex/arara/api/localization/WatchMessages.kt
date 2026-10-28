// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for watch messages. */
public interface WatchMessages {
    /** Waiting for watched input changes. */
    public val infoWatchWaiting: String

    /** Graceful watch shutdown and second-interrupt instructions. */
    public val infoWatchStopping: String

    /** Invalid watch configuration pauses new builds. */
    public val infoWatchPaused: String

    /** Watch build number and selected project names. */
    public fun infoWatchStarting(number: Int, projects: String): String

    /** Watch session build count and final exit code. */
    public fun infoWatchSummary(runs: Int, code: Int): String

    /** Unavailable watch input or monitoring error. */
    public fun warningWatchInput(path: String): String

    /** Watch argument constraints. */
    public val errorWatchArguments: String

    /** Watch summary build-number column. */
    public val infoWatchCycle: String

    /** Watch summary project column. */
    public val infoWatchProjects: String
}
