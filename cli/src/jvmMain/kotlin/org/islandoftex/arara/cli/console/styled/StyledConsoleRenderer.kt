// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.TimeSource
import org.islandoftex.arara.api.ui.ConsoleProgress
import org.islandoftex.arara.api.ui.ConsoleSummaries
import org.islandoftex.arara.api.ui.ConsoleTaskDetails
import org.islandoftex.arara.api.ui.ConsoleTasks
import org.islandoftex.arara.api.ui.ConsoleText
import org.islandoftex.arara.core.ui.LocalizedConsoleRenderer

/** Terminal-cell-aware presentation composed from focused renderers sharing one state. */
class StyledConsoleRenderer private constructor(private val parts: StyledComponents) : LocalizedConsoleRenderer,
    ConsoleText by parts.text, ConsoleTasks by parts.tasks,
    ConsoleTaskDetails by parts.state.details, ConsoleProgress by parts.state.progressOutput,
    ConsoleSummaries by parts.summary {
    /** Create a renderer with the supplied clock and progress scheduler. */
    internal constructor(terminal: Terminal, clock: TimeSource, schedulerFactory: () -> ProgressScheduler) :
        this(StyledComponents(StyledRenderState(terminal, clock, schedulerFactory)))
    /** Create a renderer using the default monotonic progress scheduler. */
    constructor(terminal: Terminal) : this(terminal, TimeSource.Monotonic, { ExecutorProgressScheduler() })
    /** Print prose through this renderer’s shared text component. */
    override fun printWrapped(text: String) = parts.text.printWrapped(text)
}

private class StyledComponents(val state: StyledRenderState) {
    val text = StyledTextRenderer(state)
    val tasks = StyledTaskRenderer(state)
    val summary = StyledRunRenderer(state)
}
