// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class LiveProgressTest {
    @Test
    fun thresholdFastTasksAndRetiredCallbacksNeverHideTheCursorOrPaintOverLaterTasks() {
        val sink = ProgressTestTerminal()
        val clock = TestTimeSource()
        val scheduler = ManualProgressScheduler()
        val progress = LiveProgress(sink.terminal, StyledLayout(sink.terminal), clock) { scheduler }
        try {
            progress.start(60, 10, '.')
            val first = scheduler.tasks.single()
            assertEquals(1000.milliseconds, first.delay)
            assertEquals(90.milliseconds, first.interval)
            first.action()
            assertEquals("", sink.output())
            clock += 999.milliseconds
            first.action()
            assertEquals("", sink.output())
            assertFalse(progress.stop())
            assertTrue(first.cancelled)
            progress.start(60, 10, ',')
            val before = sink.output()
            first.action()
            assertEquals(before, sink.output())
            clock += 1.seconds
            scheduler.tasks.last().action()
            assertTrue("⠋" in sink.output())
            assertTrue("1,0s" in sink.output())
            assertTrue("\u001b[?25l" in sink.output())
            clock += 90.milliseconds
            scheduler.tasks.last().action()
            assertTrue("⠙" in sink.output())
            progress.stop()
            assertTrue("\u001b[?25h" in sink.output())
            val landed = sink.output()
            scheduler.tasks.forEach { it.action() }
            assertEquals(landed, sink.output())
        } finally {
            progress.close()
        }
        assertTrue(scheduler.closed)
        val closed = sink.output()
        progress.close()
        progress.start(60, 10, '.')
        scheduler.tasks.forEach { it.action() }
        assertEquals(closed, sink.output())
    }

    @Test
    fun resizingRetiresFeedbackAndDoesNotMoveBackToAnInvalidOutcomeZone() {
        val sink = ProgressTestTerminal()
        val clock = TestTimeSource()
        val scheduler = ManualProgressScheduler()
        val progress = LiveProgress(sink.terminal, StyledLayout(sink.terminal), clock) { scheduler }
        try {
            progress.start(60, 10, '.')
            clock += 1.seconds
            scheduler.tasks.single().action()
            sink.width = 20
            sink.terminal.updateSize()
            val before = sink.output()
            scheduler.tasks.single().action()
            val afterResize = sink.output().removePrefix(before)
            assertTrue("\u001b[?25h" in afterResize)
            assertFalse("\u001b[60C" in afterResize)
            assertTrue(scheduler.tasks.single().cancelled)
            assertTrue(progress.stop())
            assertFalse(progress.stop())
            scheduler.tasks.single().action()
            assertEquals(before + afterResize, sink.output())
        } finally {
            progress.close()
        }
    }

    @Test
    fun idleFeedbackDoesNotCreateAnExecutorAndClosingAnActiveOneRestoresTheCursor() {
        val sink = ProgressTestTerminal()
        val clock = TestTimeSource()
        val scheduler = ManualProgressScheduler()
        var created = 0
        val idle = LiveProgress(sink.terminal, StyledLayout(sink.terminal)) { created++; scheduler }
        idle.stop()
        idle.close()
        assertEquals(0, created)
        assertFalse(scheduler.closed)
        val active = LiveProgress(sink.terminal, StyledLayout(sink.terminal), clock) { created++; scheduler }
        active.start(60, 10, '.')
        clock += 1.seconds
        scheduler.tasks.single().action()
        active.close()
        assertEquals(1, created)
        assertTrue(scheduler.closed)
        assertTrue(scheduler.tasks.single().cancelled)
        assertTrue(sink.output().endsWith("\u001b[?25h"))
    }

    @Test
    fun countersUseTheCapturedSeparatorAndStayCompactAcrossClockBoundaries() {
        assertEquals("1.2s", LiveProgress.formatElapsed(1200.milliseconds, '.'))
        assertEquals("1,2s", LiveProgress.formatElapsed(1200.milliseconds, ','))
        assertEquals("1:00", LiveProgress.formatElapsed(60.seconds, ','))
        assertEquals("59:59", LiveProgress.formatElapsed(3599.seconds, '.'))
        assertEquals("1:00:00", LiveProgress.formatElapsed(3600.seconds, '.'))
        assertEquals("99:59:59", LiveProgress.formatElapsed(359999.seconds, '.'))
        assertEquals("100h", LiveProgress.formatElapsed(100.hours, '.'))
        assertTrue(LiveProgress.framesFor(StandardCharsets.UTF_8).all { it in "⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏" })
        assertEquals(listOf("|", "/", "-", "\\"), LiveProgress.framesFor(StandardCharsets.US_ASCII))
    }

    @Test
    fun productionSchedulerUsesAnOwnedDaemonAndClosesIt() {
        val scheduler = ExecutorProgressScheduler()
        val ran = CountDownLatch(1)
        val worker = AtomicReference<Thread>()
        try {
            val task = scheduler.schedule(Duration.ZERO, 1.seconds) {
                worker.set(Thread.currentThread())
                ran.countDown()
            }
            assertTrue(ran.await(5, TimeUnit.SECONDS))
            assertTrue(worker.get().isDaemon)
            assertEquals("arara-live-progress", worker.get().name)
            task.cancel()
        } finally {
            scheduler.close()
        }
        worker.get().join(5000)
        assertFalse(worker.get().isAlive)
        assertFailsWith<RejectedExecutionException> { scheduler.schedule(Duration.ZERO, 1.seconds) {} }
    }
}
