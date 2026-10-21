// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.console.styled.StyledDialogRenderer
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StyledLifecycleTest {
    @Test
    fun styledDialogsUseHostStreamsAndKeepValidationAndClosedInputBehavior() {
        val output = StringWriter()
        val renderer = StyledDialogRenderer(
            Terminal(AnsiLevel.NONE, width = 40),
            BufferedReader(StringReader("bad\n2\n")), PrintWriter(output)
        )
        assertEquals(2, renderer.showList(4, "Pick one", "A choice", arrayOf("first", "second")))
        assertTrue("Pick one" in output.toString())
        assertTrue("Enter a number" in output.toString())
        assertFalse("\u001b" in output.toString())
        val closed = StyledDialogRenderer(
            Terminal(AnsiLevel.NONE), BufferedReader(StringReader("")), PrintWriter(output)
        )
        assertFailsWith<AraraException> { closed.showInput(4, "Input", "Value") }
        output.write("host still owns the stream")
    }
}
