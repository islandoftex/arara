// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import java.io.Closeable
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.WatchMode
import org.islandoftex.arara.core.watch.WatchMonitor

/** Captures changes while the execution thread is busy running tools. */
internal class WatchScanner(
    initial: WatchGraph, private val changes: WatchChanges,
    private val mode: WatchMode, private val intervalMillis: Long
) : Closeable {
    private data class State(val graph: WatchGraph, val baseline: WatchObservation)
    private val state = AtomicReference(State(initial, initial.observe()))
    @Volatile private var stopped = false
    private val thread = Thread({ scan() }, "arara-watch-inputs").apply { isDaemon = true }

    fun start() { thread.start() }
    fun replace(value: WatchGraph) { state.set(State(value, value.observe())) }

    private fun scan() {
        try {
            WatchMonitor(mode).use { monitor -> loop(monitor) }
        } catch (exception: IOException) {
            changes.failed(exception.toString())
        } catch (exception: UnsupportedOperationException) {
            changes.failed(exception.toString())
        }
    }

    private fun loop(monitor: WatchMonitor) {
        while (!stopped) {
            try {
                val current = state.get()
                val observation = current.graph.observe()
                if (state.compareAndSet(current, State(current.graph, observation))) {
                    changes.record(observation.changedProjects(current.baseline),
                        observation.controls != current.baseline.controls)
                    changes.failed(null)
                    changes.problems(observation.problems)
                    monitor.update(observation.directories)
                }
                monitor.await(intervalMillis)
            } catch (_: InterruptedException) {
                break
            } catch (exception: IOException) {
                changes.failed(exception.toString())
                pause()
            } catch (exception: AraraException) {
                changes.failed(exception.toString())
                pause()
            } catch (exception: UnsupportedOperationException) {
                changes.failed(exception.toString())
                pause()
            }
        }
    }

    private fun pause() {
        try { Thread.sleep(intervalMillis) } catch (_: InterruptedException) { stopped = true }
    }

    override fun close() {
        stopped = true
        thread.interrupt()
        thread.join()
    }
}
