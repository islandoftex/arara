// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.cli.ui.TerminalDialogRenderer
import org.islandoftex.arara.core.localization.LanguageController
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StyledDialogPresentationTest {
    private fun plain(text: String): String = text.replace(Regex("\u001b\\[[0-9;]*m"), "")

    @Test
    fun redirectedDialogsMatchThePlainPresentationExactly() {
        for (type in 0..4) {
            val outputs = listOf(false, true).map { styled ->
                val output = StringWriter()
                val input = StringReader(" answer \ninvalid\n2\n0\n").buffered()
                val renderer = if (styled) StyledDialogRenderer(Terminal(AnsiLevel.NONE, width = 42),
                    input, PrintWriter(output)) else TerminalDialogRenderer(input, PrintWriter(output), { 42 })
                assertEquals("answer", renderer.showInput(type, "literal %s", "body literal %s"))
                assertEquals(2, renderer.showOptions(type, "Title", "Pick", arrayOf("One", "Two")))
                assertEquals(0, renderer.showList(type, "Title", "Pick", arrayOf("One")))
                output.toString()
            }
            assertEquals(outputs[0], outputs[1])
            assertFalse("\u001b" in outputs[1])
        }
    }

    @Test
    fun decoratedDialogsBoxTheirChoicesAndRetainSelectionAndCancellation() {
        val previous = LanguageController.messages
        try {
            LanguageController.loadMessagesFor(MPPLocale("en"))
            for (type in 0..4) {
                val output = StringWriter()
                var interruptions = 0
                val renderer = StyledDialogRenderer(Terminal(AnsiLevel.TRUECOLOR, width = 40),
                    StringReader("invalid\n2\n0\n").buffered(), PrintWriter(output)) { interruptions++ }
                assertEquals(2, renderer.showOptions(type, "literal %s", "Rule text %s",
                    arrayOf("first", "second choice with a very long description")))
                assertEquals(0, renderer.showList(type, "Cancel", "Rule text", arrayOf("first")))
                assertEquals(2, interruptions)
                val text = plain(output.toString())
                assertTrue("╭" in text && "╯" in text)
                assertTrue("literal %s" in text)
                assertTrue("Rule text %s" in text)
                assertTrue("│ 1. first" in text)
                assertTrue(text.lines().filter { it.startsWith("│") }.all { it.length == 40 }, text)
                assertTrue("\u001b" in output.toString())
                assertTrue("0-2" in text)
                assertTrue("0-1" in text)
            }
        } finally {
            LanguageController.messages = previous
        }
    }

    @Test
    fun dialogWidthsAndWrappedChoicesPreserveWholeUnicodeGraphemes() {
        val choice = "界界e\u0301${"verylong".repeat(10)}"
        for (width in listOf(1, 7, 8, 20, 40, 100)) {
            val output = StringWriter()
            val terminal = Terminal(AnsiLevel.TRUECOLOR, width = width)
            val renderer = StyledDialogRenderer(terminal, StringReader("1\n").buffered(), PrintWriter(output))
            assertEquals(1, renderer.showOptions(4, "Title", "Body", arrayOf(choice)))
            val text = plain(output.toString())
            val layout = StyledLayout(terminal)
            assertTrue(text.lines().all { layout.cells(it) <= maxOf(2, minOf(width, 79)) }, text)
            assertTrue(choice in text.replace(Regex("[ │\\n]"), ""), text)
        }
    }

    @Test
    fun streamsWithoutBoxGlyphsUseThePlainDialog() {
        val previous = System.out
        val output = StringWriter()
        try {
            System.setOut(PrintStream(ByteArrayOutputStream(), true, Charsets.US_ASCII))
            StyledDialogRenderer(Terminal(AnsiLevel.TRUECOLOR, width = 40),
                StringReader("").buffered(), PrintWriter(output)).showMessage(2, "Title", "Body")
        } finally {
            System.setOut(previous)
        }
        assertTrue("[" in output.toString())
        assertFalse("╭" in output.toString())
        assertFalse("\u001b" in output.toString())
    }

    @Test
    fun emptyChoicesAndClosedInputKeepTheirContractsAndHostStreamsStayOpen() {
        var inputClosed = false
        var outputClosed = false
        var interruptions = 0
        val input = object : StringReader("") {
            override fun close() {
                inputClosed = true
                super.close()
            }
        }
        val output = object : StringWriter() {
            override fun close() {
                outputClosed = true
                super.close()
            }
        }
        val renderer = StyledDialogRenderer(Terminal(AnsiLevel.TRUECOLOR, width = 40),
            input.buffered(), PrintWriter(output)) { interruptions++ }
        assertEquals(0, renderer.showOptions(1, "Title", "Body", emptyArray()))
        assertEquals(0, renderer.showList(1, "Title", "Body", emptyArray()))
        assertEquals("", output.toString())
        assertEquals(0, interruptions)
        assertEquals(LanguageController.messages.errorDialogInputClosed,
            assertFailsWith<AraraException> { renderer.showInput(1, "Title", "Body") }.message)
        assertEquals(1, interruptions)
        assertFalse(inputClosed)
        assertFalse(outputClosed)
    }
}
