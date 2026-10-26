// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.zeroturnaround.exec.listener.ProcessDestroyer

/** Owns shutdown hooks only while this executor has an active process. */
internal class ProcessTreeDestroyer : ProcessDestroyer {
    private val hooks = ConcurrentHashMap<Process, Thread>()

    override fun add(process: Process): Boolean {
        val hook = Thread({ stop(process) }, "arara-process-tree")
        if (hooks.putIfAbsent(process, hook) != null) return false
        try {
            Runtime.getRuntime().addShutdownHook(hook)
        } catch (exception: IllegalStateException) {
            hooks.remove(process)
            stop(process)
            throw exception
        } catch (exception: SecurityException) {
            hooks.remove(process)
            stop(process)
            throw exception
        }
        return true
    }

    override fun remove(process: Process): Boolean {
        val hook = hooks.remove(process) ?: return false
        try {
            Runtime.getRuntime().removeShutdownHook(hook)
        } catch (_: IllegalStateException) {
            // A running shutdown hook must finish its cleanup.
        }
        return true
    }

    override fun size(): Int = hooks.size

    companion object {
        private val logger = KotlinLogging.logger { }
        private const val REAP_TIMEOUT_SECONDS = 2L

        /** Snapshot children before stopping their parents, with one shared deadline for reaping. */
        fun stop(process: Process) {
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(REAP_TIMEOUT_SECONDS)
            try {
                for (handle in postOrder(process.toHandle())) {
                    handle.destroyForcibly()
                    val remaining = deadline - System.nanoTime()
                    if (remaining > 0) awaitExit(handle, remaining)
                }
            } catch (exception: UnsupportedOperationException) {
                logger.debug(exception) { "Process-tree inspection is unavailable" }
            } catch (exception: SecurityException) {
                logger.debug(exception) { "Process-tree access was denied" }
            } finally {
                process.destroyForcibly()
            }
        }

        private fun postOrder(root: ProcessHandle): List<ProcessHandle> {
            val pending = ArrayDeque<Pair<ProcessHandle, Boolean>>()
            val visited = mutableSetOf<Long>()
            val result = mutableListOf<ProcessHandle>()
            pending.add(root to false)
            while (pending.isNotEmpty()) {
                val (handle, expanded) = pending.removeLast()
                when {
                    expanded -> result.add(handle)
                    visited.add(handle.pid()) -> {
                        pending.add(handle to true)
                        val children = handle.children().use { it.toList() }
                        pending.addAll(children.map { it to false })
                    }
                }
            }
            return result
        }

        private fun awaitExit(handle: ProcessHandle, timeout: Long) {
            try {
                handle.onExit().get(timeout, TimeUnit.NANOSECONDS)
            } catch (_: TimeoutException) {
                // Continue terminating the remaining tree after the shared deadline.
            } catch (_: java.util.concurrent.ExecutionException) {
                // The direct process is still stopped in the caller's finally block.
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }
}
