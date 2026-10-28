// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import org.islandoftex.arara.api.ui.ConsoleSummaries
import org.islandoftex.arara.api.ui.ConsoleTaskDetails
import org.islandoftex.arara.api.ui.ConsoleTasks
import org.islandoftex.arara.api.ui.ConsoleText
import org.islandoftex.arara.core.ui.LocalizedConsoleRenderer

/** Preserves the classic layout using separate text, task and summary renderers. */
class PlainConsoleRenderer private constructor(private val parts: PlainComponents) : LocalizedConsoleRenderer,
    ConsoleText by parts.text, ConsoleTasks by parts.tasks,
    ConsoleTaskDetails by parts.details, ConsoleSummaries by parts.summary {
    /** Create an independent plain presentation. */
    constructor() : this(PlainComponents())
    /** Print prose through this renderer’s shared text component. */
    override fun printWrapped(text: String) = parts.text.printWrapped(text)
}

private class PlainComponents {
    private val state = PlainRenderState()
    val text = PlainTextRenderer(state)
    val tasks = PlainTaskRenderer(state)
    val details = PlainDetailsRenderer(state)
    val summary = PlainSummaryRenderer(state)
}
