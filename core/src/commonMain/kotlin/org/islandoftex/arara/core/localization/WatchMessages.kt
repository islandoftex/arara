// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for watch messages. */
interface WatchMessages {
    /** Waiting for watched input changes. */
    val infoWatchWaiting: String

    /** Graceful watch shutdown and second-interrupt instructions. */
    val infoWatchStopping: String

    /** Invalid watch configuration pauses new builds. */
    val infoWatchPaused: String

    /** Watch build number and selected project names. */
    fun infoWatchStarting(number: Int, projects: String): String

    /** Watch session build count and final exit code. */
    fun infoWatchSummary(runs: Int, code: Int): String

    /** Unavailable watch input or monitoring error. */
    fun warningWatchInput(path: String): String

    /** Watch argument constraints. */
    val errorWatchArguments: String

    /** Watch summary build-number column. */
    val infoWatchCycle: String

    /** Watch summary project column. */
    val infoWatchProjects: String
}
