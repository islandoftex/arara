// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/**
 * Verifies the terminal presentation: the entry shapes across the
 * execution modes, the exception display with its details section,
 * the conditional and author listings, and the size and time
 * renderings; the file header line, the per-file printed state the
 * facade tracks, the centered separators, and the unprocessed-files
 * closing line. Output is captured from the standard stream; the
 * presentation state rides the session and the execution options,
 * which are saved and restored around every test.
 */
class DisplayTest {
    private val originalOptions = LinearExecutor.executionOptions
    private val originalUserInterface = Session.userInterfaceOptions

    @BeforeTest
    fun fixPresentationState() {
        Session.userInterfaceOptions =
            UserInterfaceOptions(locale = MPPLocale("en"), terminalOutputWidth = 20)
    }

    @AfterTest
    fun restorePresentationState() {
        LinearExecutor.executionOptions = originalOptions
        Session.userInterfaceOptions = originalUserInterface
    }

    private fun <T> withOptions(mode: ExecutionMode, verbose: Boolean, work: () -> T): T {
        LinearExecutor.executionOptions = ExecutionOptions.from(originalOptions)
            .copy(executionMode = mode, verbose = verbose)
        return work()
    }

    private fun capturingOutput(work: () -> Unit): String {
        val sysout = System.out
        val output = ByteArrayOutputStream()
        return try {
            System.setOut(PrintStream(output, true, Charsets.UTF_8))
            work()
            output.toString(Charsets.UTF_8)
        } finally {
            System.setOut(sysout)
        }
    }

    private fun conditional(
        type: DirectiveConditionalType,
        condition: String
    ): DirectiveConditional = object : DirectiveConditional {
        override val type = type
        override val condition = condition
    }

    @Test
    fun `prints entries in every mode shape`() {
        val short = withOptions(ExecutionMode.NORMAL_RUN, false) {
            capturingOutput {
                Display.printEntry("rule", "run")
                Display.printEntryResult(true)
            }
        }
        assertTrue("rule" in short, "the short entry names the rule")
        assertTrue("SUCCESS" in short, "the result label follows the entry")

        val long = withOptions(ExecutionMode.NORMAL_RUN, true) {
            capturingOutput {
                Display.printEntry("rule", "run")
                Display.printEntry("other", "run")
            }
        }
        assertTrue(long.count { it == '\n' } >= 4, "long entries roll with separators")

        val dry = withOptions(ExecutionMode.DRY_RUN, false) {
            capturingOutput {
                Display.printEntry("rule", "run")
            }
        }
        assertTrue("[DR]" in dry, "dry runs mark their entries")
    }

    @Test
    fun `prints exceptions with their details`() {
        val withCause = withOptions(ExecutionMode.NORMAL_RUN, false) {
            capturingOutput {
                Display.printEntry("rule", "run")
                Display.printEntryResult(false)
                Display.printException(
                    AraraException("the failure", RuntimeException("the cause"))
                )
            }
        }
        assertTrue("the failure" in withCause, "the message is displayed")
        assertTrue("the cause" in withCause, "the underlying cause earns a details section")
        assertTrue("FAILURE" in withCause, "the entry result precedes the exception")

        val withoutCause = capturingOutput {
            Display.printEntryResult(true)
            Display.printException(AraraException("plain failure"))
        }
        assertTrue("plain failure" in withoutCause)
        assertFalse("the cause" in withoutCause, "no cause, no details section")
    }

    @Test
    fun `prints exceptions verbosely and names a silent cause by its type`() {
        val verbose = withOptions(ExecutionMode.NORMAL_RUN, true) {
            capturingOutput {
                Display.printEntry("rule", "run")
                Display.printException(AraraException("verbose failure"))
            }
        }
        assertTrue("verbose failure" in verbose, "the verbose mode displays the exception too")

        // a cause without a message of its own is still named in the
        // details section, by its string representation
        val silentCause = capturingOutput {
            Display.printException(
                AraraException("failure", RuntimeException())
            )
        }
        assertTrue("RuntimeException" in silentCause, "a silent cause prints its type")
    }

    @Test
    fun `the result word still lands on a narrow terminal`() {
        Session.userInterfaceOptions =
            UserInterfaceOptions(locale = MPPLocale("en"), terminalOutputWidth = 5)
        val narrow = capturingOutput {
            Display.printEntry("rule", "run")
            Display.printEntryResult(true)
        }
        assertTrue("SUCCESS" in narrow, "the result prints even when narrowed")
    }

