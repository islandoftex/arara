// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StyledTaskDetailsTest {
    private fun fixture(block: () -> Unit) {
        val snapshot = ConfigurationSnapshot.capture()
        try {
            Session.userInterfaceOptions = UserInterfaceOptions(enableAnimations = false)
            LinearExecutor.executionOptions = ExecutionOptions(verbose = true)
            LanguageController.loadMessagesFor(MPPLocale("en"))
            block()
        } finally {
            snapshot.restore()
        }
    }

    private fun plain(text: String): String = text.replace(Regex("\u001b\\[[0-9;]*m"), "")

    private fun frames(text: String): Int = plain(text).lineSequence().count { line ->
        line.isNotEmpty() && line.all { it == '.' || it == '┈' || it == ' ' } && line.any { it != ' ' }
    }

    @Test
    fun verboseFieldsAreLocalizedAndRawOutputHasBalancedFrames() = fixture {
        for (level in listOf(AnsiLevel.NONE, AnsiLevel.TRUECOLOR)) {
            val output = ProgressTestTerminal(level, interactive = false)
            val renderer = StyledConsoleRenderer(output.terminal)
            renderer.printEntry("Probe", "Task")
            renderer.printTaskDetails("pdflatex run.tex", DirectiveConditional(DirectiveConditionalType.IF, "true"),
                listOf("Ada", "Ben"))
            output.terminal.print("RAW_OUTPUT_WITHOUT_NEWLINE")
            renderer.printEntryResult(true)
            renderer.close()
            val text = plain(output.output())
            assertTrue("System command: pdflatex run.tex" in text)
            assertTrue("(IF) true" in text)
            assertTrue("Ada, Ben" in text)
            assertEquals(2, frames(text))
            assertTrue("RAW_OUTPUT_WITHOUT_NEWLINE\n" in text)
            assertTrue(text.indexOf("RAW_OUTPUT") < text.indexOf("SUCCESS"))
            assertEquals(level != AnsiLevel.NONE, "\u001b" in output.output())
        }
    }

    @Test
    fun exceptionsAndSuccessiveCommandsCloseTheirFramesExactlyOnce() = fixture {
        val output = ProgressTestTerminal(AnsiLevel.NONE, interactive = false)
        val renderer = StyledConsoleRenderer(output.terminal)
        renderer.printEntry("Probe", "Task")
        renderer.printTaskDetails("first", DirectiveConditional(), emptyList())
        renderer.printTaskDetails("second", DirectiveConditional(), emptyList())
        renderer.printException("command failed", "cause")
        renderer.close()
        assertEquals(4, frames(output.output()))
        assertTrue("command failed" in output.output())
    }

    @Test
    fun narrowLocalizedFieldsPreserveUnicodeAndLongArguments() = fixture {
        val command = "tool 界界e\u0301 ${"x".repeat(60)}"
        for (locale in listOf("en", "de", "nl", "it", "pt-BR", "en-QN")) {
            LanguageController.loadMessagesFor(MPPLocale(locale))
            for (width in listOf(1, 8, 17, 20, 80)) {
                val output = ProgressTestTerminal(AnsiLevel.NONE, interactive = true, width = width)
                val renderer = StyledConsoleRenderer(output.terminal)
                renderer.printDryRunCommand(command)
                val text = output.output()
                val expected = LanguageController.messages.infoLabelSystemCommand + command
                assertEquals(expected.filterNot(Char::isWhitespace), text.filterNot(Char::isWhitespace))
                val layout = StyledLayout(output.terminal)
                assertTrue(text.lineSequence().all { layout.cells(it) <= maxOf(2, width) }, "$locale width=$width: ${text.lines().map(layout::cells)}\n$text")
                assertFalse("\u001b" in text)
                renderer.close()
            }
        }
    }
}
