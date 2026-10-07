// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

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
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/**
 * Verifies the plain console renderer: the error section on its
 * dashed rule, the word landing like a result on an open entry
 * line, the closing verdict and its tallies, and the per-file flag
 * hygiene that keeps the separations honest.
 */
class PlainConsoleRendererTest {
    private val originalOut = System.out
    private var originalOptions = LinearExecutor.executionOptions
    private var originalUIOptions = Session.userInterfaceOptions

    @BeforeTest
    fun pinTheGlobals() {
        // the localized labels drive the layout and the execution
        // options decide which entry shape renders, so the suite
        // pins the defaults before any capture
        originalOptions = LinearExecutor.executionOptions
        originalUIOptions = Session.userInterfaceOptions
        Session.userInterfaceOptions = UserInterfaceOptions()
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)
        LinearExecutor.executionOptions = ExecutionOptions()
    }

    @AfterTest
    fun restoreTheGlobals() {
        // the renderers and the suites around them share the
        // session-wide state, so the pins leave nothing behind
        Session.userInterfaceOptions = originalUIOptions
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)
        LinearExecutor.executionOptions = originalOptions
        System.setOut(originalOut)
    }

    private fun render(block: PlainConsoleRenderer.() -> Unit): String {
        val sink = ByteArrayOutputStream()
        System.setOut(PrintStream(sink, true))
        // the banner opens every run and resets the per-run state of
        // the shared object, so every capture starts hermetic
        PlainConsoleRenderer.printBanner()
        PlainConsoleRenderer.block()
        System.setOut(originalOut)
        return sink.toString("UTF-8")
    }

    private fun anyFile() = ProjectFile(
        MPPPath(
            File.createTempFile("plain", ".tex")
                .apply { writeText("quack"); deleteOnExit() }.absolutePath
        ),
        FileType("tex", "^%.*")
    )

    @Test
    fun `the banner names the run with the version line`() {
        val output = render { }
        assertTrue(
            "arara 8.0.0-SNAPSHOT" in output,
            "the version line renders under the logo"
        )
        assertTrue(
            "the cool TeX automation tool." in output,
            "the banner carries the trademark slogan"
        )
    }

    @Test
    fun `the standalone error titles its section on the dashed rule`() {
        val output = render {
            printException("the run could not proceed", null)
        }
        val lines = output.lineSequence().toList()
        val title = lines.indexOfFirst { it.startsWith("ERROR ") }
        assertTrue(title > 0, "the section title prints")
        assertFalse(
            lines[title].startsWith(" "),
            "no alignment spaces before the word on a line with no entry"
        )
        assertTrue(
            lines[title].endsWith("-"),
            "the dashed rule runs from the word to the edge"
        )
        assertEquals(
            "the run could not proceed",
            lines[title + 1],
            "the message follows the rule directly, with no blank between"
        )
    }

    @Test
    fun `the mid-flight error lands its word on the open entry line`() {
        val output = render {
            printEntry("echoone", "The echoer")
            printException("the tool never started", null)
        }
        val line = output.lineSequence().first { "ERROR" in it }
        assertTrue(
            "(echoone) The echoer" in line,
            "the word lands on the open entry line like a result"
        )
        assertTrue(
            line.endsWith("ERROR"),
            "the word lands in the column the results align into"
        )
    }

    @Test
    fun `the error after a landed result still titles its section`() {
        val output = render {
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printException("the second task never ran", null)
        }
        val lines = output.lineSequence().toList()
        val title = lines.indexOfFirst { it.startsWith("ERROR ") }
        assertTrue(
            title > 0 && lines[title - 1].isEmpty(),
            "the landed result separates the section title, and the title still arrives"
        )
    }

    @Test
    fun `a new file opens its error block without a stale separation`() {
        val first = anyFile()
        val second = anyFile()
        val output = render {
            printFileInformation(first)
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printFileInformation(second)
            printException("the second file failed", null)
        }
        val lines = output.lineSequence().toList()
        val title = lines.indexOfFirst { it.startsWith("ERROR ") }
        assertTrue(
            title > 1 && lines[title - 1].isEmpty(),
            "the header's own blank separates the title"
        )
        assertFalse(
            lines[title - 2].isEmpty(),
            "no stale separation blank from the prior file's landed result"
        )
    }

    @Test
    fun `the verdict names the run in plain words`() {
        val output = render {
            printFileInformation(anyFile())
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printRunSummary(true)
        }
        assertTrue(
            "TOTAL SUCCESS" in output,
            "the closing verdict names the success"
        )
        assertTrue("1 file, 1 task" in output, "the verdict counts the run")
    }

    @Test
    fun `the failed verdict names the failure`() {
        val output = render {
            printFileInformation(anyFile())
            printEntry("echoone", "The echoer")
            printEntryResult(false)
            printRunSummary(false)
        }
        assertTrue(
            "TOTAL FAILURE" in output,
            "the closing verdict names the failure"
        )
    }

    @Test
    fun `the dry run closes with its own verdict`() {
        LinearExecutor.executionOptions = ExecutionOptions(
            executionMode = ExecutionMode.DRY_RUN
        )
        val output = render {
            printFileInformation(anyFile())
            printEntry("pdflatex", "The engine run")
            printRunSummary(true)
        }
        assertTrue(
            "DRY RUN" in output,
            "the dry run closes with its own verdict"
        )
    }

    @Test
    fun `an empty ledger closes in silence`() {
        val output = render {
            printException("the file was never started", null)
            printRunSummary(false)
        }
        assertFalse(
            "TOTAL" in output,
            "a run that started no file has nothing to summarize"
        )
    }

    @Test
    fun `the banner resets the tallies`() {
        val first = render {
            printFileInformation(anyFile())
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printRunSummary(true)
        }
        val second = render {
            printFileInformation(anyFile())
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printRunSummary(true)
        }
        assertTrue("1 file, 1 task" in first, "the first run counts itself")
        assertTrue(
            "1 file, 1 task" in second,
            "the banner resets the tallies of the shared object"
        )
        assertFalse("2 files" in second, "the tally never accumulates across runs")
    }

    @Test
    fun `the failed landing separates the verdict`() {
        val output = render {
            printFileInformation(anyFile())
            printEntry("echoone", "The echoer")
            printEntryResult(false)
            printRunSummary(false)
        }
        val lines = output.lineSequence().toList()
        val failure = lines.indexOfFirst { it.endsWith("FAILURE") }
        assertTrue(
            failure > 0 && lines[failure + 1].isEmpty(),
            "the failed landing carries its own separation"
        )
        assertTrue(
            "TOTAL FAILURE" in lines[failure + 2],
            "the verdict starts on a fresh line"
        )
    }

    @Test
    fun `the mid-flight error separates its message from the landing`() {
        val output = render {
            printEntry("echoone", "The echoer")
            printException("the tool never started", null)
        }
        val lines = output.lineSequence().toList()
        val landing = lines.indexOfFirst { it.endsWith("ERROR") }
        assertTrue(
            landing > 0 && lines[landing + 1].isEmpty(),
            "the completed entry line separates from the message"
        )
        assertEquals(
            "the tool never started",
            lines[landing + 2],
            "the message follows the blank"
        )
    }

    @Test
    fun `a landed success with a failed run separates its verdict`() {
        val output = render {
            printFileInformation(anyFile())
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
    fun `the verdict separates the time line`() {
        val output = render {
            printFileInformation(anyFile())
            printEntry("echoone", "The echoer")
            printEntryResult(true)
            printRunSummary(true)
            printTime(0.25)
        }
        val lines = output.lineSequence().toList()
        val verdict = lines.indexOfFirst { "TOTAL SUCCESS" in it }
        assertTrue(
            verdict > 0 && lines[verdict + 1].isEmpty(),
            "the time line separates from the verdict"
        )
    }

    @Test
    fun `the unprocessed files line wraps at the terminal width`() {
        val names = (1..15).map { index -> "averylongfilenamehere$index.tex" }
        val output = render {
            printUnprocessedFiles(names)
        }
        output.lineSequence().forEach { line ->
            assertTrue(
                line.length <= 80,
                "the names wrap under the terminal width: '$line'"
            )
        }
        assertTrue(
            "averylongfilenamehere15.tex" in output,
            "the whole list still prints"
        )
    }
}
