// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.console.WatchRun
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.session.ProcessInterrupts

/** Sequential execution on the owning thread, with input collection continuing independently during builds. */
internal class WatchSession(
    private val fallback: WatchGraph,
    private val load: () -> WatchGraph,
    private val execute: (WatchGraph, Set<String>, Map<String, Int?>, Int) -> Map<String, Int?>,
    private val report: WatchReporter,
    private val settings: WatchSettings
) {
    private val runs = mutableListOf<WatchRun>()
    private var results = emptyMap<String, Int?>()
    private var valid = true
    private var lastProblem: String? = null
    private var lastWarnings = emptySet<String>()

    private var graph = fallback
    private var pending: Set<String>? = null

    fun run(interrupts: WatchInterrupts = WatchInterrupts(onStop = report::stopping)): Int {
        return interrupts.use {
            graph = reload() ?: fallback
            pending = if (valid) graph.projects.map { it.name }.toSet() else null
            val changes = WatchChanges(settings.debounceMillis)
            WatchScanner(graph, changes, settings.mode, settings.intervalMillis).use { scanner ->
                scanner.start()
                loop(scanner, changes, interrupts)
                val forced = interrupts.interrupted
                if (forced) Thread.interrupted()
                val code = exitCode(forced, changes.issue() != null)
                report.summary(runs, code)
                code
            }
        }
    }

    private fun exitCode(forced: Boolean, unavailable: Boolean): Int = when {
        forced -> INTERRUPTED
        !valid || unavailable || graph.projects.any { results[it.name] != 0 } -> 1
        else -> 0
    }

    private fun loop(scanner: WatchScanner, changes: WatchChanges, interrupts: WatchInterrupts) {
        while (!interrupts.stopping) {
            val selected = pending
            pending = null
            if (valid && selected != null && changes.issue() == null) runCycle(graph, selected)
            if (!interrupts.stopping) {
                report.waiting()
                val batch = await(changes, interrupts)
                if (batch != null) accept(batch, scanner)
            }
        }
    }

    private fun accept(batch: WatchBatch, scanner: WatchScanner) {
        if (batch.reload) {
            reload()?.let { replacement ->
                graph = replacement
                results = emptyMap()
                scanner.replace(graph)
                pending = graph.projects.map { it.name }.toSet()
            }
        } else if (valid) pending = graph.affected(batch.projects)
    }

    private fun reload(): WatchGraph? = try {
        load().also { valid = true }
    } catch (exception: AraraException) {
        valid = false
        report.invalid(exception)
        null
    }

    private fun runCycle(graph: WatchGraph, selected: Set<String>) {
        val start = TimeSource.Monotonic.markNow()
        LoggingUtils.beginWatchCycle()
        report.starting(runs.size + 1, selected)
        results = try {
            ProcessInterrupts.defer { execute(graph, selected, results, runs.size + 1) }
        } catch (exception: AraraException) {
            report.failure(exception)
            results + selected.associateWith { 1 }
        }
        val code = when {
            Thread.currentThread().isInterrupted -> INTERRUPTED
            selected.any { results[it] != 0 } -> 1
            else -> 0
        }
        val run = WatchRun(runs.size + 1, selected, code, start.elapsedNow())
        runs += run
        report.completed(run)
        LoggingUtils.finishRun(code == 0)
    }

    private fun await(changes: WatchChanges, interrupts: WatchInterrupts): WatchBatch? {
        while (!interrupts.stopping) {
            val issue = changes.issue()
            if (issue != lastProblem) {
                issue?.let(report::problem)
                lastProblem = issue
            }
            val warnings = changes.warnings()
            (warnings - lastWarnings).forEach(report::problem)
            lastWarnings = warnings
            if (issue == null) changes.take()?.let { return it }
            try { Thread.sleep(WAIT_MILLIS) } catch (_: InterruptedException) { break }
        }
        return null
    }

    private companion object {
        const val WAIT_MILLIS = 50L
        const val INTERRUPTED = 130
    }
}
