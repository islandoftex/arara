// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalInfo
import com.github.ajalt.mordant.terminal.TerminalInterface
import kotlin.time.Duration

internal class ProgressTestTerminal(
    private val level: AnsiLevel = AnsiLevel.TRUECOLOR,
    private val interactive: Boolean = true,
    private val input: Boolean = interactive,
    private val cursorClaim: Boolean = false,
    var width: Int = 80
) : TerminalInterface {
    private val output = StringBuilder()
    val terminal = Terminal(terminalInterface = this)

    override fun info(ansiLevel: AnsiLevel?, hyperlinks: Boolean?, outputInteractive: Boolean?,
                      inputInteractive: Boolean?): TerminalInfo = TerminalInfo(level, false, interactive, input, cursorClaim)

    @Synchronized
    override fun completePrintRequest(request: PrintRequest) {
        output.append(request.text)
        if (request.trailingLinebreak) output.append('\n')
    }

    override fun readLineOrNull(hideInput: Boolean): String? = null

    override fun getTerminalSize(): Size = Size(width, 24)

    @Synchronized
    fun output(): String = output.toString()
}

/** Deliberately permits cancelled callbacks to fire, as a real queued dispatch can. */
internal class ManualProgressScheduler : ProgressScheduler {
    class Task(val delay: Duration, val interval: Duration, val action: () -> Unit) : ProgressTask {
        var cancelled = false
        override fun cancel() { cancelled = true }
    }

    val tasks = mutableListOf<Task>()
    var closed = false

    override fun schedule(delay: Duration, interval: Duration, action: () -> Unit): ProgressTask =
        Task(delay, interval, action).also { tasks.add(it) }

    override fun close() { closed = true }
}
