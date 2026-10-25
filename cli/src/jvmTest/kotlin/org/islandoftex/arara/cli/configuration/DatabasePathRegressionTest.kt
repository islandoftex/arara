// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.core.session.Session

class DatabasePathRegressionTest {
    @Test
    fun changeTrackingUsesTheConfiguredSubdirectoryAcrossInvocationsAndEngines() {
        for (format in listOf("lua", "yaml")) {
            val root = Files.createTempDirectory("arara database config ").toMPPPath()
            val snapshot = ConfigurationSnapshot.capture()
            try {
                root.resolve("cache").createDirectories()
                root.resolve("arararc.$format").writeText(if (format == "yaml") """
                    !config
                    paths: ['.']
                    dbname: cache/hashes.yaml
                """.trimIndent() else "return { paths = { cwd }, database = 'cache/hashes.yaml' }")
                root.resolve("probe.$format").writeText(if (format == "yaml") """
                    !config
                    identifier: probe
                    name: Probe
                    commands:
                      - command: "@{ getSession().put('database-changed', changed(reference)); return true; }"
                    arguments: []
                """.trimIndent() else """
                    return { identifier = 'probe', name = 'Probe', commands = {
                        { command = function(ctx)
                            get_session().put('database-changed', changed(current_file())); return true
                        end }
                    } }
                """.trimIndent())
                root.resolve("run.tex").writeText("% arara: probe\n")
                for (expected in listOf(true, false)) {
                    val status = assertFailsWith<ProgramResult> {
                        CLI().parse(listOf("-d", root.toString(), "--engine", format, "run.tex"))
                    }.statusCode
                    assertEquals(0, status)
                    assertEquals(expected, Session["database-changed"], "$format / expected $expected")
                }
                assertTrue(root.resolve("cache/hashes.yaml").isRegularFile)
                assertFalse(root.resolve("hashes.yaml").exists)
                assertEquals(snapshot, ConfigurationSnapshot.capture())
            } finally {
                snapshot.restore()
                Session.clear()
                root.removeDirectory()
            }
        }
    }
}
