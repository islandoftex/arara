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
import org.islandoftex.arara.core.configuration.LoggingOptions
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
        interactive: Boolean = false,
        block: RichConsoleRenderer.() -> Unit
    ): String {
        val sink = ByteArrayOutputStream()
        System.setOut(PrintStream(sink, true))
        val renderer = RichConsoleRenderer(
            Terminal(ansiLevel, width = width, interactive = interactive)
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
            "the cool TeX automation tool" in output,
            "the greeting carries the slogan"
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
    fun `the animations key silences the breath`() {
        Session.userInterfaceOptions = UserInterfaceOptions(
            enableAnimations = false
        )
        val output = render(AnsiLevel.TRUECOLOR, interactive = true) {
            printEntry("sleeper", "The sleeper")
            Thread.sleep(1200)
            printEntryResult(true)
        }
        assertFalse(
            "⠋" in output,
            "no frame ever appears when the key asks for stillness"
        )
        assertTrue("SUCCESS" in output, "the result lands as always")
    }

    @Test
    fun `the breath is on by default`() {
        val output = render(AnsiLevel.TRUECOLOR, interactive = true) {
            printEntry("sleeper", "The sleeper")
            Thread.sleep(1300)
            printEntryResult(true)
        }
        assertTrue(
            "⠋" in output,
            "the bare options breathe with no key set anywhere"
        )
        assertTrue("SUCCESS" in output, "the result lands as always")
    }

    @Test
    fun `the live feedback breathes after the threshold`() {
        Session.userInterfaceOptions = UserInterfaceOptions(
            enableAnimations = true
        )
        val output = render(AnsiLevel.TRUECOLOR, interactive = true) {
            printEntry("sleeper", "The sleeper")
            Thread.sleep(1300)
            printEntryResult(true)
        }
        assertTrue(
            "⠋" in output,
            "the frames cycle in the outcome zone once the task outlives the threshold"
        )
        assertTrue(
            Regex("[0-9][.,][0-9]s").containsMatchIn(output),
            "the frames carry the elapsed time of the running task"
        )
        assertTrue(
            Regex("\r\u001B\\[[0-9]+C").containsMatchIn(output),
            "every frame re-anchors to the outcome zone, healing stray displacement"
        )
        assertTrue(
            "\u001B[?25l" in output,
            "the terminal cursor yields the breathing cell to the frames"
        )
        assertTrue(
            "\u001B[?25h" in output,
            "the outcome restores the cursor for the landing"
        )
        assertTrue("SUCCESS" in output, "the result still lands cleanly")
    }

    @Test
    fun `the breath stops for the error that claims its line`() {
        Session.userInterfaceOptions = UserInterfaceOptions(
            enableAnimations = true
        )
        val output = render(AnsiLevel.TRUECOLOR, interactive = true) {
            printEntry("sleeper", "The sleeper")
            Thread.sleep(1300)
            printException("the tool failed mid-breath", null)
        }
        assertTrue(
            "⠋" in output,
            "the zone breathed before the error struck"
        )
        assertTrue(
            "\u001B[?25l" in output,
            "the cursor yielded the breathing zone"
        )
        assertTrue(
            "\u001B[?25h" in output,
            "the stop on the error returns the cursor"
        )
        assertTrue(
            // the carriage returns of the re-anchors are column
            // moves, not line breaks: the terminal's eye sees one
            // physical line, and the stripped view sees it too
            output.replace("\r", "").lineSequence().any {
                "The sleeper" in it && "ERROR" in it
            },
            "the error word lands on the open line, overwriting the frame"
        )
        assertTrue(
            "the tool failed mid-breath" in output,
            "the message follows the landing"
        )
    }

    @Test
    fun `a fast task never breathes`() {
        Session.userInterfaceOptions = UserInterfaceOptions(
            enableAnimations = true
        )
        val output = render(AnsiLevel.TRUECOLOR, interactive = true) {
            printEntry("echoone", "The echoer")
            printEntryResult(true)
        }
        assertFalse(
            "⠋" in output,
            "the threshold keeps fast runs exactly as static as they always were"
        )
    }

    @Test
    fun `a stream that is not a terminal never sees a frame`() {
        Session.userInterfaceOptions = UserInterfaceOptions(
            enableAnimations = true
        )
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("sleeper", "The sleeper")
            Thread.sleep(1300)
            printEntryResult(true)
        }
        assertFalse(
            "⠋" in output,
            "the interactivity gates the frames away"
        )
        assertTrue("SUCCESS" in output, "the result lands as always")
    }

    @Test
    fun `the counter escalates its notation as the task runs longer`() {
        val renderer = RichConsoleRenderer(
            Terminal(AnsiLevel.NONE, width = 80)
        )
        assertEquals(
            "2.4s", renderer.formatElapsed(2.4),
            "under a minute: the live tenths"
        )
        assertEquals(
            "59.9s", renderer.formatElapsed(59.9),
            "the last tenth before the escalation"
        )
        assertEquals(
            "1:04", renderer.formatElapsed(64.0),
            "past the minute: the clock"
        )
        assertEquals(
            "12:30", renderer.formatElapsed(750.0),
            "the clock holds under the hour"
        )
        assertEquals(
            "1:00:04", renderer.formatElapsed(3604.0),
            "past the hour: the full clock"
        )
        assertEquals(
            "10:00:04", renderer.formatElapsed(36004.0),
            "the pathological run still tells its time"
        )
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
    fun `a silent failure separates its verdict`() {
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.NONE) {
            printFileInformation(file)
            printEntry("silentechoer", "The silent echoer")
            printEntryResult(false)
            printRunSummary(false)
        }
        val lines = output.lineSequence().toList()
        val failure = lines.indexOfFirst { "FAILURE" in it && "TOTAL" !in it }
        assertTrue(
            failure > 0 && lines[failure + 1].isEmpty(),
            "the silent landing carries its own separation"
        )
        assertTrue(
            "TOTAL FAILURE" in lines[failure + 2],
            "the verdict starts on a fresh line"
        )
    }

    @Test
    fun `a landed success with a failed run separates its verdict`() {
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.NONE) {
            printFileInformation(file)
            printEntry("halt", "The halt trigger")
            printEntryResult(true)
            printRunSummary(false)
        }
        val lines = output.lineSequence().toList()
        val success = lines.indexOfFirst {
            "SUCCESS" in it && "TOTAL" !in it
        }
        assertTrue(
            success > 0 && lines[success + 1].isEmpty(),
            "the landed outcome separates from the failed verdict - " +
                "the halted run's shape"
        )
        assertTrue(
            "TOTAL FAILURE" in lines[success + 2],
            "the verdict follows on a fresh line"
        )
    }

    @Test
    fun `the quoted panel separates the failure itself`() {
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.NONE) {
            printFileInformation(file)
            printEntry("failechoer", "The failing echoer")
            printEntryResult(false)
            printCommandFailure("QuackFail")
            printRunSummary(false)
        }
        val lines = output.lineSequence().toList()
        val panel = lines.indexOfFirst { "QuackFail" in it }
        assertTrue(
            panel > 0 && lines[panel + 1].isEmpty(),
            "the panel owns its separation"
        )
        assertTrue(
            "TOTAL FAILURE" in lines[panel + 2],
            "the verdict follows without a second blank"
        )
    }

    @Test
    fun `the skip card separates from the verdict`() {
        LinearExecutor.executionOptions = ExecutionOptions(verbose = true)
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.NONE) {
            printFileInformation(file)
            printSkippedDirective("echoone")
            printRunSummary(true)
        }
        val lines = output.lineSequence().toList()
        val card = lines.indexOfFirst { "skipped" in it }
        assertTrue(
            card > 0 && lines[card + 1].isEmpty(),
            "the card that ends a file without a task separates the verdict"
        )
        assertTrue("TOTAL SUCCESS" in lines[card + 2], "the verdict follows")
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
    fun `the mid-task error lands flush on the open entry line`() {
        val error = render(AnsiLevel.NONE) {
            printEntry("missingbinary", "The missing binary")
            printException("the tool never started", null)
        }
        val success = render(AnsiLevel.NONE) {
            printEntry("missingbinary", "The missing binary")
            printEntryResult(true)
        }
        assertEquals(
            success.lineSequence().first { "SUCCESS" in it }.length,
            error.lineSequence().first { "ERROR" in it }.length,
            "the error label ends flush at the edge like a landed result"
        )
    }

    @Test
    fun `the standalone error titles its section with the rule`() {
        val output = render(AnsiLevel.NONE) {
            printException("the run could not proceed", null)
        }
        val lines = output.lineSequence().toList()
        assertEquals(
            "x ERROR",
            lines[0].take(7),
            "the section title opens with the glyph and the word"
        )
        assertEquals(
            79,
            lines[0].length,
            "the rule fills the title line to the measure"
        )
        assertTrue(
            lines[0].endsWith("-"),
            "the rule runs from the word to the edge"
        )
        assertEquals(
            "the run could not proceed",
            lines[1],
            "the message follows the rule directly, with no blank between"
        )
    }

    @Test
    fun `the error prose speaks in the default voice`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printException("the severity stays on the title word", null)
        }
        val prose = output.lineSequence()
            .first { it.contains("the severity stays") }
        assertFalse(
            "\u001B[" in prose,
            "the message carries no styling: the title word holds the severity"
        )
    }

    @Test
    fun `the error between tasks still titles its section`() {
        val output = render(AnsiLevel.NONE) {
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printException("the second task never ran", null)
        }
        val title = output.lineSequence().first { "ERROR" in it }
        assertTrue(
            title.endsWith("-"),
            "the section title prints after a landed result too"
        )
    }

    @Test
    fun `a new file opens its error block without a stale separation`() {
        val first = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val second = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val output = render(AnsiLevel.NONE) {
            printFileInformation(
                ProjectFile(MPPPath(first.absolutePath), FileType("tex", "^%.*"))
            )
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printFileInformation(
                ProjectFile(MPPPath(second.absolutePath), FileType("tex", "^%.*"))
            )
            printException("the second file failed", null)
        }
        val lines = output.lineSequence().toList()
        val title = lines.indexOfFirst { "ERROR" in it }
        assertTrue(
            lines[title - 1].isBlank(),
            "the header's own blank separates the title"
        )
        assertTrue(
            second.name in lines[title - 2],
            "no stale separation blank from the prior file's landed result"
        )
    }

    @Test
    fun `the verbose error titles its section over the open frame`() {
        LinearExecutor.executionOptions = ExecutionOptions(verbose = true)
        val output = render(AnsiLevel.NONE) {
            printEntry("missingbinary", "The missing binary")
            printTaskDetails(
                Command(listOf("this-binary-does-not-exist")),
                DirectiveConditional(DirectiveConditionalType.NONE, ""),
                listOf("Island of TeX")
            )
            printException("the tool never started", null)
        }
        LinearExecutor.executionOptions = ExecutionOptions()
        val frame = Regex("^\\.(  \\.)*$")
        val lines = output.lineSequence().toList()
        assertEquals(
            1,
            lines.count { frame.matches(it) },
            "exactly one frame rule opens the stream that never came"
        )
        assertEquals(
            "x ERROR",
            lines[lines.indexOfFirst { frame.matches(it) } + 1].take(7),
            "the section title terminates the open frame"
        )
        assertEquals(
            "the tool never started",
            lines[lines.indexOfFirst { frame.matches(it) } + 2],
            "the message follows the title directly, with no dotted close"
        )
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
    @Test
    fun `the failed task quotes the tail of its own output`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("failechoer", "The failing echoer")
            printEntryResult(false)
            printCommandFailure("QuackFail")
        }
        assertTrue("│ QuackFail" in output, "the panel quotes the tool words")
    }

    @Test
    fun `a silent failure quotes nothing`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("failechoer", "The failing echoer")
            printEntryResult(false)
            printCommandFailure("")
        }
        assertFalse(
            "│" in output,
            "a command that said nothing has nothing to quote"
        )
    }

    @Test
    fun `a verbose failure keeps the panel away`() {
        LinearExecutor.executionOptions = ExecutionOptions(verbose = true)
        val output = render(AnsiLevel.TRUECOLOR) {
            printCommandFailure("QuackFail")
        }
        assertFalse(
            "│ QuackFail" in output,
            "a verbose run already streamed the output live"
        )
    }
    @Test
    fun `the presentation keeps the readable measure on wide terminals`() {
        val renderer = RichConsoleRenderer(
            Terminal(AnsiLevel.NONE, width = 200)
        )
        assertEquals(
            "-".repeat(79),
            renderer.displaySeparator(),
            "the chrome holds the readable measure, not the live width"
        )
    }

    @Test
    fun `the error message word-wraps at the measure`() {
        val message = List(30) { "word" + it }.joinToString(" ")
        val output = render(AnsiLevel.TRUECOLOR, width = 80) {
            printEntry("echoone", "The echoer")
            printException(message, null)
        }
        val stripped = output.replace(Regex("\u001B\\[[0-9;]*m"), "")
        stripped.lineSequence().forEach { line ->
            assertTrue(
                line.length <= 79,
                "every error line keeps the readable measure"
            )
        }
    }

    @Test
    fun `an exception fails the file the summary names`() {
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.TRUECOLOR) {
            printFileInformation(file)
            printException("the message of the failure", null)
            printRunSummary(false)
        }
        assertTrue(
            "TOTAL FAILURE" in output,
            "the verdict names the failure"
        )
        assertTrue(
            "✗" in output,
            "the failed file carries the cross in the summary"
        )
    }
    @Test
    fun `the file header runs the rule flush to the metadata`() {
        val document = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val file = ProjectFile(
            MPPPath(document.absolutePath),
            FileType("tex", "^%.*")
        )
        val output = render(AnsiLevel.NONE, width = 80) {
            printFileInformation(file)
        }
        val header = output.lineSequence()
            .first { it.contains(document.name) }
        assertTrue(
            header.startsWith(document.name),
            "the name starts flush at the edge"
        )
        assertTrue(
            header.trimEnd().endsWith(file.path.lastModified.toString()),
            "the line ends where the metadata ends, with no stub"
        )
        assertTrue(
            "-" in header && " B" in header,
            "the dimmed rule runs to the metadata"
        )
    }

    @Test
    fun `the ledger survives the files of a run`() {
        val first = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val second = File.createTempFile("rich", ".tex")
            .apply { writeText("quack"); deleteOnExit() }
        val output = render(AnsiLevel.TRUECOLOR) {
            printFileInformation(
                ProjectFile(MPPPath(first.absolutePath), FileType("tex", "^%.*"))
            )
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printFileInformation(
                ProjectFile(MPPPath(second.absolutePath), FileType("tex", "^%.*"))
            )
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printRunSummary(true)
        }
        assertTrue(
            "2 files" in output,
            "the verdict counts every file of the run"
        )
        assertTrue(
            "2 tasks" in output,
            "the verdict counts every task of the run"
        )
        assertTrue(
            first.name in output && second.name in output,
            "the summary names every file of the run"
        )
    }
    @Test
    fun `the dry run trio renders as fields`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printAuthors(listOf("Island of TeX"))
            printDryRunCommand(Command(listOf("echo", "QuackOne")))
            printConditional(
                DirectiveConditional(
                    DirectiveConditionalType.IF,
                    "changed('tex')"
                )
            )
        }
        val stripped = output.replace(Regex("\u001B\\[[0-9;]*m"), "")
        assertTrue(
            "About to run" in stripped,
            "the command field carries its label"
        )
        assertTrue(
            "[echo, QuackOne]" in stripped,
            "the command field carries the command"
        )
        assertTrue(
            "Island of TeX" in stripped,
            "the authors field carries the names"
        )
        assertTrue(
            "(IF)" in stripped,
            "the conditional field carries the expression"
        )
    }

    @Test
    fun `the failure panel opens with the log hint`() {
        // the hint only speaks when it points at the truth: the
        // output was actually truncated and logging holds the rest
        Session.loggingOptions = LoggingOptions(enableLogging = true)
        val manyLines = (1..20).joinToString("\n") { "tool line $it" }
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("failechoer", "The failing echoer")
            printEntryResult(false)
            printCommandFailure(manyLines)
        }
        val stripped = output.replace(Regex("\u001B\\[[0-9;]*m"), "")
        assertTrue(
            stripped.indexOf("the full output is in the log file") <
                stripped.indexOf("tool line 11"),
            "the hint titles the panel before the quoted tail"
        )
    }

    @Test
    fun `the failure panel stays silent when the log does not exist`() {
        Session.loggingOptions = LoggingOptions(enableLogging = false)
        val manyLines = (1..20).joinToString("\n") { "tool line $it" }
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("failechoer", "The failing echoer")
            printEntryResult(false)
            printCommandFailure(manyLines)
        }
        val stripped = output.replace(Regex("\u001B\\[[0-9;]*m"), "")
        assertTrue(
            "the full output is in the log file" !in stripped,
            "the hint stays silent when there is no log file"
        )
        assertTrue(
            "…" in stripped,
            "the ellipsis still marks the cut"
        )
    }

    @Test
    fun `the failure panel shows no hint when nothing was truncated`() {
        Session.loggingOptions = LoggingOptions(enableLogging = true)
        val output = render(AnsiLevel.TRUECOLOR) {
            printEntry("failechoer", "The failing echoer")
            printEntryResult(false)
            printCommandFailure("QuackFail")
        }
        val stripped = output.replace(Regex("\u001B\\[[0-9;]*m"), "")
        assertTrue(
            "the full output is in the log file" !in stripped,
            "the hint stays silent when everything is already shown"
        )
        assertTrue(
            "…" !in stripped,
            "no ellipsis when nothing was cut"
        )
    }
    @Test
    fun `long entries abbreviate instead of breaking the layout`() {
        val longName = "a".repeat(50)
        val longTask = "b".repeat(50)
        val output = render(AnsiLevel.NONE, width = 40) {
            printEntry(longName, longTask)
            printEntryResult(true)
        }
        output.lineSequence().forEach { line ->
            assertTrue(
                line.length <= 40,
                "every entry line keeps the measure: " + line.length
            )
        }
        assertTrue("…" in output, "the abbreviation marks the cut")
    }

    @Test
    fun `long dry run entries abbreviate as well`() {
        val output = render(AnsiLevel.NONE, width = 40) {
            printEntry("c".repeat(60), "d".repeat(60))
        }
        output.lineSequence().forEach { line ->
            assertTrue(
                line.length <= 40,
                "every dry run line keeps the measure"
            )
        }
    }

    @Test
    fun `long panel lines wrap under the bar`() {
        val longLine = List(30) { "word$it" }.joinToString(" ")
        val output = render(AnsiLevel.NONE, width = 40) {
            printEntry("failechoer", "The failing echoer")
            printEntryResult(false)
            printCommandFailure(longLine)
        }
        output.lineSequence().forEach { line ->
            assertTrue(
                line.length <= 40,
                "every panel line keeps the measure: " + line.length
            )
        }
        assertTrue(
            "word29" in output,
            "every byte of the tool's words survives the wrapping"
        )
    }
    @Test
    fun `the unprocessed files line names what the run left undone`() {
        val output = render(AnsiLevel.TRUECOLOR) {
            printUnprocessedFiles(listOf("B.tex", "C.tex"))
        }
        assertTrue(
            "Files not processed:" in output,
            "the localized label renders"
        )
        assertTrue(
            "B.tex, C.tex" in output,
            "the file names render joined"
        )
    }
}
