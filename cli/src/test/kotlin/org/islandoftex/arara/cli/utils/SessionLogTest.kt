// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configurator
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.session.Session

/**
 * Verifies the session's log trail end to end, through both rule
 * dialects: every mutation writes its line as it happens - the
 * fresh set, the overwrite naming the value it displaced, the
 * removal, the wipe with its count - and the environment
 * seeding that opens the execution records its refresh in one
 * line. Like the append pair, this class deliberately mounts a
 * real logging backend and restores the suite's backendless
 * design afterwards.
 */
class SessionLogTest {
    private val originalLoggingOptions = Session.loggingOptions
    private val logFile = Files.createTempFile("arara-session-", ".log")

    @AfterTest
    fun restoreTheBackendAndGlobals() {
        Session.loggingOptions = originalLoggingOptions
        ThreadContext.remove("araraLogFile")
        ThreadContext.remove("araraAppend")
        // the file appender holds the log open until it stops:
        // Windows refuses to delete a file a live handle claims,
        // so the appenders release it before the delete - Linux
        // tolerates the opposite order, which is why this
        // teardown was born green there. The context itself keeps
        // running: stopping it would strand every shared logger
        // created before the stop, and this class asserts lines
        // the shared singletons write
        (LogManager.getContext(false) as LoggerContext)
            .configuration.appenders.values.forEach { it.stop() }
        Files.deleteIfExists(logFile)
        Configurator.setRootLevel(Level.OFF)
    }

    /**
     * Runs the provided arguments against a fresh command line
     * instance, capturing the console and returning the exit
     * status the invocation produced.
     */
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

    /**
     * Runs the trail scenario under the given extra arguments and
     * the provided rule dialect, then returns the log the run
     * wrote. The session is wiped beforehand - the earlier runs
     * of the same virtual machine hold no claim on this run's
     * story - and the log path is pinned before the command line
     * assembles its options, which preserve it.
     *
     * @param extraArgs The arguments beyond the trail scenario.
     * @param ruleFile The rule file the scenario resolves.
     * @param ruleContent The rule's own content.
     * @return The log file's content.
     */
    private fun runTheTrail(
        extraArgs: List<String>,
        ruleFile: String,
        ruleContent: String
    ): String {
        val directory = Files.createTempDirectory("arara-session-trail")
        Files.writeString(
            directory.resolve("arararc.yaml"),
            "!config\npaths:\n  - './'\n"
        )
        Files.writeString(directory.resolve(ruleFile), ruleContent)
        Files.writeString(
            directory.resolve("trail.tex"),
            "% arara: sessionrule\n"
        )
        Session.clear()
        Session.loggingOptions = LoggingOptions(
            enableLogging = false,
            appendLog = false,
            logFile = MPPPath(logFile.toString())
        )
        val (status, _) = runCLI(
            "--log",
            *extraArgs.toTypedArray(),
            "--working-directory", directory.toString(),
            "trail.tex"
        )
        assertEquals(0, status, "the trail run succeeds")
        return Files.readString(logFile)
    }

    /**
     * Asserts the trail the scenario leaves in the log: the
     * seed's one-line refresh, the four mutation lines in the
     * order they happened, and the wipe naming a count.
     *
     * @param content The log file's content.
     */
    private fun assertTheTrail(content: String) {
        assertTrue(
            "Session environment refreshed (" in content,
            "the seeding records its refresh in one line"
        )
        val set = "Session variable 'alpha' set to 'one'."
        val overwrite =
            "Session variable 'alpha' overwritten with " +
                "'two' (was 'one')."
        val beta = "Session variable 'beta' set to 'three'."
        val removed = "Session variable 'alpha' removed."
        assertTrue(set in content, "the fresh set writes its line")
        assertTrue(
            overwrite in content,
            "the overwrite names the value it displaced"
        )
        assertTrue(beta in content, "the second variable writes its line")
        assertTrue(removed in content, "the removal writes its line")
        assertTrue(
            Regex("Session cleared \\([0-9]+ variables?\\)\\.")
                .containsMatchIn(content),
            "the wipe names its count"
        )
        assertTrue(
            "Session cleared (1 variable)." in content,
            "a lone variable clears under the singular spelling"
        )
        assertTrue(
            content.indexOf(set) < content.indexOf(overwrite) &&
                content.indexOf(overwrite) < content.indexOf(removed),
            "the lines land in the order the mutations happened"
        )
    }

    @Test
    fun `the log trails the session's mutations`() {
        val content = runTheTrail(
            emptyList(),
            "sessionrule.yaml",
            """
            !config
            identifier: sessionrule
            name: The session trail
            commands:
              - name: The session trail
                command: >
                  @{
                      getSession().put('alpha', 'one');
                      getSession().put('alpha', 'two');
                      getSession().put('beta', 'three');
                      getSession().remove('alpha');
                      getSession().clear();
                      getSession().put('gamma', 'four');
                      getSession().clear();
                      return isWindows(getCommand('cmd', '/c', 'echo', 'Quack'), getCommand('echo', 'Quack'));
                  }
            arguments: []
            """.trimIndent()
        )
        assertTheTrail(content)
    }

    @Test
    fun `the log trails the session's mutations in the Lua dialect`() {
        val content = runTheTrail(
            listOf("--engine", "lua"),
            "sessionrule.lua",
            """
            return {
              identifier = "sessionrule",
              name = "The session trail",
              authors = { "Island of TeX" },
              commands = {
                {
                  name = "The session trail",
                  command = function(ctx)
                    local session = getSession()
                    session.put('alpha', 'one')
                    session.put('alpha', 'two')
                    session.put('beta', 'three')
                    session.remove('alpha')
                    session.clear()
                    session.put('gamma', 'four')
                    session.clear()
                    return isWindows(
                      getCommand('cmd', '/c', 'echo', 'Quack'),
                      getCommand('echo', 'Quack')
                    )
                  end
                }
              },
              arguments = {}
            }
            """.trimIndent()
        )
        assertTheTrail(content)
    }
}
