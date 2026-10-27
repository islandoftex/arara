// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.diagnostics

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.console.RunPresentation
import org.islandoftex.arara.cli.console.styled.StyledConsoleRenderer
import org.islandoftex.arara.cli.console.styled.StyledLayout
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.session.Command
import org.islandoftex.arara.core.session.Session

class ToolOutputAnalysisTest {
    private fun fixture(test: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara tool diagnostics ").toMPPPath()
        val configuration = ConfigurationSnapshot.capture()
        val presentation = DisplayUtils.presentation
        try {
            LoggingUtils.init()
            DisplayUtils.presentation = RunPresentation()
            test(root)
        } finally {
            LoggingUtils.init()
            DisplayUtils.presentation = presentation
            configuration.restore()
            root.removeDirectory()
        }
    }

    @Test
    fun logsFilterClassifiedRecordsWhileFileSummariesKeepAllWarningsAndErrors() = fixture { root ->
        val log = root.resolve("diagnostics.jsonl")
        val file = ProjectFile(root.resolve("run.tex"), FileType("tex", "^%"))
        val ledger = DisplayUtils.presentation
        ledger.prepare(listOf(Project("project", root, setOf(file))))
        ledger.start(file, false)
        LoggingUtils.setupLogging(LoggingOptions(LogFormat.JSON, false, log, level = LogLevel.ERROR))
        ToolOutputAnalysis.record(Command(listOf("pdflatex")), "pdflatex", """
            This is pdfTeX, Version 3.14
            (./run.tex
            LaTeX Warning: Please rerun.
            C:\run.tex:73: Undefined control sequence.
        """.trimIndent())
        LoggingUtils.init()
        val records = log.readLines().map { Json.parseToJsonElement(it).jsonObject }
        assertEquals(1, records.size)
        assertEquals("ERROR", records.single().getValue("level").jsonPrimitive.content)
        assertEquals("tool.diagnostic", records.single().getValue("event").jsonPrimitive.content)
        val fields = records.single().getValue("data").jsonObject
        assertEquals("pdflatex", fields.getValue("tool").jsonPrimitive.content)
        assertEquals("4", fields.getValue("outputLine").jsonPrimitive.content)
        assertEquals("C:\\run.tex", fields.getValue("sourceFile").jsonPrimitive.content)
        assertEquals("73", fields.getValue("sourceLine").jsonPrimitive.content)
        ledger.finish(0)
        val summary = ledger.summary(true, false)
        assertTrue(summary.success)
        assertEquals(listOf(LogLevel.WARN, LogLevel.ERROR), summary.files.single().diagnostics.map { it.level })
    }

    @Test
    fun explicitToolLevelsAndUnclassifiedOutputAreLoggedWithoutDuplicatingBuffers() = fixture { root ->
        val log = root.resolve("levels.jsonl")
        LoggingUtils.setupLogging(LoggingOptions(LogFormat.JSON, false, log))
        ToolOutputAnalysis.record(Command(listOf("unknown")), null,
            "INFO: native-info\nDEBUG: native-debug\nTRACE: native-trace\nunclassified\n")
        ToolOutputAnalysis.record(Command(listOf("unknown")), null, "")
        LoggingUtils.init()
        val records = log.readLines().map { Json.parseToJsonElement(it).jsonObject }
        assertEquals(listOf("INFO", "DEBUG", "TRACE", "TRACE", "TRACE"),
            records.map { it.getValue("level").jsonPrimitive.content })
        assertEquals(1, records.count { "unclassified" in it.getValue("message").jsonPrimitive.content })
        assertEquals("true", records.last().getValue("data").jsonObject.getValue("empty").jsonPrimitive.content)
    }

    @Test
    fun diagnosticsRemainWithTheirProcessedFileAndRepeatedPassesAreCountedInTheTable() = fixture { root ->
        val ledger = DisplayUtils.presentation
        val first = ProjectFile(root.resolve("first.tex"), FileType("tex", "^%"))
        val second = ProjectFile(root.resolve("second.tex"), FileType("tex", "^%"))
        ledger.prepare(listOf(Project("project", root, setOf(first, second))))
        ledger.start(first, false)
        repeat(2) { ToolOutputAnalysis.record(Command(listOf("pdflatex")), "pdflatex", "LaTeX Warning: first-only") }
        ledger.finish(0)
        ledger.start(second, false)
        ToolOutputAnalysis.record(Command(listOf("biber")), "biber", "ERROR - second-only")
        ledger.finish(1)
        val summary = ledger.summary(false, false)
        assertEquals(2, summary.files.first().diagnostics.size)
        assertEquals(1, summary.files.last().diagnostics.size)
        for (width in listOf(20, 40, 80, 120)) {
            Session.userInterfaceOptions = UserInterfaceOptions(terminalOutputWidthLimit = width)
            val terminal = Terminal(AnsiLevel.NONE, width = width)
            val rendered = capture { StyledConsoleRenderer(terminal).printRunSummary(summary) }
            assertTrue("first-only" in rendered.replace("\n", "").replace(" ", ""), rendered)
            assertTrue("second-only" in rendered.replace("\n", "").replace(" ", ""), rendered)
            assertFalse("\u001b" in rendered)
            val layout = StyledLayout(terminal)
            assertTrue(rendered.lines().all { layout.cells(it) <= width }, rendered)
        }
        ledger.prepare(emptyList())
        assertTrue(ledger.summary(true, false).files.isEmpty())
    }

    private fun capture(action: () -> Unit): String {
        val previous = System.out
        val bytes = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(bytes, true, Charsets.UTF_8))
            action()
        } finally {
            System.setOut(previous)
        }
        return bytes.toString(Charsets.UTF_8)
    }
}
