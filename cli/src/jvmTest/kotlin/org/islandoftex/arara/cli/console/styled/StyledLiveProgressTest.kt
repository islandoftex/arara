// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.console.RunSummary
import org.islandoftex.arara.cli.ui.TerminalDialogRenderer
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class StyledLiveProgressTest {
    private fun withRenderer(
        sink: ProgressTestTerminal = ProgressTestTerminal(),
        block: (ProgressTestTerminal, StyledConsoleRenderer, ManualProgressScheduler, TestTimeSource) -> Unit
    ) {
        val snapshot = ConfigurationSnapshot.capture()
        val clock = TestTimeSource()
        val scheduler = ManualProgressScheduler()
        val renderer = StyledConsoleRenderer(sink.terminal, clock) { scheduler }
        try {
            Session.userInterfaceOptions = UserInterfaceOptions()
            LinearExecutor.executionOptions = ExecutionOptions()
            block(sink, renderer, scheduler, clock)
        } finally {
            renderer.close()
            snapshot.restore()
        }
    }

    @Test
    fun ineligibleStreamsModesAndConfigurationNeverScheduleFeedback() {
        val cases = listOf(
            ProgressTestTerminal(level = AnsiLevel.NONE),
            ProgressTestTerminal(interactive = false),
            ProgressTestTerminal(input = false, cursorClaim = true),
            ProgressTestTerminal(width = 10)
        )
        cases.forEach { terminal ->
            withRenderer(terminal) { sink, renderer, scheduler, clock ->
                renderer.printEntry("Rule", "Task")
                clock += 2.seconds
                renderer.printEntryResult(true)
                assertTrue(scheduler.tasks.isEmpty())
                assertFalse("\u001b[?25l" in sink.output())
                assertFalse("⠋" in sink.output())
            }
        }
        for (mode in listOf("disabled", "verbose", "dry-run")) {
            withRenderer { sink, renderer, scheduler, clock ->
                Session.userInterfaceOptions = UserInterfaceOptions(enableAnimations = mode != "disabled")
                LinearExecutor.executionOptions = ExecutionOptions(verbose = mode == "verbose",
                    executionMode = if (mode == "dry-run") ExecutionMode.DRY_RUN else ExecutionMode.NORMAL_RUN)
                renderer.printEntry("Rule", "Task")
                clock += 2.seconds
                renderer.printEntryResult(true)
                assertTrue(scheduler.tasks.isEmpty())
                assertFalse("\u001b[?25l" in sink.output())
            }
        }
    }

    @Test
    fun successFailureAndExceptionsRetireQueuedFramesBeforePrintingTheirVerdict() {
        for (result in listOf("success", "failure", "exception")) {
            withRenderer { sink, renderer, scheduler, clock ->
                renderer.printEntry("Rule", "Task")
                clock += 1.seconds
                scheduler.tasks.single().action()
                assertTrue("⠋" in sink.output())
                when (result) {
                    "success" -> renderer.printEntryResult(true)
                    "failure" -> renderer.printEntryResult(false)
                    else -> renderer.printException("Command timed out", "timeout details")
                }
                val verdict = when (result) {
                    "success" -> "SUCCESS"
                    "failure" -> "FAILURE"
                    else -> "ERROR"
                }
                assertTrue(verdict in sink.output())
                assertTrue("\u001b[?25h" in sink.output())
                assertTrue(scheduler.tasks.single().cancelled)
                val landed = sink.output()
                scheduler.tasks.single().action()
                assertEquals(landed, sink.output())
                renderer.close()
                assertTrue(scheduler.closed)
            }
        }
    }

    @Test
    fun dialogsAndOtherOutputSuspendFeedbackBeforeUsingTheirStreams() {
        for (styled in listOf(false, true)) {
            withRenderer { sink, renderer, scheduler, clock ->
                renderer.printEntry("Rule", "Task")
                clock += 1.seconds
                scheduler.tasks.single().action()
                val dialogOutput = StringWriter()
                val input = BufferedReader(StringReader("answer\n"))
                val output = PrintWriter(dialogOutput)
                val dialog = if (styled) StyledDialogRenderer(sink.terminal, input, output,
                    beforeDialog = renderer::suspendProgress)
                    else TerminalDialogRenderer(input, output, beforeDialog = renderer::suspendProgress)
                assertEquals("answer", dialog.showInput(4, "Question", "Value"))
                assertTrue("Question" in dialogOutput.toString())
                assertTrue(sink.output().endsWith("\n"))
                val paused = sink.output()
                scheduler.tasks.single().action()
                assertEquals(paused, sink.output())
                renderer.printEntryResult(true)
                assertTrue("SUCCESS" in sink.output().removePrefix(paused))
                assertTrue("\u001b[?25h" in paused)
            }
        }
        withRenderer { sink, renderer, scheduler, clock ->
            renderer.printEntry("Rule", "Task")
            clock += 1.seconds
            scheduler.tasks.single().action()
            renderer.printWrapped("Other output")
            val paused = sink.output()
            scheduler.tasks.single().action()
            assertEquals(paused, sink.output())
            assertTrue("Other output" in paused)
            renderer.printRunSummary(RunSummary(true, false, emptyList()))
            renderer.close()
            assertTrue(scheduler.closed)
        }
    }

    @Test
    fun aResizeLandsTheResultOnAFreshLineWithoutRestartingTheAnimation() = withRenderer { sink, renderer, scheduler, clock ->
        renderer.printEntry("Rule", "Task")
        clock += 1.seconds
        scheduler.tasks.single().action()
        sink.width = 18
        sink.terminal.updateSize()
        scheduler.tasks.single().action()
        val resized = sink.output()
        renderer.printEntryResult(true)
        val result = sink.output().removePrefix(resized)
        assertTrue(result.startsWith("\n"))
        assertTrue("SUCCESS" in result)
        assertFalse("⠋" in result)
        scheduler.tasks.single().action()
        assertEquals(resized + result, sink.output())
    }
}
