// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

/** Thread-safe accumulation; consuming a batch never drops later changes arriving during a build. */
internal class WatchChanges(private val debounceMillis: Long, private val nanoTime: () -> Long = System::nanoTime) {
    private val projects = linkedSetOf<String>()
    private var reload = false
    private var changedAt = 0L
    private var problem: String? = null
    private var inputProblems = emptySet<String>()

    @Synchronized
    fun record(changed: Set<String>, controlsChanged: Boolean) {
        if (changed.isNotEmpty() || controlsChanged) {
            projects += changed
            reload = reload || controlsChanged
            changedAt = nanoTime()
        }
    }

    @Synchronized
    fun problems(value: Set<String>) { inputProblems = value }

    @Synchronized
    fun failed(message: String?) {
        if (problem != null && message == null) record(emptySet(), true)
        problem = message
    }

    @Synchronized
    fun take(): WatchBatch? = if ((reload || projects.isNotEmpty()) &&
        nanoTime() - changedAt >= java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(debounceMillis)) {
        WatchBatch(projects.toSet(), reload).also { projects.clear(); reload = false }
    } else null

    @Synchronized
    fun issue(): String? = problem

    @Synchronized
    fun warnings(): Set<String> = inputProblems
}

internal data class WatchBatch(val projects: Set<String>, val reload: Boolean)
