// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.lua.configuration.LuaLocalConfiguration
import org.islandoftex.arara.mvel.configuration.YamlLocalConfiguration

class ConsoleWidthTest {
    private fun fixture(block: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara console width ").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        try {
            recorded.clear()
            block(root)
        } finally {
            recorded.clear()
            snapshot.restore()
            root.removeDirectory()
        }
    }

    @Test
    fun bothFormatsValidateWidthsAndPreserveInheritedSettings() = fixture { root ->
        val inherited = UserInterfaceOptions(terminalOutputWidth = 90, terminalOutputWidthLimit = 90)
        val legacyHost = object : org.islandoftex.arara.api.configuration.UserInterfaceOptions {
            override val locale = inherited.locale
            override val terminalOutputWidth = 65
        }
        assertNull(legacyHost.terminalOutputWidthLimit)
        for (format in listOf("lua", "yaml")) {
            val config = root.resolve("arararc.$format")
            fun load() = if (format == "lua") LuaLocalConfiguration.load(config, root)
                else YamlLocalConfiguration.load(config)
            fun write(value: String) = config.writeText(if (format == "lua") "return { terminal_width = $value }"
                else "!config\nterminalWidth: $value")
            for (width in listOf(20, 65, 100)) {
                write(width.toString())
                val result = load().toUserInterfaceOptions(inherited)
                assertEquals(width, result.terminalOutputWidth)
                assertEquals(width, result.terminalOutputWidthLimit)
            }
            for (value in listOf("-1", "0", "19", "1.5", "true", "{}", "2147483648")) {
                write(value)
                assertFailsWith<AraraException> { load().toUserInterfaceOptions() }
            }
            config.writeText(if (format == "lua") "return {}" else "!config\n{}")
            assertEquals(inherited, load().toUserInterfaceOptions(inherited))
            val defaults = load().toUserInterfaceOptions()
            assertEquals(65, defaults.terminalOutputWidth)
            assertNull(defaults.terminalOutputWidthLimit)
        }
    }

    @Test
    fun styledWidthsRespectExplicitLimitsAndTerminalResizes() = fixture {
        val terminal = ProgressTestTerminal(width = 100)
        val layout = StyledLayout(terminal.terminal)
        Session.userInterfaceOptions = UserInterfaceOptions()
        assertEquals(100, layout.width)
        Session.userInterfaceOptions = UserInterfaceOptions(terminalOutputWidth = 40, terminalOutputWidthLimit = 40)
        assertEquals(40, layout.width)
        terminal.width = 30
        terminal.terminal.updateSize()
        assertEquals(30, layout.width)
        terminal.width = 120
        terminal.terminal.updateSize()
        assertEquals(40, layout.width)
    }

    @Test
    fun cliOverridesProjectWidthsWithoutLeakingThemIntoLaterProjectsOrTheHost() = fixture { root ->
        val inherited = UserInterfaceOptions()
        Session.userInterfaceOptions = inherited
        for ((name, width) in listOf("first" to 40, "second" to null)) {
            val directory = root.resolve(name).apply { createDirectories() }
            directory.resolve("arararc.lua").writeText("return { paths={cwd}" +
                (width?.let { ", terminal_width=$it" } ?: "") + " }")
            directory.resolve("probe.yaml").writeText("""
                !config
                identifier: probe
                name: Width probe
                commands:
                  - command: '@{ return org.islandoftex.arara.cli.console.styled.ConsoleWidthTest.recordWidth(); }'
                arguments: []
            """.trimIndent())
            directory.resolve("run.tex").writeText("% arara: probe\n")
        }
        root.resolve("projects.proj.lua").writeText("""
            return {
              {name='first', working_directory='first', files={['run.tex']={}}},
              {name='second', working_directory='second', dependencies={'first'}, files={['run.tex']={}}}
            }
        """.trimIndent())
        for (mode in listOf("plain", "styled")) {
            for (requestedWidth in listOf(null, 50)) {
                recorded.clear()
                val flags = requestedWidth?.let { listOf("--terminal-width", it.toString()) } ?: emptyList()
                assertEquals(0, assertFailsWith<ProgramResult> {
                    CLI().parse(listOf("-d", root.toString(), "--console", mode) + flags + "projects.proj.lua")
                }.statusCode)
                assertEquals(if (requestedWidth == null) listOf(40 to 40, 65 to null) else listOf(50 to 50, 50 to 50), recorded)
                assertEquals(inherited, Session.userInterfaceOptions)
            }
        }
    }

    companion object {
        private val recorded = mutableListOf<Pair<Int, Int?>>()

        @JvmStatic
        fun recordWidth(): Boolean {
            recorded.add(Session.userInterfaceOptions.let { it.terminalOutputWidth to it.terminalOutputWidthLimit })
            return true
        }
    }
}
