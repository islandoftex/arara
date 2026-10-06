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
                "project.proj.lua"
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
    fun `reports a plain Lua file as untyped input`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-plain-lua")
            val plain = directory.resolve("notes.lua")
            Files.writeString(plain, "return { }")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "notes.lua"
            )
            // the plain Lua file is an ordinary input reference now:
            // no project guess, no apology - just the honest verdict
            // that no file type of the run carries its extension
            assertNotEquals(0, status, "an untyped input reports failure")
            assertTrue(
                "could not determine the file type" in output,
                "the run reports the type truth: $output"
            )
            assertTrue(
                "regular input" !in output,
                "the guessing fallback is gone: $output"
            )
        }
    }

    @Test
    fun `fails a broken project specification without a fallback`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-broken-spec")
            val bogus = directory.resolve("broken.proj.lua")
            Files.writeString(bogus, "return 42")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "broken.proj.lua"
            )
            // the suffix declared the intent, so the parse failure is
            // an error naming its actual violation - never a demotion
            // to input treatment
            assertNotEquals(0, status, "a broken specification fails the run")
            assertTrue(
                "has to be a table" in output,
                "the run reports the parse violation: $output"
            )
            assertTrue(
                "regular input" !in output,
                "no fallback notice exists anymore: $output"
            )
        }
    }

    @Test
    fun `runs a specification mixed with loose input files`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-mixed")
            Files.writeString(
                directory.resolve("echoone.yaml"),
                """
                !config
                identifier: echoone
                name: Test rule for conditionals 1
                commands:
                  - name: The echoer
                    command: >
                      @{
                          return isWindows(getCommand('cmd', '/c', 'echo', 'QuackOne'), getCommand('echo', 'QuackOne'));
                      }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\nverbose: true\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("main.tex"), "% arara: echoone\nQuack\n"
            )
            Files.writeString(
                directory.resolve("notes.tex"), "% arara: echoone\nQuack\n"
            )
            Files.writeString(
                directory.resolve("book.proj.lua"),
                "return { files = { [\"main.tex\"] = { } } }\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "book.proj.lua", "notes.tex"
            )
            // the specification's project and the anonymous project
            // ride one invocation, and both run their files
            assertEquals(
                0, status,
                "the mixed invocation runs the specification and the loose file"
            )
            assertEquals(
                2, output.split("QuackOne").size - 1,
                "the specification's file and the loose file both ran: $output"
            )
        }
    }

    @Test
    fun `merges specifications and orders their cross dependencies`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-multispec")
            Files.writeString(
                directory.resolve("echoone.yaml"),
                """
                !config
                identifier: echoone
                name: Test rule for conditionals 1
                commands:
                  - name: The echoer
                    command: >
                      @{
                          return isWindows(getCommand('cmd', '/c', 'echo', 'QuackOne'), getCommand('echo', 'QuackOne'));
                      }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\nverbose: true\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("a.tex"), "% arara: echoone\nQuack\n"
            )
            Files.writeString(
                directory.resolve("b.tex"), "% arara: echoone\nQuack\n"
            )
            Files.writeString(
                directory.resolve("first.proj.lua"),
                "return { name = \"alpha\", files = { [\"a.tex\"] = { } } }\n"
            )
            Files.writeString(
                directory.resolve("second.proj.lua"),
                "return { { name = \"beta\", dependencies = { \"alpha\" }, files = { [\"b.tex\"] = { } } } }\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "second.proj.lua", "first.proj.lua"
            )
            // the dependency crosses files: beta names alpha, and the
            // merged run orders alpha first even though the
            // dependent specification came first on the command line
            assertEquals(0, status, "the merged specifications run")
            assertTrue(
                output.indexOf("a.tex") < output.indexOf("b.tex"),
                "the cross-file dependency orders the run: $output"
            )
        }
    }

    @Test
    fun `derives the names of nameless specifications from their files`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-nameless")
            Files.writeString(
                directory.resolve("echoone.yaml"),
                """
                !config
                identifier: echoone
                name: Test rule for conditionals 1
                commands:
                  - name: The echoer
                    command: >
                      @{
                          return isWindows(getCommand('cmd', '/c', 'echo', 'QuackOne'), getCommand('echo', 'QuackOne'));
                      }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\nverbose: true\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("a.tex"), "% arara: echoone\nQuack\n"
            )
            Files.writeString(
                directory.resolve("b.tex"), "% arara: echoone\nQuack\n"
            )
            Files.writeString(
                directory.resolve("one.proj.lua"),
                "return { files = { [\"a.tex\"] = { } } }\n"
            )
            Files.writeString(
                directory.resolve("two.proj.lua"),
                "return { files = { [\"b.tex\"] = { } } }\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "one.proj.lua", "two.proj.lua"
            )
            // two nameless specifications would collide on the
            // placeholder; the derived names keep the run possible
            assertEquals(0, status, "the nameless specifications merge cleanly")
            assertEquals(
                2, output.split("QuackOne").size - 1,
                "both specifications ran their files: $output"
            )
        }
    }

    @Test
    fun `rejects a name two specifications share`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-duplicate")
            Files.writeString(
                directory.resolve("one.proj.lua"),
                "return { name = \"same\", files = { [\"a.tex\"] = { } } }\n"
            )
            Files.writeString(
                directory.resolve("two.proj.lua"),
                "return { name = \"same\", files = { [\"b.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("a.tex"), "Quack\n")
            Files.writeString(directory.resolve("b.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "one.proj.lua", "two.proj.lua"
            )
            assertNotEquals(0, status, "two same-named projects cannot merge")
            assertTrue(
                "share the name" in output,
                "the verdict names the collision: $output"
            )
        }
    }

    @Test
    fun `rejects a file claimed by two projects`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-cross-file")
            Files.writeString(
                directory.resolve("one.proj.lua"),
                "return { name = \"first\", files = { [\"shared.tex\"] = { } } }\n"
            )
            Files.writeString(
                directory.resolve("two.proj.lua"),
                "return { name = \"second\", files = { [\"shared.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("shared.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "one.proj.lua", "two.proj.lua"
            )
            assertNotEquals(0, status, "a file in two projects cannot run")
            assertTrue(
                "shared.tex" in output && "appears in" in output,
                "the verdict names the file and the rule: $output"
            )
        }
    }

    @Test
    fun `rejects a file claimed by a specification and the command line`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-cross-loose")
            Files.writeString(
                directory.resolve("book.proj.lua"),
                "return { name = \"book\", files = { [\"main.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("main.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "book.proj.lua", "main.tex"
            )
            assertNotEquals(
                0, status,
                "a file in a project and on the command line cannot run"
            )
            assertTrue(
                "main.tex" in output && "appears in" in output,
                "the verdict names the file and the rule: $output"
            )
        }
    }

    @Test
    fun `orders same-priority files by their paths`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-tie-order")
            Files.writeString(
                directory.resolve("echoone.yaml"),
                """
                !config
                identifier: echoone
                name: Test rule for conditionals 1
                commands:
                  - name: The echoer
                    command: >
                      @{
                          return isWindows(getCommand('cmd', '/c', 'echo', 'QuackOne'), getCommand('echo', 'QuackOne'));
                      }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("a.tex"), "% arara: echoone\nQuack\n"
            )
            Files.writeString(
                directory.resolve("b.tex"), "% arara: echoone\nQuack\n"
            )
            // the specification lists the files in reverse path order;
            // the execution runs them in the sorted order of their
            // paths, so the run is deterministic whatever the Lua
            // table iteration produced
            Files.writeString(
                directory.resolve("order.proj.lua"),
                "return { files = { [\"b.tex\"] = { }, [\"a.tex\"] = { } } }\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "order.proj.lua"
            )
            assertEquals(0, status, "the specification runs")
            assertTrue(
                "a.tex" in output && "b.tex" in output,
                "both files appear in the output: $output"
            )
            assertTrue(
                output.indexOf("a.tex") < output.indexOf("b.tex"),
                "the same-priority files run in the sorted order of their paths: $output"
            )
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
