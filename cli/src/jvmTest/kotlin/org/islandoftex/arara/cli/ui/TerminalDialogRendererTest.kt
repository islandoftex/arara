// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ui

import org.islandoftex.arara.api.AraraException
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TerminalDialogRendererTest {
    private fun renderer(input: String, output: StringWriter = StringWriter()): TerminalDialogRenderer =
        TerminalDialogRenderer(StringReader(input).buffered(), PrintWriter(output), { 24 })

    @Test
    fun invalidChoicesRepromptUntilAValidOneBasedSelection() {
        val output = StringWriter()
        val renderer = renderer("text\n-1\n3\n2\n", output)
        assertEquals(2, renderer.showOptions(4, "Choice", "Pick one", arrayOf("One", "Two")))
        assertEquals(3, Regex("Enter a number").findAll(output.toString()).count())
        assertTrue(output.toString().contains("1. One"))
        assertTrue(output.toString().contains("2. Two"))
    }

    @Test
    fun bothSelectionHelpersSupportCancellationAndEmptyLists() {
        assertEquals(0, renderer("0\n").showList(0, "List", "Body", arrayOf("One")))
        assertEquals(0, renderer("0\n").showOptions(0, "Options", "Body", arrayOf("One")))
        assertEquals(0, renderer("").showList(0, "Empty", "Body", emptyArray()))
        assertEquals(0, renderer("").showOptions(0, "Empty", "Body", emptyArray()))
    }

    @Test
    fun blankSelectionsRepromptInBothHelpers() {
        assertEquals(1, renderer("\n1\n").showOptions(0, "Options", "Body", arrayOf("One")))
        assertEquals(1, renderer("\n1\n").showList(0, "List", "Body", arrayOf("One")))
    }

    @Test
    fun closedInputFailsInsteadOfSelectingOrAcceptingAnAnswer() {
        assertFailsWith<AraraException> { renderer("").showInput(0, "Input", "Body") }
        assertFailsWith<AraraException> { renderer("bad\n").showOptions(0, "Options", "Body", arrayOf("One")) }
        assertFailsWith<AraraException> { renderer("").showList(0, "List", "Body", arrayOf("One")) }
    }

    @Test
    fun textInputTrimsWhitespaceButAllowsAnEmptyAnswer() {
        assertEquals("answer", renderer("  answer  \n").showInput(0, "Title", "Body"))
        assertEquals("", renderer("\n").showInput(0, "Title", "Body"))
    }

    @Test
    fun messagesDoNotReadInputAndWrapAtTheConfiguredWidth() {
        val output = StringWriter()
        renderer("", output).showMessage(99, "A title", "a".repeat(60))
        assertTrue(output.toString().contains("[PLAIN] A title"))
        assertTrue(output.toString().lines().all { it.length <= 24 })
    }
}
