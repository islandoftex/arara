// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import org.islandoftex.arara.api.reporting.WatchRun
import org.islandoftex.arara.api.reporting.WatchSummary
import org.islandoftex.arara.cli.console.styled.StyledConsoleRenderer
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.session.Session

class WatchRenderingTest {

    @Test
    fun styledHistoryKeepsAllRunsAndWrapsAtNarrowWidths() {
        val previousOutput = System.out
        val previousUi = Session.userInterfaceOptions
        try {
            for (width in listOf(20, 40, 120)) {
                val bytes = ByteArrayOutputStream()
                System.setOut(PrintStream(bytes, true, Charsets.UTF_8))
                Session.userInterfaceOptions = UserInterfaceOptions(terminalOutputWidthLimit = width)
                val renderer = StyledConsoleRenderer(Terminal(ansiLevel = AnsiLevel.NONE, interactive = false))
                renderer.printWatchSummary(WatchSummary(listOf(
                    WatchRun(1, setOf("first", "second"), 7, 1200.milliseconds),
                    WatchRun(2, setOf("first", "second"), 0, 500.milliseconds)), 0))
                renderer.close()
                val text = bytes.toString(Charsets.UTF_8)
                assertTrue("2 builds" in text.replace(Regex("\\s+"), " "), text)
                assertTrue("FAILURE" in text && "SUCCESS" in text, text)
                assertTrue(text.lineSequence().all { it.length <= width }, text)
            }
        } finally {
            System.setOut(previousOutput)
            Session.userInterfaceOptions = previousUi
        }
    }
}
