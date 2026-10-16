// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Files.createTempDirectory
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.configuration.ConfigurationFiles
import org.islandoftex.arara.core.configuration.PreambleState
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/**
 * Runs the actual CLI entry point to verify behavior that only manifests
 * through it: the project specification flow (the configuration loading
 * that made the `project-config` fixture solvable, the merging of
 * several specifications with their cross-file dependencies, the name
 * derivation and the name collisions, the cycle verdict naming only
 * its members, and the degenerate specification names), the
 * configuration resolution (the project directory, the user home
 * fallback, the directory that poses as a configuration file), and
 * the command line surface (the option mapping onto the execution,
 * the dry and safe run modes, the help request, the preambles, the
 * call parameter precedence, and the log file that never materializes
 * without the flag).
 */
class ProjectConfigurationTest {
    /**
     * Runs the block restoring the global state a command line
     * invocation mutates - the execution options, the user interface
     * options, the message catalog and the preamble state. The entry
     * point applies its options globally and never reverts them, so
     * a test leaving them behind changes the behavior of every
     * subsequent test in the same virtual machine.
     */
    private fun <T> withRestoredState(work: () -> T): T {
        val executionOptions = LinearExecutor.executionOptions
        val userInterfaceOptions = Session.userInterfaceOptions
        val messages = LanguageController.messages
        val defaultPreamble = PreambleState.defaultPreamble
        val preambles = PreambleState.preambles.toMap()
        return try {
            work()
        } finally {
            LinearExecutor.executionOptions = executionOptions
            Session.userInterfaceOptions = userInterfaceOptions
            LanguageController.messages = messages
            PreambleState.defaultPreamble = defaultPreamble
            PreambleState.preambles.clear()
            PreambleState.preambles.putAll(preambles)
        }
    }

    private fun runCLI(vararg args: String): Pair<Int, String> =
        runOn(CLI(), *args)

    /**
     * Runs the provided command line instance against the arguments,
     * so a test can drive one instance through several runs the way
     * an embedding would.
     */
    private fun runOn(cli: CLI, vararg args: String): Pair<Int, String> {
        val sysout = System.out
        val output = ByteArrayOutputStream()
        return try {
            System.setOut(PrintStream(output))
            val status = try {
                cli.parse(args.toList())
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
    fun `two nameless specifications merge without colliding`() {
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
    fun `rejects a specification named after the localized anonymous label`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-label-collision")
            Files.writeString(
                directory.resolve("collide.proj.lua"),
                "return { name = \"Arquivos avulsos\", files = { [\"c.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("c.tex"), "Quack\n")
            Files.writeString(directory.resolve("b.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "-L", "pt-BR",
                "collide.proj.lua", "b.tex"
            )
            assertNotEquals(
                0,
                status,
                "a specification named after the localized label cannot merge"
            )
            assertTrue(
                "compartilham" in output,
                "the verdict names the collision: $output"
            )
        }
    }

    @Test
    fun `rejects a specification file named exactly after the suffix`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-degenerate-spec")
            Files.writeString(
                directory.resolve(".proj.lua"),
                "return { files = { [\"a.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("a.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                ".proj.lua"
            )
            assertNotEquals(
                0,
                status,
                "the degenerate file name cannot derive a project"
            )
            assertTrue(
                "cannot be blank" in output,
                "the verdict names the blank name: $output"
            )
        }
    }

    @Test
    fun `rejects a specification deriving a whitespace name`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-space-spec")
            Files.writeString(
                directory.resolve(" .proj.lua"),
                "return { files = { [\"a.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("a.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                " .proj.lua"
            )
            assertNotEquals(
                0,
                status,
                "a whitespace name cannot identify a project"
            )
            assertTrue(
                "cannot be blank" in output,
                "the verdict names the blank name: $output"
            )
        }
    }

