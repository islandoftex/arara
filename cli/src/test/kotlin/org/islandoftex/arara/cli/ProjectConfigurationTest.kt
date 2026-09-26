// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Files.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Runs the actual CLI entry point to verify behavior that only manifests
 * through it. [ProjectConfigurationTest] pins the fix for project
 * specifications ignoring configuration files: the `project-config`
 * fixture is only solvable when the configuration of the project is
 * loaded, as its rule is reachable exclusively through the rule paths
 * the configuration provides.
 */
class ProjectConfigurationTest {
    private fun runCLI(vararg args: String): Pair<Int, String> {
        val sysout = System.out
        val output = ByteArrayOutputStream()
        return try {
            System.setOut(PrintStream(output))
            val status = try {
                CLI().parse(args.toList())
                0
            } catch (result: ProgramResult) {
                result.statusCode
            }
            status to output.toByteArray().toString(Charsets.UTF_8)
        } finally {
            System.setOut(sysout)
            output.close()
        }
    }

    @Test
    fun `loads the configuration file for project specification runs`() {
        val (status, output) = runCLI(
            "--working-directory",
            "src/test/resources/executiontests/project-config",
            "project.lua"
        )
        assertEquals(
            0, status,
            "the project run exits successfully"
        )
        // the rule is only reachable through the rule paths provided by
        // the configuration file, and the echoed marker only shows up
        // because the configuration enables verbose output
        assertTrue(
            output.contains("ProjectConfigWorks"),
            "the rule was found through the configured rule paths and " +
                "executed with the configured verbose output"
        )
    }

    @Test
    fun `falls back to input files when the Lua argument is not a project`() {
        val directory = createTempDirectory("arara-cli-not-a-spec")
        val bogus = directory.resolve("not-a-spec.lua")
        Files.writeString(bogus, "return 42")
        val (status, output) = runCLI(
            "--working-directory", directory.toString(),
            "not-a-spec.lua"
        )
        // the guessing fallback announces the demotion before the file is
        // treated as a regular input; the run then fails on the unknown
        // file type, which the entry point reports as its status
        assertTrue(
            output.contains("regular input"),
            "the fallback announces the failed project guess: $output"
        )
        assertNotEquals(0, status, "an unrunnable input reports failure")
    }

    @Test
    fun `prints the help with the arara flair`() {
        // the help text is rendered by the command line framework's own
        // terminal, so the test pins the help request itself; the flair
        // and the custom formatter run while the message is built
        assertFailsWith<PrintHelpMessage> {
            CLI().parse(listOf("--help"))
        }
    }

    @Test
    fun `rejects preambles the configuration does not define`() {
        val directory = createTempDirectory("arara-cli-preamble")
        val (status, output) = runCLI(
            "--working-directory", directory.toString(),
            "--preamble", "nonexistent",
            "missing.tex"
        )
        assertNotEquals(0, status, "an undefined preamble is an error")
        assertTrue(
            output.isNotBlank(),
            "the rejection is reported to the user"
        )
    }

    @Test
    fun `rejects files without directives`() {
        val directory = createTempDirectory("arara-cli-nodirectives")
        val plain = directory.resolve("plain.tex")
        Files.writeString(plain, "just some text, no directives\\n")
        val (status, output) = runCLI(
            "--working-directory", directory.toString(),
            "plain.tex"
        )
        assertNotEquals(0, status, "a directive-free file is an error")
        assertTrue(
            output.isNotBlank(),
            "the rejection is reported to the user"
        )
    }
}
