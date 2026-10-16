// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.terminal.Terminal
import java.nio.charset.Charset
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Task feedback owns one outcome zone; retired callbacks can never repaint a result. */
internal class LiveProgress(
    private val terminal: Terminal,
    private val layout: StyledLayout,
    private val clock: TimeSource = TimeSource.Monotonic,
    private val schedulerFactory: () -> ProgressScheduler = { ExecutorProgressScheduler() }
) : AutoCloseable {
    private val lock = Any()
    private val frames = framesFor(System.out.charset())
    private var scheduler: ProgressScheduler? = null
    private var pending: ProgressTask? = null
    private var generation = 0L
    private var zone: Zone? = null
    private var hidden = false
    private var painted = false
    private var resized = false
    private var closed = false
    private var frame = 0

    fun start(column: Int, columns: Int, decimalSeparator: Char) = synchronized(lock) {
        stopLocked()
        if (!closed) {
            resized = false
            zone = Zone(column, columns, layout.width, decimalSeparator, clock.markNow())
            frame = 0
            val expected = generation
            val executor = scheduler ?: schedulerFactory().also { scheduler = it }
            pending = executor.schedule(THRESHOLD, INTERVAL) { tick(expected) }
        }
    }

    /** Return true after a resize, so the result can use a fresh line instead of stale columns. */
    fun stop(): Boolean = synchronized(lock) {
        stopLocked()
        resized.also { resized = false }
    }

    private fun tick(expected: Long) = synchronized(lock) {
        val area = zone
        if (expected == generation && area != null && area.started.elapsedNow() >= THRESHOLD) {
            if (layout.width != area.terminalWidth) {
                stopLocked()
            } else {
                if (!hidden) {
                    hidden = true
                    terminal.cursor.hide()
                }
                anchor(area)
                val text = frames[frame] + " " + formatElapsed(area.started.elapsedNow(), area.decimalSeparator)
                val fitted = layout.fit(text, area.columns)
                terminal.rawPrint(StyledTheme.logo(fitted) + " ".repeat(area.columns - layout.cells(fitted)))
                painted = true
                frame = (frame + 1) % frames.size
            }
        }
    }

    private fun stopLocked() {
        generation++
        pending?.cancel()
        pending = null
        zone?.let { area ->
            resized = resized || layout.width != area.terminalWidth
            if (painted && !resized) {
                anchor(area)
                terminal.rawPrint(" ".repeat(area.columns))
                anchor(area)
            }
        }
        if (hidden) terminal.cursor.show()
        hidden = false
        painted = false
        zone = null
    }

    private fun anchor(area: Zone) = terminal.cursor.move {
        startOfLine()
        right(area.column)
    }

    override fun close() = synchronized(lock) {
        stopLocked()
        closed = true
        scheduler?.close()
        scheduler = null
    }

    private data class Zone(
        val column: Int,
        val columns: Int,
        val terminalWidth: Int,
        val decimalSeparator: Char,
        val started: TimeMark
    )

    companion object {
        val THRESHOLD = 1000.milliseconds
        val INTERVAL = 90.milliseconds
        const val CLOCK_WIDTH = 8
        private const val SECONDS_PER_MINUTE = 60
        private const val SECONDS_PER_HOUR = 3600
        private const val MAX_CLOCK_HOURS = 100
        private const val MILLIS_PER_SECOND = 1000.0
        private val BRAILLE = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
        private val ASCII = listOf("|", "/", "-", "\\")

        internal fun framesFor(charset: Charset): List<String> =
            if (charset.newEncoder().canEncode(BRAILLE.joinToString(""))) BRAILLE else ASCII

        internal fun formatElapsed(elapsed: Duration, separator: Char): String {
            val seconds = elapsed.inWholeSeconds
            val hours = seconds / SECONDS_PER_HOUR
            return when {
                seconds < SECONDS_PER_MINUTE ->
                    String.format(Locale.ROOT, "%.1f", elapsed.inWholeMilliseconds / MILLIS_PER_SECOND)
                        .replace('.', separator) + "s"
                seconds < SECONDS_PER_HOUR -> String.format(Locale.ROOT, "%d:%02d",
                    seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)
                hours < MAX_CLOCK_HOURS -> String.format(Locale.ROOT, "%d:%02d:%02d", hours,
                    (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)
                else -> "${hours}h"
            }
        }
    }
}

internal fun interface ProgressTask {
    fun cancel()
}

internal interface ProgressScheduler : AutoCloseable {
    fun schedule(delay: Duration, interval: Duration, action: () -> Unit): ProgressTask
}

/** Created only for an eligible task, and closed with its renderer. */
internal class ExecutorProgressScheduler : ProgressScheduler {
    private val executor = Executors.newSingleThreadScheduledExecutor { action ->
        Thread(action, "arara-live-progress").apply { isDaemon = true }
    }

    override fun schedule(delay: Duration, interval: Duration, action: () -> Unit): ProgressTask {
        val future = executor.scheduleAtFixedRate(action, delay.inWholeMilliseconds,
            interval.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        return ProgressTask { future.cancel(false) }
    }

    override fun close() {
        executor.shutdownNow()
    }
}