    @Test
    fun `a degenerately named specification with explicit names runs`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-named-spec")
            Files.writeString(
                directory.resolve(".proj.lua"),
                "return { name = \"good\", files = { [\"a.tex\"] = { } } }\n"
            )
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("echoone.yaml"),
                """
                !config
                identifier: echoone
                name: The echoer
                commands:
                  - name: The echoer
                    command: >
                      @{
                        return isWindows(getCommand('cmd', '/c', 'echo', 'Quack'), getCommand('echo', 'Quack'));
                      }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(directory.resolve("a.tex"), "% arara: echoone\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                ".proj.lua"
            )
            assertEquals(
                0,
                status,
                "the declared name carries the run: $output"
            )
            assertTrue("TOTAL SUCCESS" in output, "the file ran: $output")
        }
    }

    @Test
    fun `a reused instance forgets the previous run's anonymous project`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-stale-anonymous")
            Files.writeString(directory.resolve("loose.tex"), "Quack\n")
            Files.writeString(
                directory.resolve("collide.proj.lua"),
                "return { name = \"Single files\", files = { [\"c.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("c.tex"), "Quack\n")
            val cli = CLI()
            runOn(
                cli,
                "--working-directory", directory.toString(),
                "loose.tex"
            )
            val (status, output) = runOn(
                cli,
                "--working-directory", directory.toString(),
                "collide.proj.lua"
            )
            assertTrue(
                "share the name" !in output,
                "the stale anonymous project does not reject the run: $output"
            )
            assertTrue(
                "c.tex" in output,
                "the specification's file reached processing: $output"
            )
        }
    }

    @Test
    fun `a failed resolution does not report the previous run's files`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-reuse")
            Files.writeString(
                directory.resolve("good.proj.lua"),
                "return { files = { [\"a.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("a.tex"), "Quack\n")
            val cli = CLI()
            runOn(
                cli,
                "--working-directory", directory.toString(),
                "good.proj.lua"
            )
            val (status, output) = runOn(
                cli,
                "--working-directory", directory.toString(),
                "missing.proj.lua"
            )
            assertNotEquals(
                0,
                status,
                "the missing specification fails the run"
            )
            assertTrue(
                "a.tex" !in output,
                "the previous run's files are not the failure's unreached files: $output"
            )
        }
    }

    @Test
    fun `the cycle verdict names only the cycle members`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-cycle")
            Files.writeString(
                directory.resolve("cycle.proj.lua"),
                """
                return {
                  { name = "a", dependencies = { "b" }, files = { ["a.tex"] = { } } },
                  { name = "b", dependencies = { "a" }, files = { ["b.tex"] = { } } },
                  { name = "c", dependencies = { "a" }, files = { ["c.tex"] = { } } }
                }
                """.trimIndent()
            )
            Files.writeString(directory.resolve("a.tex"), "Quack\n")
            Files.writeString(directory.resolve("b.tex"), "Quack\n")
            Files.writeString(directory.resolve("c.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "cycle.proj.lua"
            )
            assertNotEquals(0, status, "a cyclic specification fails the run")
            assertTrue(
                "(a, b)" in output,
                "the verdict names the cycle members: $output"
            )
            assertTrue(
                "(a, b, c)" !in output,
                "the dependent is not accused of the cycle: $output"
            )
        }
    }

    @Test
    fun `rejects a specification named Single files`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-english-collision")
            Files.writeString(
                directory.resolve("collide.proj.lua"),
                "return { name = \"Single files\", files = { [\"c.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("c.tex"), "Quack\n")
            Files.writeString(directory.resolve("b.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "collide.proj.lua", "b.tex"
            )
            assertNotEquals(0, status, "the model name cannot be claimed")
            assertTrue(
                "share the name" in output,
                "the verdict names the collision: $output"
            )
        }
    }

    @Test
    fun `a missing specification gets its own verdict`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-missing-spec")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "missing.proj.lua"
            )
            assertNotEquals(0, status, "the missing specification fails the run")
            assertTrue(
                "could not find the specification file" in output,
                "the verdict names the file: $output"
            )
            assertTrue(
                "with any of the extensions" !in output,
                "no extension enumeration for a direct open: $output"
            )
        }
    }

    @Test
    fun `rejects stray dependencies in the merged specifications`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-stray")
            Files.writeString(
                directory.resolve("one.proj.lua"),
                "return {\n  { name = \"one\", dependencies = { \"nowhere\" }, files = { [\"a.tex\"] = { } } }\n}\n"
            )
            Files.writeString(
                directory.resolve("two.proj.lua"),
                "return {\n  { name = \"two\", dependencies = { \"nowhere\" }, files = { [\"b.tex\"] = { } } }\n}\n"
            )
            Files.writeString(directory.resolve("a.tex"), "Quack\n")
            Files.writeString(directory.resolve("b.tex"), "Quack\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "one.proj.lua", "two.proj.lua"
            )
            assertNotEquals(0, status, "a dependency nothing carries fails the run")
            assertTrue(
                "list (nowhere) as dependencies" in output,
                "the verdict lists the stray name: $output"
            )
            assertTrue(
                "nowhere, nowhere" !in output,
                "the same missing name is listed once: $output"
            )
        }
    }

    @Test
    fun `the merge validation runs before the configuration loads`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-ordering")
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
            // a configuration that cannot load: were the loads to
            // come first, its error would surface before the merge
            // validation's own verdict
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\nfiletypes:\n  - extension"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "one.proj.lua", "two.proj.lua"
            )
            assertNotEquals(0, status, "the run fails either way")
            assertTrue(
                "share the name" in output,
                "the merge verdict comes first: $output"
            )
        }
    }

    @Test
    fun `a directory named like a configuration file is not one`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-config-trap")
            Files.createDirectory(directory.resolve("araraconfig.yaml"))
            val found = ConfigurationFiles.configFileForProject(
                Project("p", directory.toMPPPath(), emptySet())
            )
            assertNotEquals(
                directory.resolve("araraconfig.yaml").toString(),
                found?.toString(),
                "the directory never poses as the configuration"
            )
        }
    }

    @Test
    fun `the configuration falls back to the user home`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-config-project")
            val home = createTempDirectory("arara-cli-config-home")
            Files.writeString(
                home.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n"
            )
            val original = System.getProperty("user.home")
            System.setProperty("user.home", home.toString())
            try {
                val found = ConfigurationFiles.configFileForProject(
                    Project("p", directory.toMPPPath(), emptySet())
                )
                assertEquals(
                    home.resolve("arararc.yaml").toString(),
                    found?.toString(),
                    "the home configuration answers when the project has none"
                )
            } finally {
                System.setProperty("user.home", original ?: directory.toString())
            }
        }
    }

    @Test
    fun `the invocation configuration provides the loose file's type`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-loose-type")
            Files.writeString(
                directory.resolve("spec.proj.lua"),
                "return { files = { [\"s.tex\"] = { } } }\n"
            )
            Files.writeString(directory.resolve("s.tex"), "% arara: echoone\n")
            Files.writeString(directory.resolve("loose.my"), "% arara: echoone\n")
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\nfiletypes:\n  - extension: my\n    pattern: '^\\s*%\\s*'\n"
            )
            Files.writeString(
                directory.resolve("echoone.yaml"),
                """
                !config
                identifier: echoone
                name: The echoer
                commands:
                  - name: The echoer
                    command: >
                      @{
                        return isWindows(getCommand('cmd', '/c', 'echo', 'Quack'), getCommand('echo', 'Quack'));
                      }
                arguments: []
                """.trimIndent()
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "spec.proj.lua", "loose.my"
            )
            assertEquals(
                0,
                status,
                "the configured type carries the loose file: $output"
            )
            assertTrue(
                "loose.my" in output,
                "the loose file resolves and runs: $output"
            )
            assertTrue(
                "Project: spec" in output,
                "the nameless specification shows its file's base name: $output"
            )
        }
    }

    @Test
    fun `a higher priority file runs first within its project`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-priority-order")
            Files.writeString(
                directory.resolve("order.proj.lua"),
                """
                return {
                  files = {
                    ["low.tex"] = { priority = 1 },
                    ["high.tex"] = { priority = 2 }
                  }
                }
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("echoone.yaml"),
                """
                !config
                identifier: echoone
                name: The echoer
                commands:
                  - name: The echoer
                    command: >
                      @{
                        return isWindows(getCommand('cmd', '/c', 'echo', 'Quack'), getCommand('echo', 'Quack'));
                      }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(directory.resolve("low.tex"), "% arara: echoone\n")
            Files.writeString(directory.resolve("high.tex"), "% arara: echoone\n")
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "order.proj.lua"
            )
            assertEquals(0, status, "the project runs")
            assertTrue(
                output.indexOf("high.tex") < output.indexOf("low.tex"),
                "the higher priority runs first: $output"
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
        // the framework throws its own message for the help request and
        // renders the text at the catching side, so the pin has two
        // halves: the request raises the framework's help message, and
        // the command's own formatted help carries the flair the custom
        // formatter builds
        val cli = CLI()
        assertFailsWith<PrintHelpMessage> {
            cli.parse(listOf("--help"))
        }
        val text = cli.getFormattedHelp() ?: ""
        assertTrue(
            "cool TeX automation tool" in text,
            "the tagline renders in the help"
        )
        assertTrue(
            "arara" in text,
            "the logo names the tool"
        )
    }

    @Test
    fun `an invocation with no arguments prints the help`() {
        withRestoredState {
            assertFailsWith<PrintHelpMessage> { runCLI() }
        }
    }

    @Test
    fun `propagates a task's requested exit code to the process`() {
        withRestoredState {
            val (status, _) = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/halt-code",
                "halt-code.tex"
            )
            assertEquals(
                42, status,
                "the requested halt status reaches the process unchanged"
            )
        }
    }

    @Test
    fun `a zero max-loops value is a usage error`() {
        withRestoredState {
            // the framework rejects the value during parsing, before
            // the run could begin, so the rejection surfaces as its
            // own usage error rather than a failed run
            val exception = assertFailsWith<BadParameterValue> {
                runCLI(
                    "--working-directory",
                    "src/test/resources/executiontests/simple-directive",
                    "--max-loops", "0",
                    "simple-directive.tex"
                )
            }
            assertTrue(
                "minimum" in exception.message!!,
                "the usage error names the lower bound: ${exception.message}"
            )
        }
    }

    @Test
    fun `a zero timeout is a usage error`() {
        withRestoredState {
            val exception = assertFailsWith<BadParameterValue> {
                runCLI(
                    "--working-directory",
                    "src/test/resources/executiontests/simple-directive",
                    "--timeout", "0",
                    "simple-directive.tex"
                )
            }
            assertTrue(
                "minimum" in exception.message!!,
                "the usage error names the lower bound: ${exception.message}"
            )
        }
    }

    @Test
    fun `an unknown engine is a usage error`() {
        withRestoredState {
            val exception = assertFailsWith<BadParameterValue> {
                runCLI(
                    "--working-directory",
                    "src/test/resources/executiontests/simple-directive",
                    "--engine", "nonsense",
                    "simple-directive.tex"
                )
            }
            assertTrue(
                "yaml, lua" in exception.message!!,
                "the usage error names the choices: ${exception.message}"
            )
        }
    }

    @Test
    fun `a nonexistent working directory is a usage error`() {
        withRestoredState {
            val exception = assertFailsWith<BadParameterValue> {
                runCLI(
                    "--working-directory", "/nowhere-quack",
                    "simple-directive.tex"
                )
            }
            assertTrue(
                "nowhere-quack" in exception.message!!,
                "the usage error names the path: ${exception.message}"
            )
        }
    }

    @Test
    fun `--whole-file extracts directives beyond the header through the CLI`() {
        withRestoredState {
            // the header-only half runs first: the whole-file half
            // leaves the global options with header parsing off, and
            // the entry point applies its options globally, never
            // reverting them
            val (withoutFlag, _) = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/whole-file",
                "body.tex"
            )
            assertNotEquals(
                0, withoutFlag,
                "the header alone holds no directive"
            )
            val (withFlag, _) = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/whole-file",
                "--whole-file", "body.tex"
            )
            assertEquals(
                0, withFlag,
                "the body directive runs under the whole-file flag"
            )
        }
    }

    @Test
    fun `--silent negates a standing verbose configuration`() {
        withRestoredState {
            // the fixture's configuration asks for the tool output to
            // echo, so the run without the alias shows it and the run
            // with the alias is the one that has to defeat it
            val (chatty, _) = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/simple-directive",
                "simple-directive.tex"
            )
            assertEquals(0, chatty, "the verbose configuration runs")
            val (quiet, output) = runCLI(
                "--working-directory",
                "src/test/resources/executiontests/simple-directive",
                "--silent",
                "simple-directive.tex"
            )
            assertEquals(0, quiet, "the silenced run still succeeds")
            assertFalse(
                "QuackOne" in output,
                "the alias negates the configuration's echo request"
            )
        }
    }

    @Test
    fun `--verbose lifts a quiet configuration`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-lift")
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("echoone.yaml"),
                """
                !config
                identifier: echoone
                name: The echoer
                commands:
                  - name: The echoer
                    command: >
                      @{ return isWindows(getCommand('cmd', '/c', 'echo', 'QuackOne'), getCommand('echo', 'QuackOne')); }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("main.tex"),
                "% arara: echoone\n"
            )
            val (quiet, plain) = runCLI(
                "--working-directory", directory.toString(),
                "main.tex"
            )
            assertEquals(0, quiet, "the quiet configuration runs")
            assertFalse(
                "QuackOne" in plain,
                "the quiet configuration echoes nothing"
            )
            val (chatty, output) = runCLI(
                "--working-directory", directory.toString(),
                "--verbose",
                "main.tex"
            )
            assertEquals(0, chatty, "the lifted run still succeeds")
            assertTrue(
                "QuackOne" in output,
                "the verbose flag lifts the quiet configuration"
            )
        }
    }

    /**
     * Builds a runnable echo fixture in a fresh directory: the echo
     * rules and a one-directive document, so a test can shape the
     * configuration and the document to its need.
     */
    private fun echoFixture(document: String = "% arara: echoone\n"): Path {
        val directory = createTempDirectory("arara-cli-fixture")
        Files.writeString(
            directory.resolve("echoone.yaml"),
            """
            !config
            identifier: echoone
            name: The echoer
            commands:
              - name: The echoer
                command: >
                  @{ return isWindows(getCommand('cmd', '/c', 'echo', 'QuackOne'), getCommand('echo', 'QuackOne')); }
            arguments: []
            """.trimIndent()
        )
        Files.writeString(
            directory.resolve("preambleer.yaml"),
            """
            !config
            identifier: preambleer
            name: The preamble echoer
            commands:
              - name: The preamble echoer
                command: >
                  @{ return isWindows(getCommand('cmd', '/c', 'echo', 'PreambleFirst'), getCommand('echo', 'PreambleFirst')); }
            arguments: []
            """.trimIndent()
        )
        Files.writeString(directory.resolve("main.tex"), document)
        return directory
    }

    @Test
    fun `a logname in the YAML configuration renames the log`() {
        withRestoredState {
            val directory = echoFixture()
            val log = directory.resolve("mylog.log")
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n" +
                    "logname: ${log.toString().replace("\\", "/")}\n"
            )
            val (status, _) = runCLI(
                "--working-directory", directory.toString(),
                "--log", "main.tex"
            )
            assertEquals(0, status, "the run with the renamed log succeeds")
            assertTrue(
                log.toFile().exists(),
                "the log carries the configured name"
            )
            assertFalse(
                directory.resolve("arara.log").toFile().exists(),
                "the default name stays unused"
            )
        }
    }

    @Test
    fun `a Lua configuration's log file key renames the log`() {
        withRestoredState {
            val directory = echoFixture()
            val log = directory.resolve("lua-mylog.log")
            Files.writeString(
                directory.resolve("arararc.lua"),
                """
                return {
                  paths = { "./" },
                  log = {
                    file = "${log.toString().replace("\\", "/")}"
                  }
                }
                """.trimIndent()
            )
            val (status, _) = runCLI(
                "--working-directory", directory.toString(),
                "--log", "main.tex"
            )
            assertEquals(0, status, "the run under the Lua configuration succeeds")
            assertTrue(log.toFile().exists(), "the Lua log key renames the log")
        }
    }

    @Test
    fun `the database anchors at the project's working directory`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-dbanchor")
            Files.createDirectories(directory.resolve("book/rules"))
            Files.writeString(
                directory.resolve("book/.araraconfig.lua"),
                "return { paths = { \"./rules\" } }\n"
            )
            Files.writeString(
                directory.resolve("book/rules/echoone.yaml"),
                """
                !config
                identifier: echoone
                name: The echoer
                commands:
                  - name: The echoer
                    command: >
                      @{ return isWindows(getCommand('cmd', '/c', 'echo', 'QuackOne'), getCommand('echo', 'QuackOne')); }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("book/book.tex"),
                "% arara: echoone if changed('tex')\n"
            )
            Files.writeString(
                directory.resolve("spec.proj.lua"),
                "return { name = \"book\", working_directory = \"book\", " +
                    "files = { [\"book.tex\"] = { } } }\n"
            )
            val (status, _) = runCLI(
                "--working-directory", directory.toString(),
                "spec.proj.lua"
            )
            assertEquals(0, status, "the subdirectory project runs")
            assertTrue(
                directory.resolve("book/arara.db").toFile().exists(),
                "the database anchors at the project's working directory"
            )
            assertFalse(
                directory.resolve("arara.db").toFile().exists(),
                "the invocation directory holds no database of the project"
            )
        }
    }

    @Test
    fun `a Lua configuration provides the file type end to end`() {
        withRestoredState {
            val directory = echoFixture("-- arara: echoone\n")
            Files.writeString(directory.resolve("main.my"), "-- arara: echoone\n")
            Files.writeString(
                directory.resolve("arararc.lua"),
                """
                return {
                  paths = { "./" },
                  file_types = {
                    { extension = "my", pattern = "^\\s*--\\s*" }
                  }
                }
                """.trimIndent()
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "main.my"
            )
            assertEquals(0, status, "the Lua-configured type carries the file")
            assertTrue("SUCCESS" in output, "the directive of the foreign type ran")
        }
    }

    @Test
    fun `a redeclared extension changes the directive lines end to end`() {
        withRestoredState {
            val slashes = echoFixture("// arara: echoone\n")
            Files.writeString(
                slashes.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n" +
                    "filetypes:\n  - extension: tex\n    pattern: '^\\s*//\\s*'\n"
            )
            val (status, _) = runCLI(
                "--working-directory", slashes.toString(),
                "main.tex"
            )
            assertEquals(0, status, "the redeclared pattern carries the line")

            val plain = echoFixture("% arara: echoone\n")
            Files.writeString(
                plain.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n" +
                    "filetypes:\n  - extension: tex\n    pattern: '^\\s*//\\s*'\n"
            )
            val (rejected, _) = runCLI(
                "--working-directory", plain.toString(),
                "main.tex"
            )
            assertNotEquals(
                0, rejected,
                "the historic comment line is no longer a directive"
            )
        }
    }

    @Test
    fun `a pattern that matches nothing leaves the file directive-less`() {
        withRestoredState {
            val directory = echoFixture()
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n" +
                    "filetypes:\n  - extension: tex\n    pattern: '^ZZZ'\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "main.tex"
            )
            assertNotEquals(0, status, "a pattern that matches nothing finds nothing")
            assertTrue(
                "directives" in output,
                "the rejection is the no-directives verdict"
            )
        }
    }

    @Test
    fun `the default preamble applies without a request`() {
        withRestoredState {
            val directory = echoFixture("just text, no directives\n")
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\npaths:\n  - './'\n" +
                    "preambles:\n  test: '% arara: echoone'\n" +
                    "defaultPreamble: test\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "main.tex"
            )
            assertEquals(0, status, "the default preamble runs the file")
            assertTrue("SUCCESS" in output, "the preamble's directive ran")
        }
    }

    @Test
    fun `the without-directives policy spares files with their own directives`() {
        withRestoredState {
            val configuration = "!config\nverbose: true\npaths:\n  - './'\n" +
                "preambles:\n  test: '% arara: echoone'\n" +
                "defaultPreamble: test\n" +
                "prependPreambleIfDirectivesGiven: false\n"
            val withOwn = echoFixture()
            Files.writeString(withOwn.resolve("arararc.yaml"), configuration)
            val (ownStatus, ownOutput) = runCLI(
                "--working-directory", withOwn.toString(),
                "main.tex"
            )
            assertEquals(0, ownStatus, "the file with its own directive runs")
            assertEquals(
                1, ownOutput.split("QuackOne").size - 1,
                "the policy spares the file its own directives"
            )
            val withoutOwn = echoFixture("just text\n")
            Files.writeString(withoutOwn.resolve("arararc.yaml"), configuration)
            val (bareStatus, bareOutput) = runCLI(
                "--working-directory", withoutOwn.toString(),
                "main.tex"
            )
            assertEquals(0, bareStatus, "the directive-less file runs")
            assertEquals(
                1, bareOutput.split("QuackOne").size - 1,
                "the policy still serves the file with none of its own"
            )
        }
    }

    @Test
    fun `preamble directives run before the file's own`() {
        withRestoredState {
            val directory = echoFixture()
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\nverbose: true\npaths:\n  - './'\n" +
                    "preambles:\n  test: '% arara: preambleer'\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "--preamble", "test",
                "main.tex"
            )
            assertEquals(0, status, "the preamble and the file's directive both run")
            assertTrue(
                output.indexOf("PreambleFirst") < output.indexOf("QuackOne"),
                "the preamble's directive runs before the file's own"
            )
        }
    }

    @Test
    fun `the command line preamble wins over a Lua configuration's default`() {
        withRestoredState {
            val directory = echoFixture()
            Files.writeString(
                directory.resolve("arararc.lua"),
                """
                return {
                  paths = { "./" },
                  verbose = true,
                  preamble = {
                    default = "alpha",
                    definitions = {
                      alpha = { "% arara: preambleer" },
                      beta = { "% arara: echoone" }
                    }
                  }
                }
                """.trimIndent()
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "--preamble", "beta",
                "main.tex"
            )
            assertEquals(0, status, "the requested preamble runs")
            assertTrue("QuackOne" in output, "the requested preamble's directive ran")
            assertFalse(
                "PreambleFirst" in output,
                "the configuration's default stays unapplied"
            )
        }
    }

    @Test
    fun `an explicit preamble applies even under the without-directives policy`() {
        withRestoredState {
            val directory = echoFixture()
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\nverbose: true\npaths:\n  - './'\n" +
                    "preambles:\n  test: '% arara: echoone'\n" +
                    "prependPreambleIfDirectivesGiven: false\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "--preamble", "test",
                "main.tex"
            )
            assertEquals(0, status, "the explicit preamble and the file's directive run")
            assertEquals(
                2, output.split("QuackOne").size - 1,
                "the explicit request beats the policy"
            )
        }
    }

    @Test
    fun `rules read the seeded environment in the deprecated dialect`() {
        withRestoredState {
            val directory = echoFixture()
            Files.writeString(
                directory.resolve("envreader.yaml"),
                """
                !config
                identifier: envreader
                name: The environment reader
                commands:
                  - name: The environment reader
                    command: >
                      @{ return isWindows(getCommand('cmd', '/c', 'echo',
                      getSession().contains('environment:PATH') ||
                      getSession().contains('environment:Path')
                      ? 'EnvWorks' : 'NoEnv'),
                      getCommand('echo',
                      getSession().contains('environment:PATH') ||
                      getSession().contains('environment:Path')
                      ? 'EnvWorks' : 'NoEnv')); }
                arguments: []
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\nverbose: true\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("main.tex"),
                "% arara: envreader\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "main.tex"
            )
            assertEquals(0, status, "the environment-reading rule runs")
            assertTrue(
                "EnvWorks" in output,
                "the rule reads the seeded environment"
            )
        }
    }

    @Test
    fun `rules read the seeded environment in the Lua dialect`() {
        withRestoredState {
            val directory = echoFixture()
            Files.writeString(
                directory.resolve("envreader.lua"),
                """
                return {
                  identifier = "envreader",
                  name = "The environment reader",
                  authors = { "Island of TeX" },
                  commands = {
                    {
                      name = "The environment reader",
                      command = function(ctx)
                        local session = getSession()
                        local has = session.contains('environment:PATH')
                          or session.contains('environment:Path')
                        return isWindows(
                          getCommand('cmd', '/c', 'echo', has and 'EnvWorks' or 'NoEnv'),
                          getCommand('echo', has and 'EnvWorks' or 'NoEnv')
                        )
                      end
                    }
                  },
                  arguments = {}
                }
                """.trimIndent()
            )
            Files.writeString(
                directory.resolve("arararc.yaml"),
                "!config\nverbose: true\npaths:\n  - './'\n"
            )
            Files.writeString(
                directory.resolve("main.tex"),
                "% arara: envreader\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "--engine", "lua",
                "main.tex"
            )
            assertEquals(0, status, "the environment-reading rule runs")
            assertTrue(
                "EnvWorks" in output,
                "the Lua rule reads the seeded environment"
            )
        }
    }

    @Test
    fun `rejects preambles the configuration does not define`() {
        withRestoredState {
            val directory = createTempDirectory("arara-cli-preamble")
            // the file has to exist and carry a directive, so the run
            // reaches the preamble resolution itself instead of dying
            // at the file lookup - the rejection under test is the
            // verdict, not the missing file
            Files.writeString(
                directory.resolve("preamble.tex"),
                "% arara: echoone\n"
            )
            val (status, output) = runCLI(
                "--working-directory", directory.toString(),
                "--preamble", "nonexistent",
                "preamble.tex"
            )
            assertNotEquals(0, status, "an undefined preamble is an error")
            assertTrue(
                "the preamble \"nonexistent\" could not be found" in output,
                "the rejection names the requested preamble"
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
    fun `a run under the full option surface succeeds`() {
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
    fun `the dry run mode marks its entries and the safe mode keeps ordinary commands running`() {
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
