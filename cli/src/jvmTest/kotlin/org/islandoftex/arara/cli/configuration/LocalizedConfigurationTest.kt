// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.core.localization.GermanMessages
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalizedConfigurationTest {
    @Test
    fun cliLanguageAppliesBeforeConfigurationParsingAndIsRestoredAfterErrors() {
        val snapshot = ConfigurationSnapshot.capture()
        val stdout = System.out
        val root = Files.createTempDirectory("arara locale override ").toMPPPath()
        val output = ByteArrayOutputStream()
        try {
            root.resolve("arararc.lua").writeText("return { verbose = 'true' }")
            root.resolve("run.tex").writeText("% arara: missing\n")
            System.setOut(PrintStream(output, true, "UTF-8"))
            val status = assertFailsWith<ProgramResult> {
                CLI().parse(listOf("-d", root.toString(), "--language", "de", "run.tex"))
            }.statusCode
            assertEquals(2, status)
            val text = output.toString("UTF-8").replace(Regex("\\s+"), " ")
            assertTrue(text.contains(GermanMessages.errorScriptBoolean("verbose")))
            assertEquals(snapshot, ConfigurationSnapshot.capture())
        } finally {
            System.setOut(stdout)
            snapshot.restore()
            root.removeDirectory()
        }
    }
}
