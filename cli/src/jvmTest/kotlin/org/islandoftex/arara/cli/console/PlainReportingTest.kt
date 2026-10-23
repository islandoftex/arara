// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlainReportingTest {
    private fun fixture(block: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara plain reporting ").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        try {
            root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
            rule(root, "true")
            root.resolve("run.tex").writeText("% arara: probe\n% arara: probe if false\n")
            block(root)
        } finally {
            snapshot.restore()
            root.removeDirectory()
        }
    }

    private fun rule(root: MPPPath, value: String) = root.resolve("probe.yaml").writeText(
        "!config\nidentifier: probe\nname: Probe\ncommands:\n  - command: '@{ return $value; }'\narguments: []\n"
    )

    private fun capture(block: () -> Unit): String {
        val previous = System.out
        val output = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(output, true, Charsets.UTF_8))
            block()
        } finally {
            System.setOut(previous)
        }
        return output.toString(Charsets.UTF_8)
    }

    private fun run(root: MPPPath, expected: Int, vararg args: String): String = capture {
        assertEquals(expected, assertFailsWith<ProgramResult> {
            CLI().parse(listOf("-d", root.toString(), "-L", "en", "--console", "plain") + args)
        }.statusCode)
    }

    @Test
    fun greetingAndTotalsCountEvaluatedTasksWithoutSkippedDirectives() = fixture { root ->
        val output = run(root, 0, "run.tex")
        assertTrue(DisplayUtils.versionLine in output)
        assertFalse("Project:" in output)
        assertTrue("TOTAL SUCCESS · 1 file, 1 task" in output)
        root.resolve("empty.tex").writeText("% arara: probe if false\n")
        val next = run(root, 0, "empty.tex")
        assertTrue("TOTAL SUCCESS · 1 file, 0 tasks" in next)
        assertFalse("1 task" in next)
    }

    @Test
    fun failedRunsNameFilesNeverReached() = fixture { root ->
        rule(root, "false")
        root.resolve("pending.tex").writeText("% arara: probe\n")
        val output = run(root, 1, "run.tex", "pending.tex")
        assertTrue("TOTAL FAILURE · 1 file, 1 task" in output)
        assertTrue("Not processed:" in output)
        assertTrue("pending.tex" in output)
    }

    @Test
    fun dryRunsDoNotClaimSuccessAndTotalsSpanProjectConfigurationChanges() = fixture { root ->
        val dry = run(root, 0, "--dry-run", "run.tex")
        assertTrue("DRY RUN · 2 tasks" in dry)
        assertFalse("SUCCESS" in dry)
        val directory = root.resolve("other").apply { createDirectories() }
        directory.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
        rule(directory, "true")
        directory.resolve("run.tex").writeText("% arara: probe\n")
        root.resolve("project.proj.lua").writeText(
            "return { working_directory = 'other', files = { ['run.tex'] = {} } }\n"
        )
        val output = run(root, 0, "project.proj.lua", "run.tex")
        assertTrue("TOTAL SUCCESS · 2 files, 2 tasks" in output)
        assertEquals(2, Regex("Project:").findAll(output).count())
    }

    @Test
    fun elapsedTimeHasThreeDecimalPlacesInTheSelectedLocale() {
        val snapshot = ConfigurationSnapshot.capture()
        try {
            for ((language, separator) in listOf("en" to ".", "de" to ",")) {
                val locale = MPPLocale(language)
                Session.userInterfaceOptions = UserInterfaceOptions(locale = locale)
                LanguageController.loadMessagesFor(locale)
                assertEquals("0${separator}005", DisplayUtils.elapsedSeconds(0.005))
                assertEquals("1${separator}042", DisplayUtils.elapsedSeconds(1.042))
                val output = capture { PlainConsoleRenderer().printTime(0.005) }
                assertTrue("0${separator}005" in output)
            }
        } finally {
            snapshot.restore()
        }
    }

    @Test
    fun failuresBeforeProjectPreparationDoNotInventTotals() = fixture { root ->
        root.resolve("invalid.proj.lua").writeText("return { files = false }\n")
        val output = run(root, 2, "invalid.proj.lua")
        assertFalse("TOTAL" in output)
    }
}