    @Test
    fun `prints authors conditionally`() {
        val single = capturingOutput { Display.printAuthors(listOf("Island of TeX")) }
        assertTrue("Island" in single)
        assertTrue("Author:" in single, "a lone author carries the singular label")
        val multiple = capturingOutput { Display.printAuthors(listOf("One", "Two")) }
        assertTrue("One" in multiple && "Two" in multiple)
        assertTrue("Authors:" in multiple, "several authors carry the plural label")
        val none = capturingOutput { Display.printAuthors(emptyList()) }
        // the empty listing runs past the pinned measure and wraps;
        // flattened, the label and its text still read as one line
        val flat = none.lineSequence().joinToString(" ") { it.trim() }
        assertTrue(
            "Authors: No authors provided" in flat,
            "an empty author list has its own label"
        )
    }

    @Test
    fun `prints conditionals unless they are absent`() {
        val present = capturingOutput {
            Display.printConditional(
                conditional(DirectiveConditionalType.IF, "found('log', 'x')")
            )
        }
        assertTrue("found('log', 'x')" in present, "the condition text is displayed")
        val absent = capturingOutput {
            Display.printConditional(
                conditional(DirectiveConditionalType.NONE, "")
            )
        }
        assertTrue(absent.isEmpty(), "the absent conditional prints nothing")
    }

    @Test
    fun `renders byte sizes at every decimal magnitude`() {
        assertEquals("999 B", byteSizeToString(999))
        assertEquals("800 B", byteSizeToString(800))
        assertEquals("1.0 kB", byteSizeToString(1000))
        // the binary neighbour keeps the decimal prefix and truncates
        assertEquals("1.0 kB", byteSizeToString(1024))
        assertEquals("1.5 kB", byteSizeToString(1500))
        assertEquals("1.0 MB", byteSizeToString(1000000))
        assertEquals("1.5 MB", byteSizeToString(1500000))
        assertEquals("1.0 GB", byteSizeToString(1000000000))
        assertEquals("1.0 TB", byteSizeToString(1_000_000_000_000L))
        assertEquals("1.0 PB", byteSizeToString(1_000_000_000_000_000L))
        assertEquals("1.0 EB", byteSizeToString(1_000_000_000_000_000_000L))
    }

    @Test
    fun `prints the elapsed time`() {
        val timed = capturingOutput {
            Display.printEntry("rule", "run")
            Display.printTime(1.5)
        }
        assertTrue("1.500" in timed, "seconds and milliseconds join through the separator")
    }

    @Test
    fun `centers the separators`() {
        assertEquals(
            20,
            Display.displayOutputSeparator("QUACK").length,
            "the message centers within the terminal width"
        )
        assertTrue("QUACK" in Display.displayOutputSeparator("QUACK"))
        assertEquals("-".repeat(20), Display.displaySeparator())
    }

    @Test
    fun `prints the file information`() {
        val file = Files.createTempFile("arara-display-", ".tex")
        Files.writeString(file, "content")
        val information = capturingOutput {
            ProjectFile(MPPPath(file.toString()), FileType("tex", "^%.*"))
                .let { Display.printFileInformation(it) }
        }
        assertTrue(file.fileName.toString() in information, "the file name is displayed")
        assertTrue("7 B" in information, "the file size is displayed")
    }

    @Test
    fun `the printed state of a file tracks its console output`() {
        val file = Files.createTempFile("arara-display-", ".tex")
        Files.writeString(file, "content")
        val projectFile =
            ProjectFile(MPPPath(file.toString()), FileType("tex", "^%.*"))
        capturingOutput { Display.printFileInformation(projectFile) }
        assertFalse(
            Display.printedForFile,
            "the file opens with nothing printed beyond its header"
        )
        capturingOutput { Display.printSkippedDirective("echoone") }
        assertFalse(
            Display.printedForFile,
            "the skip naming does not mark the file: the plain silence " +
                "leaves the facade unable to know"
        )
        capturingOutput { Display.printEntry("echoone", "The echoer") }
        assertTrue(Display.printedForFile, "an entry marks the file as printed")
        capturingOutput { Display.printFileInformation(projectFile) }
        assertFalse(
            Display.printedForFile,
            "the next file resets the state"
        )
    }

    @Test
    fun `the plain renderer names unprocessed files`() {
        val original = System.out
        val sink = java.io.ByteArrayOutputStream()
        System.setOut(java.io.PrintStream(sink, true))
        Display.printUnprocessedFiles(listOf("B.tex", "C.tex"))
        System.setOut(original)
        val output = sink.toString("UTF-8")
        assertTrue(
            "Files not processed:" in output,
            "the localized label renders in the plain voice"
        )
        assertTrue(
            "B.tex, C.tex" in output,
            "the file names render joined"
        )
    }
}
