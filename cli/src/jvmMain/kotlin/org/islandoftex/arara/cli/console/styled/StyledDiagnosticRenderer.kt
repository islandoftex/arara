// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.cli.console.FileSummary
import org.islandoftex.arara.cli.diagnostics.ToolDiagnostic
import org.islandoftex.arara.core.localization.LanguageController

/** Per-file diagnostic tables, retaining complete messages at narrow terminal widths. */
internal class StyledDiagnosticRenderer(private val terminal: Terminal, private val layout: StyledLayout) {
    fun print(file: FileSummary, displayPath: String) {
        if (file.diagnostics.isEmpty()) return
        val messages = LanguageController.messages
        terminal.println("")
        layout.print("${messages.infoDisplayDiagnosticsTitle}: $displayPath")
        val groups = file.diagnostics.groupBy { listOf(it.tool, it.level.name, it.file, it.line, it.message) }
        val rows = groups.values.map { occurrences ->
            val diagnostic = occurrences.first()
            listOf(label(diagnostic), diagnostic.tool, diagnostic.message, occurrences.size.toString())
        }
        val headers = listOf(messages.infoDisplayDiagnosticLevel, messages.infoDisplayDiagnosticTool,
            messages.infoDisplayDiagnosticMessage, messages.infoDisplayDiagnosticCount)
        val fixed = listOf(LEVEL, TOOL, COUNT).map { column ->
            (rows.map { it[column] } + headers[column]).maxOf(layout::cells)
        }
        if (fixed.sum() + GAPS + MINIMUM_MESSAGE <= layout.width) {
            table(headers, rows, listOf(fixed[0], fixed[1], layout.width - fixed.sum() - GAPS, fixed[2]))
        } else {
            val labelWidth = (rows.map { it[LEVEL] } + headers[LEVEL]).maxOf(layout::cells)
            val first = minOf(layout.width / NARROW_RATIO, labelWidth)
                .coerceAtLeast(1)
            val compact = rows.map { listOf(it[LEVEL], "${it[TOOL]} · ${it[COUNT]} × ${it[MESSAGE]}") }
            table(listOf(headers[LEVEL], headers[MESSAGE]), compact,
                listOf(first, (layout.width - first - GAP).coerceAtLeast(1)))
        }
    }

    private fun table(headers: List<String>, rows: List<List<String>>, widths: List<Int>) {
        printRow(headers, widths)
        layout.print(widths.joinToString("  ") { "-".repeat(it) })
        rows.forEach { printRow(it, widths) }
    }

    private fun printRow(values: List<String>, widths: List<Int>) {
        val wrapped = values.mapIndexed { index, text -> layout.render(text, widths[index]).lines() }
        repeat(wrapped.maxOf { it.size }) { line ->
            layout.print(wrapped.mapIndexed { column, lines ->
                val text = lines.getOrNull(line).orEmpty()
                text + " ".repeat((widths[column] - layout.cells(text)).coerceAtLeast(0))
            }.joinToString("  ").trimEnd())
        }
    }

    private fun label(diagnostic: ToolDiagnostic): String = LanguageController.messages.run {
        if (diagnostic.level == LogLevel.ERROR) terminal.theme.danger(infoLabelOnError)
        else terminal.theme.warning(infoLabelOnWarning)
    }

    private companion object {
        const val LEVEL = 0
        const val TOOL = 1
        const val MESSAGE = 2
        const val COUNT = 3
        const val GAP = 2
        const val GAPS = 6
        const val MINIMUM_MESSAGE = 16
        const val NARROW_RATIO = 3
    }
}
