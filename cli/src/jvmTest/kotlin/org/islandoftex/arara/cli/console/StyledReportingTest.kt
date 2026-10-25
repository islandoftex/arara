// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.console.styled.StyledConsoleRenderer
import org.islandoftex.arara.cli.console.styled.StyledLayout
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

class StyledReportingTest {
    private fun fixture(test: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara styled reporting ").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        try {
            LinearExecutor.executionOptions = ExecutionOptions()
            Session.userInterfaceOptions = UserInterfaceOptions(locale = MPPLocale("en"))
            LanguageController.loadMessagesFor(MPPLocale("en"))
            test(root)
        } finally {
            snapshot.restore()
            root.removeDirectory()
        }
    }

    private fun capture(block: () -> Unit): String {
        val previous = System.out
        val output = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(output, true, Charsets.UTF_8))
            block()
        } finally {
            System.setOut(previous)
        }
        return output.toString(Charsets.UTF_8)
    }

    private fun run(root: MPPPath, expected: Int, vararg arguments: String): String = capture {
        assertEquals(expected, assertFailsWith<ProgramResult> {
            CLI().parse(listOf("--engine", "yaml", "-d", root.toString()) + arguments)
        }.statusCode)
    }

    @Test
    fun projectConsoleChangesKeepTheCompleteLedgerAndEachInvocationRestoresItsHost() = fixture { root ->
        val presentation = DisplayUtils.presentation
        val renderer = DisplayUtils.renderer
        for (name in listOf("plain", "styled")) {
            val directory = root.resolve(name).apply { createDirectories() }
            directory.resolve("arararc.yaml").writeText("!config\npaths: ['.']\nconsole: $name\n")
            directory.resolve("probe.yaml").writeText("!config\nidentifier: probe\nname: Probe\n" +
                "commands:\n  - command: '@{ return true; }'\narguments: []\n")
            directory.resolve("main.tex").writeText("% arara: probe\n% arara: probe if false\n")
        }
        root.resolve("projects.proj.lua").writeText("""
            return {
              { name = "styled", working_directory = "styled", dependencies = {"plain"},
                files = { ["main.tex"] = {} } },
              { name = "plain", working_directory = "plain", files = { ["main.tex"] = {} } }
            }
        """.trimIndent())
        val output = run(root, 0, "projects.proj.lua")
        val summary = output.substringAfter("Execution summary")
        assertTrue("TOTAL SUCCESS · 2 files, 2 tasks" in output)
        assertTrue("Project: plain" in summary)
        assertTrue("Project: styled" in summary)
        assertTrue(summary.indexOf("Project: plain") < summary.indexOf("Project: styled"))
        assertEquals(2, Regex("main.tex").findAll(summary).count())
        assertTrue("(Probe)" in output)
        assertTrue("> Probe" in output)
        assertSame(presentation, DisplayUtils.presentation)
        assertSame(renderer, DisplayUtils.renderer)
        val next = run(root.resolve("plain"), 0, "--console", "styled", "main.tex")
        assertTrue("TOTAL SUCCESS · 1 file, 1 task" in next)
        assertFalse("Project: styled" in next)
        assertSame(presentation, DisplayUtils.presentation)
    }

    @Test
    fun cliReportsExceptionsAndSkippedOnlyFilesWithoutInventingExecutedTasks() = fixture { root ->
        root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
        root.resolve("first.tex").writeText("% arara: missingrule\n")
        root.resolve("pending.tex").writeText("% arara: missingrule\n")
        val failed = run(root, 2, "--console", "styled", "first.tex", "pending.tex")
        val failedSummary = failed.substringAfter("Execution summary")
        assertTrue("TOTAL FAILURE · 1 file, 0 tasks" in failed)
        assertTrue("FAILURE (2)" in failedSummary)
        assertTrue("pending.tex" in failedSummary)
        assertTrue("Not processed" in failedSummary)
        root.resolve("probe.yaml").writeText("!config\nidentifier: probe\nname: Probe\n" +
            "commands:\n  - command: '@{ return true; }'\narguments: []\n")
        root.resolve("first.tex").writeText("% arara: probe if false\n")
        val skipped = run(root, 0, "--console", "styled", "first.tex")
        assertTrue("TOTAL SUCCESS · 1 file, 0 tasks" in skipped)
        assertTrue("Skipped" in skipped.substringAfter("Execution summary"))
        val dry = run(root, 0, "--console", "styled", "--dry-run", "first.tex")
        assertTrue("DRY RUN · 1 task" in dry)
        assertFalse("SUCCESS" in dry)
        root.resolve("first.tex").writeText("% arara: missingrule\n")
        val dryFailure = run(root, 2, "--console", "styled", "--dry-run", "first.tex")
        assertTrue("DRY RUN · 0 tasks" in dryFailure)
        assertTrue("FAILURE (2)" in dryFailure.substringAfter("Execution summary"))
    }

    @Test
    fun summariesGroupProjectsAndDistinguishRepeatedBasenamesSkippedFailedAndUnprocessedFiles() = fixture { root ->
        val first = root.resolve("first")
        val second = root.resolve("second")
        val files = listOf(
            FileSummary(first.resolve("main.tex"), "first", first, 0, 1250.milliseconds, tasks = 2),
            FileSummary(first.resolve("skipped.tex"), "first", first, 0, 0.milliseconds, skipped = 1),
            FileSummary(second.resolve("main.tex"), "second", second, 7, 2500.milliseconds, tasks = 1),
            FileSummary(second.resolve("pending.tex"), "second", second)
        )
        for (width in listOf(40, 160)) {
            val output = capture {
                StyledConsoleRenderer(Terminal(AnsiLevel.NONE, width = width))
                    .printRunSummary(RunSummary(false, false, files))
            }
            assertTrue("TOTAL FAILURE" in output)
            assertTrue("3 files, 3 tasks" in output)
            assertTrue("Project: first" in output)
            assertTrue("Project: second" in output)
            assertEquals(2, Regex("main.tex").findAll(output).count())
            assertTrue("Skipped" in output)
            assertTrue("FAILURE (7)" in output)
            assertTrue("Not processed" in output)
            assertTrue("1.3 s" in output)
            assertFalse("\u001b" in output)
        }
    }

    @Test
    fun dryRunDoesNotClaimExecutionSuccessAndErrorsStillHaveAFailureVerdict() = fixture { root ->
        val files = listOf(FileSummary(root.resolve("main.tex"), "example", root,
            0, 0.milliseconds, tasks = 1, dryRun = true))
        val renderer = StyledConsoleRenderer(Terminal(AnsiLevel.NONE, width = 100))
        val success = capture { renderer.printRunSummary(RunSummary(true, true, files)) }
        assertTrue("DRY RUN · 1 task" in success)
        assertTrue("[DR]" in success)
        assertFalse("SUCCESS" in success)
        val failure = capture {
            renderer.printRunSummary(RunSummary(false, true, files.map { it.copy(exitCode = 2) }))
        }
        assertTrue("DRY RUN" in failure)
        assertTrue("FAILURE (2)" in failure)
        assertFalse("SUCCESS" in failure)
        val empty = capture { renderer.printRunSummary(RunSummary(false, false, emptyList())) }
        assertTrue("TOTAL FAILURE" in empty)
        assertFalse("Execution summary" in empty)
    }

    @Test
    fun layoutsMeasureUnicodeCellsAndCompactEntriesLeaveRoomForTheVerdict() = fixture { root ->
        val file = root.resolve("very-long-file-name.tex").apply { writeText("source") }
        for (width in listOf(1, 8, 20, 40, 100)) {
            val terminal = Terminal(AnsiLevel.NONE, width = width)
            val layout = StyledLayout(terminal)
            assertEquals(2, layout.cells("界"))
            assertEquals(1, layout.cells("e\u0301"))
            assertTrue(layout.cells(layout.fit("界界e\u0301😀long-name", width)) <= width)
            val unbroken = "界界e\u0301😀verylongunbrokenword"
            val wrapped = capture { layout.print(unbroken) }
            assertEquals(unbroken, wrapped.lineSequence().joinToString(""))
            val output = capture {
                val renderer = StyledConsoleRenderer(terminal)
                renderer.printBanner()
                renderer.printProjectInformation("long project name", root.toString())
                renderer.printFileInformation(ProjectFile(file, FileType("tex", "^%")))
                renderer.printEntry("界界e\u0301 very long rule", "a very long task name")
                renderer.printEntryResult(true)
                renderer.printEntry("Rule", "bad task")
                renderer.printException("long failure message", "verylongunbrokenexceptiondetail")
                renderer.printWrapped("verylongunbrokenprose")
                renderer.printTime(0.005)
                assertTrue(layout.cells(renderer.displayOutputSeparator("a very long title")) <= width)
            }
            assertFalse("\u001b" in output)
            assertTrue(output.lineSequence().all { layout.cells(it) <= maxOf(2, width) }, output)
        }
        val layout = StyledLayout(Terminal(AnsiLevel.NONE))
        assertEquals("0.0 s", layout.seconds(5.milliseconds))
        assertEquals("1.1 s", layout.seconds(1100.milliseconds))
    }

    @Test
    fun failureTailOnlyPointsToALogWhenOneExistsAndNeverRepeatsVerboseOutput() = fixture { _ ->
        val renderer = StyledConsoleRenderer(Terminal(AnsiLevel.NONE, width = 120))
        val tail = (1..20).joinToString("\n") { "tool line $it" } + "\n"
        Session.loggingOptions = LoggingOptions(format = LogFormat.NONE)
        val noLog = capture { renderer.printCommandFailure(tail) }
        assertFalse("tool line 8\n" in noLog)
        assertTrue("tool line 9\n" in noLog)
        assertTrue("tool line 20\n" in noLog)
        assertTrue("..." in noLog)
        assertFalse("log file" in noLog)
        Session.loggingOptions = LoggingOptions(format = LogFormat.JSON)
        assertTrue("the full output is in the log file" in capture { renderer.printCommandFailure(tail) })
        assertFalse("..." in capture { renderer.printCommandFailure("short output\n") })
        LinearExecutor.executionOptions = ExecutionOptions(verbose = true)
        assertEquals("", capture { renderer.printCommandFailure(tail) })
        assertEquals("", capture { renderer.printCommandFailure("  ") })
        capture {
            renderer.printEntry("Rule", "Task")
            renderer.printTaskDetails("command", DirectiveConditional(DirectiveConditionalType.IF, "true"), listOf("Author"))
            renderer.printEntryResult(false)
            renderer.printEntry("Rule", "Task two")
            renderer.printException("Error", null)
        }
    }

    @Test
    fun summaryLabelsFollowTheSelectedCatalogAndDurationUsesItsDecimalSeparator() = fixture { root ->
        for (language in listOf("en", "de", "it", "nl", "pt-BR", "en-QN")) {
            val locale = MPPLocale(language)
            Session.userInterfaceOptions = UserInterfaceOptions(locale = locale)
            LanguageController.loadMessagesFor(locale)
            val messages = LanguageController.messages
            assertTrue(messages.infoDisplayTotalSuccess(1, 1).isNotBlank())
            assertTrue(messages.infoDisplayTotalSuccess(2, 2).isNotBlank())
            assertTrue(messages.infoDisplayTotalFailure(1, 1).isNotBlank())
            assertTrue(messages.infoDisplayTotalFailure(2, 2).isNotBlank())
            assertTrue(messages.infoDisplayDryRun(1).isNotBlank())
            assertTrue(messages.infoDisplayDryRun(2).isNotBlank())
            val output = capture {
                StyledConsoleRenderer(Terminal(AnsiLevel.TRUECOLOR, hyperlinks = true, width = 160)).printRunSummary(
                    RunSummary(true, false, listOf(FileSummary(root.resolve("main file.tex"), "project", root,
                        0, 1250.milliseconds, tasks = 1))))
            }
            assertTrue(messages.infoDisplaySummaryTitle in output)
            assertTrue(messages.infoDisplaySummaryFile in output)
            assertTrue(messages.infoDisplaySummaryTasks in output)
            assertTrue(messages.infoDisplaySummaryTime in output)
            assertTrue("1${locale.decimalSeparator}3 s" in output)
            assertTrue("\u001b]8;" in output)
            assertTrue("main%20file.tex" in output)
        }
    }
}
