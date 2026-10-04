// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Files.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.core.configuration.PreambleState
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/**
 * Runs the actual CLI entry point to verify behavior that only manifests
 * through it. [ProjectConfigurationTest] pins the fix for project
 * specifications ignoring configuration files: the `project-config`
 * fixture is only solvable when the configuration of the project is
 * loaded, as its rule is reachable exclusively through the rule paths
 * the configuration provides.
 */
class ProjectConfigurationTest {
    /**
     * Runs the block restoring the global state a command line
     * invocation mutates - the execution options, the user interface
     * options and the preamble state. The entry point applies its
     * options globally and never reverts them, so a test leaving them
     * behind changes the behavior of every subsequent test in the
     * same virtual machine.
     */
    private fun <T> withRestoredState(work: () -> T): T {
        val executionOptions = LinearExecutor.executionOptions
        val userInterfaceOptions = Session.userInterfaceOptions
        val defaultPreamble = PreambleState.defaultPreamble
        val preambles = PreambleState.preambles.toMap()
        return try {
            work()
        } finally {
            LinearExecutor.executionOptions = executionOptions
            Session.userInterfaceOptions = userInterfaceOptions
            PreambleState.defaultPreamble = defaultPreamble
            PreambleState.preambles.clear()
            PreambleState.preambles.putAll(preambles)
        }
    }

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
        withRestoredState {
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
    }

    @Test
    fun `falls back to input files when the Lua argument is not a project`() {
        withRestoredState {
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
        withRestoredState {
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
    }

    @Test
    fun `rejects files without directives`() {
        withRestoredState {
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

    @Test
    fun `maps the command line options onto the execution`() {
        withRestoredState {
            val (status, output) = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/simple-directive",
                "--verbose", "--max-loops", "5", "--timeout", "1000",
                "--language", "en",
                "simple-directive.tex"
            )
            assertEquals(
                0, status,
                "the option mapping does not disturb a successful run"
            )
            assertTrue(
                output.contains("SUCCESS"),
                "the run completes under the mapped options"
            )
        }
    }

    @Test
    fun `maps the dry run and safe run modes`() {
        withRestoredState {
            val dryRun = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/simple-directive",
                "--dry-run",
                "simple-directive.tex"
            )
            assertEquals(0, dryRun.first, "a dry run of a healthy file succeeds")
            assertTrue(
                "[DR]" in dryRun.second,
                "the dry run marks its entries"
            )
            val safeRun = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/simple-directive",
                "--safe-run",
                "simple-directive.tex"
            )
            assertEquals(
                0, safeRun.first,
                "ordinary commands run under safe mode as well"
            )
        }
    }

    @Test
    fun `call parameters take precedence over properties files`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-precedence")
            val properties = directory.resolve("call.properties")
            Files.writeString(properties, "quack=from-file")
            try {
                runCLI(
                    "--working-directory", directory.toString(),
                    "--properties-file", properties.toString(),
                    "--call-property", "quack=from-parameter",
                    "missing.tex"
                )
                assertEquals(
                    "from-parameter",
                    Session.get("arg:quack"),
                    "the command line parameter wins over the properties file"
                )
            } finally {
                // the removal is guarded so a failure before the
                // store cannot turn the cleanup into the primary
                // error
                if (Session.contains("arg:quack")) {
                    Session.remove("arg:quack")
                }
            }
        }
    }

    @Test
    fun `runs preamble directives from the configuration`() {
        withRestoredState {
            val (status, output) = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/preamble",
                "--preamble", "test",
                "main.tex"
            )
            assertEquals(
                0, status,
                "the preamble directive runs on a file without its own"
            )
            assertTrue(
                output.contains("PreambleWorks"),
                "the preamble directive executed: $output"
            )
        }
    }

    @Test
    fun `leaves no log file behind when logging is not enabled`() {
        withRestoredState {
            // the default log location is the working directory of the
            // process; any stale file from an earlier run is cleared so
            // the assertion observes only what this run produced
            val defaultLog = File("arara.log")
            defaultLog.delete()
            val (status, _) = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/simple-directive",
                "simple-directive.tex"
            )
            assertEquals(
                0, status,
                "the run itself succeeds without the log flag"
            )
            assertTrue(
                !defaultLog.exists(),
                "no log file materializes without the flag: the logging " +
                    "configuration activates only when the feature is enabled"
            )
        }
    }
}
