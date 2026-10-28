// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.cli.console.WatchSummary
import org.islandoftex.arara.core.localization.LanguageController

/** Compact watch history with a wrapped layout on narrow terminals. */
internal class StyledWatchRenderer(private val terminal: Terminal, private val layout: StyledLayout) {
    fun print(summary: WatchSummary) {
        val messages = LanguageController.messages
        terminal.println("")
        val title = messages.infoWatchSummary(summary.runs.size, summary.exitCode)
        layout.print(if (summary.exitCode == 0) terminal.theme.success(title) else terminal.theme.danger(title))
        val headers = listOf(messages.infoWatchCycle, messages.infoWatchProjects, messages.infoDisplaySummaryTime, "")
        val rows = summary.runs.map { run ->
            val status = if (run.exitCode == 0) messages.infoLabelOnSuccess
                else "${messages.infoLabelOnFailure} (${run.exitCode})"
            listOf(run.number.toString(), run.projects.joinToString(", "), layout.seconds(run.elapsed), status)
        }
        val widths = headers.indices.map { column -> (rows.map { it[column] } + headers[column]).maxOf(layout::cells) }
        if (widths.sum() + COLUMN_GAPS <= layout.width) {
            layout.print(row(headers, widths))
            rows.forEach { layout.print(row(it, widths)) }
        } else rows.forEach {
            layout.print("${it[0]} · ${it[1]}")
            layout.print("${it[STATUS]} · ${messages.infoDisplaySummaryTime}: ${it[2]}")
        }
    }

    private fun row(values: List<String>, widths: List<Int>): String = values.mapIndexed { index, value ->
        value + " ".repeat((widths[index] - layout.cells(value)).coerceAtLeast(0))
    }.joinToString("  ").trimEnd()

    private companion object {
        const val COLUMN_GAPS = 6
        const val STATUS = 3
    }
}
