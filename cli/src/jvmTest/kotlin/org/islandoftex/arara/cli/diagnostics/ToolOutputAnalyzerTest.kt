// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.diagnostics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.Command

class ToolOutputAnalyzerTest {
    @Test
    fun catalogCoversEveryShippedRuleAndAllDeclaredProfilesExist() {
        val rules = MPPPath("../rules").listEntries().map { it.fileName }
        for (suffix in listOf(".lua", ".yaml")) {
            assertEquals(rules.filter { it.endsWith(suffix) }
                .map { it.removePrefix("arara-rule-").removeSuffix(suffix) }.toSet(),
                ToolProfiles.tools.map { it.rule }.toSet())
        }
        val profiles = resource("/org/islandoftex/arara/cli/diagnostics/patterns.tsv").map { it[0] }.toSet() + "none"
        ToolProfiles.tools.forEach { tool ->
            assertTrue(tool.profiles.all { it in profiles }, tool.toString())
            tool.executables.forEach { executable ->
                assertTrue(ToolProfiles.profiles(executable, tool.rule).containsAll(tool.profiles))
            }
        }
    }

    @Test
    fun upstreamDiagnosticFormatsKeepTheirSeverity() {
        resource("/diagnostics/messages.tsv").forEach { columns ->
            val (tool, expected, text) = columns
            val records = ToolOutputAnalyzer.analyze(Command(listOf(tool)), text)
            assertEquals(LogLevel.valueOf(expected), records.single().level, "$tool: $text")
            assertEquals(text, records.single().message)
        }
    }

    @Test
    fun multilineWarningsAndErrorsPreserveLocationsAndStripTerminalEscapes() {
        val diagnostics = ToolOutputAnalyzer.analyze(Command(listOf("C:\\TeX Live\\pdflatex.exe")), """
            This is pdfTeX, Version 3.14
            Package hyperref Warning: Token not allowed in a PDF string
            (hyperref)                removing invalid command on input line 42.
            C:\Users\TeX Files\run.tex:73: Undefined control sequence.
            l.73 \undefined
            Output written on run.pdf (1 page).
        """.trimIndent().replace("Package hyperref", "\u001b[33mPackage hyperref").replace("Token", "\u001b[0mToken"))
        assertEquals(listOf(LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR, LogLevel.INFO), diagnostics.map { it.level })
        assertEquals(42, diagnostics[1].line)
        assertEquals(73, diagnostics[2].line)
        assertEquals("C:\\Users\\TeX Files\\run.tex", diagnostics[2].file)
        assertTrue("(hyperref)" in diagnostics[1].message)
        assertTrue("l.73" in diagnostics[2].message)
        assertTrue(diagnostics.none { '\u001b' in it.message })
    }

    @Test
    fun sourceLocationsDistinguishErrorsFromExplicitWarnings() {
        val cases = listOf(
            Triple("asy", "drawing.asy: 12.4: no matching variable", LogLevel.ERROR),
            Triple("gnuplot", "\"plot.gp\" line 12: undefined variable: missing", LogLevel.ERROR),
            Triple("sketch", "drawing.sk(12) : warning, degenerate polygon", LogLevel.WARN),
            Triple("makeindex", "!! Input index error (file = run.idx, line = 12):", LogLevel.ERROR),
            Triple("pdflatex", "run.tex:12: LaTeX Warning: Please rerun.", LogLevel.WARN)
        )
        cases.forEach { (tool, message, severity) ->
            val diagnostic = ToolOutputAnalyzer.analyze(Command(listOf(tool)), message).single()
            assertEquals(severity, diagnostic.level)
            assertEquals(12, diagnostic.line)
            assertTrue(diagnostic.file != null, message)
        }
    }

    @Test
    fun filenamesAndZeroWarningCountsAreNotDiagnostics() {
        val samples = mapOf(
            "pdflatex" to "(./error.tex\nNo pages of output.\nTranscript written on warning.log.",
            "makeindex" to "Generating output file run.ind....done (8 lines written, 0 warnings).",
            "qpdf" to "checking warning.pdf\nNo syntax or stream encoding errors found; the file may still contain errors",
            "context" to "resolvers       > warning.tex",
            "unknown" to "writing error.log\nthis text discusses warnings and errors"
        )
        samples.forEach { (tool, text) ->
            assertTrue(ToolOutputAnalyzer.analyze(Command(listOf(tool)), text).none { it.level >= LogLevel.WARN }, text)
        }
    }

    @Test
    fun executableAliasesWrappersAndUnknownToolsUseTheAppropriateGrammar() {
        assertEquals(LogLevel.ERROR, ToolOutputAnalyzer.analyze(
            Command(listOf("C:\\Windows\\cmd.exe", "/C", "del", "run.aux")), "Access is denied.").single().level)
        assertEquals(LogLevel.WARN, ToolOutputAnalyzer.analyze(
            Command(listOf("/opt/bin/custom-wrapper")), "LaTeX Warning: Please rerun.", "latexmk").single().level)
        assertEquals(LogLevel.TRACE, ToolOutputAnalyzer.analyze(Command(listOf("unknown")), "unstructured text").single().level)
        assertEquals(LogLevel.ERROR, ToolOutputAnalyzer.analyze(Command(listOf("unknown")), "ERROR: Failed").single().level)
        assertTrue(ToolOutputAnalyzer.analyze(Command(listOf("pdflatex")), "\n\r\n").isEmpty())
    }

    private fun resource(path: String): List<List<String>> = checkNotNull(javaClass.getResourceAsStream(path))
        .bufferedReader().useLines { lines ->
            lines.filter { it.isNotBlank() && !it.startsWith('#') }.map { it.split('\t') }.toList()
        }
}
