// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.cli.console.FileSummary
import org.islandoftex.arara.cli.console.RunSummary
import org.islandoftex.arara.core.localization.LanguageController

/** Group the invocation ledger by project, including files never reached. */
internal class StyledSummaryRenderer(private val terminal: Terminal, private val layout: StyledLayout) {
    fun print(summary: RunSummary) {
        val messages = LanguageController.messages
        val tasks = summary.files.sumOf { it.tasks }
        val reached = summary.files.count { it.exitCode != null }
        val verdict = when {
            summary.dryRun -> messages.infoDisplayDryRun(tasks)
            summary.success -> messages.infoDisplayTotalSuccess(reached, tasks)
            else -> messages.infoDisplayTotalFailure(reached, tasks)
        }
        terminal.println("")
        layout.print(when {
            !summary.success -> terminal.theme.danger(verdict)
            summary.dryRun -> terminal.theme.muted(verdict)
            else -> terminal.theme.success(verdict)
        })
        if (summary.dryRun && !summary.success) layout.print(terminal.theme.danger(messages.infoLabelOnFailure))
        if (summary.files.isEmpty()) return
        terminal.println("")
        layout.print(terminal.theme.muted(messages.infoDisplaySummaryTitle))
        summary.files.groupBy { it.project to it.directory }.forEach { (project, files) ->
            terminal.println("")
            if (project.first.isNotBlank()) {
                layout.print("${messages.infoLabelProject} ${project.first} (${project.second})")
            }
            printFiles(files)
            files.forEach { StyledDiagnosticRenderer(terminal, layout).print(it, displayPath(it)) }
        }
    }

    private fun printFiles(files: List<FileSummary>) {
        val messages = LanguageController.messages
        val rows = files.map { file ->
            listOf(displayPath(file), file.tasks.toString(), file.elapsed?.let(layout::seconds) ?: "-", status(file))
        }
        val headers = listOf(messages.infoDisplaySummaryFile, messages.infoDisplaySummaryTasks,
            messages.infoDisplaySummaryTime, "")
        val widths = headers.indices.map { column ->
            (rows.map { it[column] } + headers[column]).maxOf(layout::cells)
        }
        if (widths.sum() + COLUMN_GAPS <= layout.width) {
            layout.print(row(headers, widths))
            files.zip(rows).forEach { (file, columns) ->
                layout.print(row(columns, widths, file.path.toJVMPath().toUri().toASCIIString()))
            }
        } else {
            files.zip(rows).forEach { (_, columns) ->
                layout.print("${columns[FILE]} · ${columns[STATUS]}")
                layout.print("${messages.infoDisplaySummaryTasks}: ${columns[TASKS]} · " +
                    "${messages.infoDisplaySummaryTime}: ${columns[TIME]}")
            }
        }
    }

    private fun row(columns: List<String>, widths: List<Int>, url: String? = null): String =
        columns.mapIndexed { index, text ->
            val padding = " ".repeat(widths[index] - layout.cells(text))
            if (index == FILE) {
                val title = if (url != null && terminal.terminalInfo.ansiHyperLinks)
                    TextStyle(hyperlink = url)(text) else text
                title + padding
            } else padding + text
        }.joinToString("  ").trimEnd()

    private fun displayPath(file: FileSummary): String = runCatching {
        file.directory.toJVMPath().relativize(file.path.toJVMPath()).toString()
    }.getOrDefault(file.path.toString())

    private fun status(file: FileSummary): String = LanguageController.messages.run {
        when {
            file.exitCode == null -> infoConsoleUnprocessed
            file.exitCode != 0 -> "$infoLabelOnFailure (${file.exitCode})"
            file.dryRun -> "[DR]"
            file.tasks == 0 && file.skipped > 0 -> infoConsoleSkipped
            else -> infoLabelOnSuccess
        }
    }

    private companion object {
        const val FILE = 0
        const val TASKS = 1
        const val TIME = 2
        const val STATUS = 3
        const val COLUMN_GAPS = 6
    }
}
