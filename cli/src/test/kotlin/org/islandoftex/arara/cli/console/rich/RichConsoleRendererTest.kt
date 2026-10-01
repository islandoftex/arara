// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.rich

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.session.Command
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/**
 * Verifies the rich console renderer against a terminal with forced
 * capabilities: the decorated stream carries the tint, the glyphs
 * and the aligned results, and the plain stream keeps every
 * structure with the ASCII fallbacks and no escape sequences. The
 * renderer prints through the terminal interface, so the standard
 * output is redirected to capture the rendered run.
 */
class RichConsoleRendererTest {
    private val originalOut = System.out

    @BeforeTest
    fun pinTheGlobals() {
        // the localized labels drive both the needles and the layout,
        // and the execution options decide which entry shape renders,
        // so the suite pins the defaults before any capture
        Session.userInterfaceOptions = UserInterfaceOptions()
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)
        LinearExecutor.executionOptions = ExecutionOptions()
    }

    @AfterTest
    fun restoreStreams() {
        System.setOut(originalOut)
    }

    private fun render(
        ansiLevel: AnsiLevel,
        width: Int = 80,
        block: RichConsoleRenderer.() -> Unit
    ): String {
        val sink = ByteArrayOutputStream()
        System.setOut(PrintStream(sink, true))
        val renderer = RichConsoleRenderer(
            Terminal(ansiLevel, width = width)
        )
        renderer.block()
        System.setOut(originalOut)
        return sink.toString("UTF-8")
    }

    @Test
    fun `the banner carries the tint and the version`() {
        val output = render(AnsiLevel.TRUECOLOR) { printBanner() }
        assertTrue(
            "arara 8.0.0-SNAPSHOT" in output,
            "the version line renders under the logo"
        )
        assertTrue(
            "\u001B[" in output,
            "the decorated stream carries the color escapes"
        )
        assertTrue("__ _ _ __ __" in output, "the logo renders")
    }

    @Test
    fun `the entry runs with the glyph and aligns the result`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("echoone", "The echoer")
            printEntryResult(true)
        }
        assertTrue(
            "▸ echoone · The echoer" in output,
            "the running entry carries the glyph"
        )
        assertTrue(
            "✓ SUCCESS" in output,
            "the success result carries the check glyph"
        )
        assertFalse("\n" in output.trimEnd(), "the compact entry stays on one line")
    }

    @Test
    fun `the failure result carries the failure glyph`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("echoone", "The echoer")
            printEntryResult(false)
        }
        assertTrue(
            "✗ FAILURE" in output,
            "the failure result carries the cross glyph"
        )
    }

    @Test
    fun `the exception block names the error and the details`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("echoone", "The echoer")
            printException("the message of the failure", "the underlying cause")
        }
        assertTrue("ERROR" in output, "the error label renders")
        assertTrue("the message of the failure" in output, "the message renders")
        assertTrue("the underlying cause" in output, "the details render")
    }

    @Test
    fun `the plain stream keeps the structure with ASCII`() {
        val output = render(AnsiLevel.NONE) {
            printBanner()
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printConditional(
                DirectiveConditional(
                    DirectiveConditionalType.IF,
                    "found('log', 'Rerun')"
                )
            )
        }
        assertFalse(
            "\u001B" in output,
            "the plain stream carries no escape sequences"
        )
        assertTrue("> echoone · The echoer" in output, "the ASCII running glyph")
        assertTrue("+ SUCCESS" in output, "the ASCII success glyph")
        assertTrue("(IF)" in output, "the conditional renders")
    }

    @Test
    fun `prose wraps at the readable measure`() {
        val text = List(40) { "word" + it }.joinToString(" ")
        val output = render(AnsiLevel.NONE, width = 200) {
            printWrapped(text)
        }
        output.lineSequence().forEach { line ->
            assertTrue(
                line.length <= 79,
                "every wrapped line keeps the readable measure"
            )
        }
    }

    @Test
    fun `the separators follow the live width`() {
        val renderer = RichConsoleRenderer(
            Terminal(AnsiLevel.NONE, width = 40)
        )
        assertEquals(
            "-".repeat(40),
            renderer.displaySeparator(),
            "the rule spans the live width in the ASCII fallback"
        )
        assertTrue(
            "QUACK" in renderer.displayOutputSeparator("QUACK"),
            "the titled separator names its title"
        )
    }

    @Test
    fun `the closing summary carries the verdict and the table`() {
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.TRUECOLOR) {
            printFileInformation(file)
            printEntry("pdflatex", "The engine run")
            printEntryResult(true)
            printEntry("biber", "References")
            printEntryResult(true)
            printRunSummary(true)
        }
        assertTrue(
            "TOTAL SUCCESS" in output,
            "the verdict line renders the success word and the counts"
        )
        assertFalse(
            "┌" in output,
            "the summary keeps the borderless layout of the presentation"
        )
        assertTrue("Execution summary" in output, "the table title renders")
        assertTrue(
            document.name in output,
            "the file row names the file"
        )
        assertTrue("2 tasks" in output, "the verdict counts the tasks")
    }

    @Test
    fun `the closing verdict of a failed run carries the failure`() {
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.TRUECOLOR) {
            printFileInformation(file)
            printEntry("pdflatex", "The engine run")
            printEntryResult(false)
            printRunSummary(false)
        }
        assertTrue(
            "TOTAL FAILURE" in output,
            "the failure verdict renders the failure word"
        )
        assertTrue(
            "✗" in output,
            "the failed file row carries the cross glyph"
        )
    }

    @Test
    fun `the dry run verdict makes no success claim`() {
        LinearExecutor.executionOptions = ExecutionOptions(
            executionMode = ExecutionMode.DRY_RUN
        )
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.TRUECOLOR) {
            printFileInformation(file)
            printEntry("pdflatex", "The engine run")
            printRunSummary(true)
        }
        assertTrue("DRY RUN" in output, "the dry run verdict renders")
        assertFalse(
            "Execution summary" in output,
            "the dry run carries no table"
        )
    }
    @Test
    fun `the verbose details show the command and frame the stream`() {
        LinearExecutor.executionOptions = ExecutionOptions(verbose = true)
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("echoone", "The echoer")
            printTaskDetails(
                Command(listOf("echo", "QuackOne")),
                DirectiveConditional(
                    DirectiveConditionalType.IF,
                    "found('log', 'Rerun')"
                ),
                listOf("Island of TeX")
            )
            printEntryResult(true)
        }
        assertTrue("System command" in output, "the field label renders")
        assertTrue("echo" in output, "the command field carries the command")
        assertTrue("Island of TeX" in output, "the authors field renders")
        assertTrue("┈" in output, "the output frame renders")
        // the color escapes surround the word, so the contiguous
        // needle is asserted against the stripped stream
        val stripped = output.replace(Regex("\u001B\\[[0-9;]*m"), "")
        assertTrue(
            "SUCCESS ·" in stripped,
            "the result carries the duration"
        )
    }

    @Test
    fun `the skip card names the conditionally skipped directive`() {
        LinearExecutor.executionOptions = ExecutionOptions(verbose = true)
        val output = render(AnsiLevel.TRUECOLOR) {
            printSkippedDirective("makeglossaries")
        }
        assertTrue("skipped" in output, "the skip card names the skip")
        assertTrue(
            "makeglossaries" in output,
            "the skip card names the directive"
        )
    }

    @Test
    fun `the compact run stays silent about the skip`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printSkippedDirective("makeglossaries")
        }
        assertFalse("skipped" in output, "the compact run shows no skip card")
    }
}
