// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.utils.OS

/** Invocation-thread scope for tools that must finish after the first watch-mode Ctrl+C. */
object ProcessInterrupts {
    private val deferred = ThreadLocal.withInitial { false }

    /** Defer terminal interrupts in child tools; thread interruption still invokes normal process-tree cleanup. */
    fun <T> defer(action: () -> T): T {
        val previous = deferred.get()
        deferred.set(true)
        return try { action() } finally {
            if (previous) deferred.set(true) else deferred.remove()
        }
    }

    internal fun elements(elements: List<String>): List<String> = if (deferred.get() && !OS.isWindows)
        listOf("/bin/sh", "-c", "trap '' INT; exec \"\$@\"", "arara-watch") + elements else elements
}
