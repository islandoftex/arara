// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import org.islandoftex.arara.api.files.MPPPath

internal fun SmokeContext.processLifecycleCases(): Map<String, (MPPPath) -> Unit> =
    ProcessLifecycleChecks(this).cases

private class ProcessLifecycleChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "watch_queues_changes_and_rebuilds_only_dependents" to ::watchQueuesChangesAndRebuildsOnlyDependents,
        "watch_recovers_failures_and_retains_failed_cycle_logs" to ::watchRecoversFailuresAndRetainsFailedCycleLogs,
        "watch_pauses_invalid_configuration_and_reloads" to ::watchPausesInvalidConfigurationAndReloads,
        "watch_active_build_keeps_original_configuration" to ::watchActiveBuildKeepsOriginalConfiguration,
        "watch_first_interrupt_finishes_active_build" to ::watchFirstInterruptFinishesActiveBuild,
        "watch_second_interrupt_stops_active_tools" to ::watchSecondInterruptStopsActiveTools,
        "active_descendants_stop_when_arara_is_terminated" to ::activeDescendantsStopWhenAraraIsTerminated,
        "active_descendants_stop_when_a_command_times_out" to ::activeDescendantsStopWhenACommandTimesOut,
        "normal_completion_preserves_detached_processes" to ::normalCompletionPreservesDetachedProcesses,
    )

    private fun watchQueuesChangesAndRebuildsOnlyDependents(
        root: MPPPath
    ) {

        WatchCheck(root, launcher, jar, fixture).queuedChanges()
    }

    private fun watchRecoversFailuresAndRetainsFailedCycleLogs(
        root: MPPPath
    ) {

        WatchCheck(root, launcher, jar, fixture).failureRecovery()
    }

    private fun watchPausesInvalidConfigurationAndReloads(
        root: MPPPath
    ) {

        WatchCheck(root, launcher, jar, fixture).reloadConfiguration()
    }

    private fun watchActiveBuildKeepsOriginalConfiguration(
        root: MPPPath
    ) {

        WatchCheck(root, launcher, jar, fixture).activeConfigurationIsFrozen()
    }

    private fun watchFirstInterruptFinishesActiveBuild(
        root: MPPPath
    ) {

        WatchCheck(root, launcher, jar, fixture).gracefulInterrupt()
    }

    private fun watchSecondInterruptStopsActiveTools(
        root: MPPPath
    ) {

        WatchCheck(root, launcher, jar, fixture).forcedInterrupt()
    }

    private fun activeDescendantsStopWhenAraraIsTerminated(
        root: MPPPath
    ) {
        processTree(root).interrupted()
    }

    private fun activeDescendantsStopWhenACommandTimesOut(
        root: MPPPath
    ) {
        processTree(root).timedOut()
    }

    private fun normalCompletionPreservesDetachedProcesses(
        root: MPPPath
    ) {
        processTree(root).detached()
    }

}
