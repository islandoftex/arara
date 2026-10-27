// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import sun.misc.Signal
import sun.misc.SignalHandler

/** Own the INT handler only for this session; restore the embedding application's handler on exit. */
internal class WatchInterrupts(
    private val executionThread: Thread = Thread.currentThread(), private val onStop: () -> Unit = { }
) : Closeable {
    private val count = AtomicInteger()
    private val signal = Signal("INT")
    private val previous: SignalHandler = Signal.handle(signal) { request() }
    val stopping: Boolean get() = count.get() > 0 || executionThread.isInterrupted
    val interrupted: Boolean get() = count.get() > 1 || executionThread.isInterrupted

    fun request() {
        if (count.incrementAndGet() > 1) executionThread.interrupt() else onStop()
    }

    override fun close() { Signal.handle(signal, previous) }
}
