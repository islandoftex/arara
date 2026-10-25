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
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.console.styled.StyledConsoleRenderer
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

class ConsolePresentationTest {
    private fun capture(block: () -> Unit): String {
        val previous = System.out
        val sink = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(sink, true, "UTF-8"))
            block()
        } finally {
            System.setOut(previous)
        }
        return sink.toString("UTF-8")
    }

    @Test
    fun styledOutputUsesColorsOnlyWhenSupportedAndHandlesNarrowTerminals() {
        val snapshot = ConfigurationSnapshot.capture()
        try {
            Session.userInterfaceOptions = UserInterfaceOptions()
            LinearExecutor.executionOptions = ExecutionOptions()
            for (level in listOf(AnsiLevel.NONE, AnsiLevel.TRUECOLOR)) {
                val output = capture {
                    val renderer = StyledConsoleRenderer(Terminal(level, width = 40))
                    renderer.printBanner()
                    renderer.printEntry("Probe", "Task")
                    renderer.printEntryResult(true)
                    renderer.printException("Failure", "Details")
                    renderer.printSkippedDirective("probe")
                    renderer.printRunSummary(false, listOf("pending.tex" to null, "failed.tex" to 7))
                }
                assertEquals(level != AnsiLevel.NONE, "\u001b[" in output)
                assertTrue("SUCCESS" in output)
                assertTrue("Failure" in output)
                assertTrue("Skipped" in output)
                assertTrue("pending.tex" in output)
                assertTrue("Not processed" in output)
                if (level == AnsiLevel.NONE) {
                    assertTrue("> Probe" in output)
                    assertTrue("+ SUCCESS" in output)
                    assertFalse("\u001b" in output)
                }
            }
            capture { StyledConsoleRenderer(Terminal(AnsiLevel.NONE, width = 1)).printException("Error", "Cause") }
        } finally {
            snapshot.restore()
        }
    }

    @Test
    fun commandLineOverridesConfigurationAndCliRestoresItsHostRenderer() {
        val root = Files.createTempDirectory("arara console config ").toMPPPath()
        val renderer = DisplayUtils.renderer
        try {
            root.resolve("run.tex").writeText("% arara: probe\n")
            root.resolve("probe.yaml").writeText("""
                !config
                identifier: probe
                name: Probe
                commands:
                  - command: "@{ return true; }"
                arguments: []
            """.trimIndent())
            for (configuration in listOf("lua", "yaml")) {
                val config = root.resolve("arararc.$configuration")
                config.writeText(if (configuration == "yaml") "!config\npaths: ['.']\nconsole: styled\n"
                    else "return { paths = { cwd }, console = 'styled' }")
                val styled = capture {
                    assertEquals(0, assertFailsWith<ProgramResult> {
                        CLI().parse(listOf("--engine", "yaml", "-d", root.toString(), "run.tex"))
                    }.statusCode)
                }
                assertTrue("> Probe" in styled)
                val plain = capture {
                    assertEquals(0, assertFailsWith<ProgramResult> {
                        CLI().parse(listOf("--engine", "yaml", "-d", root.toString(), "--console", "plain", "run.tex"))
                    }.statusCode)
                }
                assertTrue("(Probe)" in plain)
                assertSame(renderer, DisplayUtils.renderer)
                Files.delete(config.toJVMPath())
            }
            assertEquals(ConsoleMode.PLAIN, ConsoleMode.resolve(null, null))
            assertEquals(ConsoleMode.STYLED, ConsoleMode.resolve(ConsoleMode.STYLED, "invalid"))
            assertFailsWith<AraraException> { ConsoleMode.resolve(null, "invalid") }
        } finally {
            root.removeDirectory()
        }
    }
}
