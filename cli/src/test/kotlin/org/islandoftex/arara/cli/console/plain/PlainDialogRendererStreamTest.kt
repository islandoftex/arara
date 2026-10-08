// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.HeadlessDialogRenderer
import org.islandoftex.arara.core.ui.TerminalDialogs

/**
 * Verifies the terminal dialogs end to end through the facade with
 * the application's renderer wired, over redirected streams: the
 * rendering of the blocks, the re-prompt on invalid choices, the
 * closed-input error of a numbered dialog, the input trimming, and
 * the empty-list yield of zero. Everything except the human typing
 * is exercised here; the only true residue left is the terminal
 * itself.
 */
class PlainDialogRendererStreamTest {
    private val originalIn = System.`in`
    private val originalOut = System.out

    init {
        // the application wires its renderer before any rule can
        // reach a dialog, so the tests wire it the same way
        TerminalDialogs.renderer = PlainDialogRenderer
    }

    @AfterTest
    fun restoreStreams() {
        System.setIn(originalIn)
        System.setOut(originalOut)
        // every dialog test leaves the session options alone, but the
        // guard keeps a redirected run from leaking into other tests
        Session.userInterfaceOptions = UserInterfaceOptions()
        // the headless default is the state between applications, so
        // the wiring never leaks past this suite
        TerminalDialogs.renderer = HeadlessDialogRenderer
    }

    private fun withStreams(input: String, block: () -> String): Pair<String, String> {
        val sink = ByteArrayOutputStream()
        System.setIn(ByteArrayInputStream(input.toByteArray()))
        System.setOut(PrintStream(sink, true))
        val result = block()
        System.setOut(originalOut)
        System.setIn(originalIn)
        return result to sink.toString("UTF-8")
    }

    @Test
    fun `the message block opens and closes on a separator`() {
        val (_, output) = withStreams("") {
            TerminalDialogs.showMessage(1, "Greetings", "This is a dialog")
            ""
        }
        val lines = output.lineSequence().filter { it.isNotEmpty() }.toList()
        val separator = "-".repeat(79)
        assertEquals(separator, lines.first(), "the block opens with a separator")
        assertEquals(separator, lines.last(), "the block closes with a separator")
        assertTrue("ERROR — Greetings" in output, "the tag and title render")
        assertTrue("This is a dialog" in output, "the body renders")
    }

    @Test
    fun `renders the plain dialog without a tag`() {
        val (_, output) = withStreams("") {
            TerminalDialogs.showMessage(9, "No icon", "Plain and simple")
            ""
        }
        assertTrue("No icon" in output, "the title renders")
        assertTrue("ERROR" !in output && "QUESTION" !in output, "no tag")
    }

    @Test
    fun `asks again on invalid choice`() {
        val (result, output) = withStreams("9\n2\n") {
            TerminalDialogs.showOptions(
                4, "Keep going?", "Shall we finish?", arrayOf("Yes", "No")
            ).toString()
        }
        assertEquals("2", result, "the valid choice wins")
        assertEquals(
            2,
            Regex("Choose an option \\(1-2\\): ").findAll(output).count(),
            "the dialog re-prompts on the invalid choice"
        )
        assertTrue("1) Yes" in output && "2) No" in output, "the entries render")
    }

    @Test
    fun `honors the deliberate no-selection of the dropdown`() {
        val (result, output) = withStreams("0\n") {
            TerminalDialogs.showDropdown(
                2, "Pick one", "Choose an engine",
                arrayOf("pdflatex", "lualatex", "xelatex")
            ).toString()
        }
        assertEquals("0", result, "the no-selection yields zero")
        assertTrue("(0-3)" in output, "the range offers the no-selection")
    }

    @Test
    fun `fails loudly when the input closes on a numbered dialog`() {
        val sink = ByteArrayOutputStream()
        System.setIn(ByteArrayInputStream(ByteArray(0)))
        System.setOut(PrintStream(sink, true))
        val exception = assertFailsWith<AraraException>(
            "a closed input on a numbered dialog is an error"
        ) {
            TerminalDialogs.showOptions(
                4, "Keep going?", "Shall we finish?", arrayOf("Yes", "No")
            )
        }
        System.setOut(originalOut)
        System.setIn(originalIn)
        assertTrue(
            "terminal input ended" in (exception.message ?: ""),
            "the localized closed-input error carries"
        )
        assertTrue(
            sink.toString("UTF-8").endsWith("\n"),
            "the prompt line completes before the error arrives"
        )
    }

    @Test
    fun `trims the input and yields empty for a closed input`() {
        val (typed, output) = withStreams("  Paulo  \n") {
            TerminalDialogs.showInput(4, "Your name", "What should I call you?")
        }
        assertEquals("Paulo", typed, "the input is trimmed")
        assertTrue(
            "QUESTION — Your name" in output,
            "the question tag renders on the input block"
        )
        val (closed, closingOutput) = withStreams("") {
            TerminalDialogs.showInput(4, "Your name", "What should I call you?")
        }
        assertEquals("", closed, "a closed input cancels, as Swing did")
        assertTrue(
            "-".repeat(79) in closingOutput,
            "the input block closes even on cancel"
        )
    }

    @Test
    fun `yields zero for an empty choice list`() {
        val (result, output) = withStreams("") {
            TerminalDialogs.showOptions(
                4, "Nothing", "No buttons at all", emptyArray()
            ).toString() + ":" +
                TerminalDialogs.showDropdown(
                    4, "Nothing", "No elements at all", emptyArray()
                )
        }
        assertEquals("0:0", result, "empty lists yield the no-selection")
        assertTrue(
            "Choose an option" !in output,
            "no prompt appears for an empty list"
        )
    }
}
