// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.lua.configuration.LuaLocalConfiguration
import org.islandoftex.arara.mvel.configuration.YamlLocalConfiguration

class AnimationConfigurationTest {
    private fun fixture(test: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara animation configuration ").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        try {
            recorded.clear()
            test(root)
        } finally {
            snapshot.restore()
            recorded.clear()
            root.removeDirectory()
        }
    }

    @Test
    fun bothFormatsAcceptBooleansAndPreserveInheritedUiSettingsWhenOmitted() = fixture { root ->
        val inherited = UserInterfaceOptions(MPPLocale("de"), 100, "styled", enableAnimations = false)
        for (format in listOf("lua", "yaml")) {
            val file = root.resolve("arararc.$format")
            fun load() = if (format == "yaml") YamlLocalConfiguration.load(file)
                else LuaLocalConfiguration.load(file, root)
            for (enabled in listOf(false, true)) {
                file.writeText(if (format == "yaml") "!config\nanimations: $enabled\n"
                    else "return { animations = $enabled }")
                assertEquals(inherited.copy(enableAnimations = enabled), load().toUserInterfaceOptions(inherited))
            }
            file.writeText(if (format == "yaml") "!config\n{}\n" else "return {}")
            assertEquals(inherited, load().toUserInterfaceOptions(inherited))
            assertTrue(load().toUserInterfaceOptions().enableAnimations)
            val invalidValues = if (format == "lua") listOf("'false'", "5", "{}")
                else listOf("not-a-boolean", "5", "{}")
            for (invalid in invalidValues) {
                file.writeText(if (format == "yaml") "!config\nanimations: $invalid\n"
                    else "return { animations = $invalid }")
                assertFailsWith<AraraException> { load() }
            }
        }
        val legacyOptions = object : org.islandoftex.arara.api.configuration.UserInterfaceOptions {
            override val locale = MPPLocale("en")
            override val terminalOutputWidth = 65
        }
        assertTrue(legacyOptions.enableAnimations)
    }

    @Test
    fun cliPreservesEachProjectsAnimationSettingAndNeverClosesTheHostsRenderer() = fixture { root ->
        Session.userInterfaceOptions = UserInterfaceOptions(enableAnimations = true)
        val previous = DisplayUtils.renderer
        var hostClosed = false
        val host = object : ConsoleRenderer by PlainConsoleRenderer() {
            override fun close() { hostClosed = true }
        }
        DisplayUtils.renderer = host
        try {
            for (name in listOf("disabled", "inherited", "enabled")) {
                val directory = root.resolve(name).apply { createDirectories() }
                val flag = when (name) {
                    "disabled" -> "animations: false\n"
                    "enabled" -> "animations: true\n"
                    else -> ""
                }
                directory.resolve("arararc.yaml").writeText("!config\npaths: ['.']\nconsole: styled\n$flag")
                directory.resolve("main.tex").writeText("% arara: probe\n")
                directory.resolve("probe.yaml").writeText("!config\nidentifier: probe\nname: Probe\ncommands:\n" +
                    "  - command: '@{ return org.islandoftex.arara.cli.console.styled." +
                    "AnimationConfigurationTest.recordAnimations(); }'\narguments: []\n")
            }
            root.resolve("projects.proj.lua").writeText("""
                return {
                  { name = 'disabled', working_directory = 'disabled', files = { ['main.tex'] = {} } },
                  { name = 'inherited', working_directory = 'inherited', dependencies = {'disabled'},
                    files = { ['main.tex'] = {} } },
                  { name = 'enabled', working_directory = 'enabled', dependencies = {'inherited'},
                    files = { ['main.tex'] = {} } }
                }
            """.trimIndent())
            assertEquals(0, assertFailsWith<ProgramResult> {
                CLI().parse(listOf("--engine", "yaml", "-d", root.toString(), "projects.proj.lua"))
            }.statusCode)
            assertEquals(listOf(false, true, true), recorded)
            assertTrue(Session.userInterfaceOptions.enableAnimations)
            assertSame(host, DisplayUtils.renderer)
            assertFalse(hostClosed)
            root.resolve("enabled/main.tex").writeText("% arara: missingrule\n")
            assertEquals(2, assertFailsWith<ProgramResult> {
                CLI().parse(listOf("--engine", "yaml", "-d", root.resolve("enabled").toString(), "main.tex"))
            }.statusCode)
            assertSame(host, DisplayUtils.renderer)
            assertFalse(hostClosed)
        } finally {
            DisplayUtils.renderer = previous
        }
    }

    companion object {
        private val recorded = mutableListOf<Boolean>()

        @JvmStatic
        fun recordAnimations(): Boolean {
            recorded.add(Session.userInterfaceOptions.enableAnimations)
            return true
        }
    }
}
