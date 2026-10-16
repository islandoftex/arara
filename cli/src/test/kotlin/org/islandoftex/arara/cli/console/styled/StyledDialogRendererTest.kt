// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session

/**
 * Verifies the styled dialog renderer over redirected streams: the
 * decorated terminal carries the tinted box with the severity
 * glyph, the numbered choices and the bold range prompt, and the
 * plain stream falls back to the classic terminal dialogs
 * themselves - the sibling renders the degraded form, so its
 * needles prove the identity by construction.
 */
class StyledDialogRendererTest {
    private val originalIn = System.`in`
    private val originalOut = System.out
    private var originalUIOptions = Session.userInterfaceOptions

    @BeforeTest
    fun pinTheGlobals() {
        // the localized labels drive the tags and the prompt, so the
        // suite pins the default language before any capture
        originalUIOptions = Session.userInterfaceOptions
        Session.userInterfaceOptions = UserInterfaceOptions()
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)
    }

    @AfterTest
    fun restoreTheGlobals() {
        // the dialog suites and the renderer suites around them
        // share the session-wide state, so the pin leaves nothing
        // behind
        Session.userInterfaceOptions = originalUIOptions
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)
        System.setIn(originalIn)
        System.setOut(originalOut)
    }

    private fun withStreams(
        ansiLevel: AnsiLevel,
        input: String,
        block: StyledDialogRenderer.() -> Unit
    ): String {
        val sink = ByteArrayOutputStream()
        System.setIn(ByteArrayInputStream(input.toByteArray()))
        System.setOut(PrintStream(sink, true))
        StyledDialogRenderer(Terminal(ansiLevel, width = 80)).block()
        System.setOut(originalOut)
        System.setIn(originalIn)
        return sink.toString("UTF-8")
    }

    @Test
    fun `the message box carries the severity and the body`() {
        val output = withStreams(AnsiLevel.TRUECOLOR, "") {
            showMessage(1, "Greetings", "This is a dialog")
        }
        assertTrue("ERROR — Greetings" in output, "the tag and title head the box")
        assertTrue(
            "\u001B[" in output.substringBefore("ERROR"),
            "the heading carries the tint of its box"
        )
        assertTrue("This is a dialog" in output, "the body renders inside")
        assertTrue("│" in output, "the box frames the conversation")
    }

    @Test
    fun `the options box numbers the choices and prompts boldly`() {
        val output = withStreams(AnsiLevel.TRUECOLOR, "2\n") {
            assertEquals(
                2,
                showOptions(4, "Keep going?", "Shall we finish?", arrayOf("Yes", "No")),
                "the typed choice flows back"
            )
        }
        assertTrue("QUESTION — Keep going?" in output, "the question tag heads the box")
        // the accent styling surrounds the numbers, so the
        // contiguous needles are asserted against the stripped
        // stream
        val stripped = output.replace(Regex("\u001B\\[[0-9;]*m"), "")
        assertTrue("1) Yes" in stripped, "the first choice is numbered")
        assertTrue("2) No" in stripped, "the second choice is numbered")
    }

    @Test
    fun `the dropdown honors the no-selection`() {
        val output = withStreams(AnsiLevel.TRUECOLOR, "0\n") {
            assertEquals(
                0,
                showList(
                    2, "Pick one", "Choose an engine",
                    arrayOf("pdflatex", "lualatex", "xelatex")
                ),
                "the deliberate no-selection flows back"
            )
        }
        assertTrue("0-3" in output, "the range offers the no-selection")
    }

    @Test
    fun `the input box prompts after the frame`() {
        val output = withStreams(AnsiLevel.TRUECOLOR, "  Paulo  \n") {
            assertEquals(
                "Paulo",
                showInput(4, "Your name", "What should I call you?"),
                "the answer flows back trimmed"
            )
        }
        assertTrue("> " in output, "the input prompt renders after the box")
    }

    @Test
    fun `an empty choice list offers nothing`() {
        val output = withStreams(AnsiLevel.TRUECOLOR, "") {
            assertEquals(
                0,
                showOptions(4, "Nothing", "No buttons at all", emptyArray()),
                "empty lists yield the no-selection"
            )
        }
        assertFalse("Choose an option" in output, "no prompt appears")
    }

    @Test
    fun `the plain stream falls back to the classic dialogs`() {
        val output = withStreams(AnsiLevel.NONE, "") {
            showMessage(1, "Greetings", "This is a dialog")
        }
        // the sibling renders the degraded form, so its own needles
        // prove the identity: the dash separators and the tagged
        // block of the classic terminal dialogs
        assertTrue(
            "-".repeat(79) in output,
            "the classic block opens with the separator"
        )
        assertTrue("ERROR — Greetings" in output, "the classic tag and title render")
        assertFalse("│" in output, "the plain stream carries no box")
    }
}
