// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Command
import org.islandoftex.arara.core.session.LinearExecutor
import java.nio.file.Files
import java.util.concurrent.TimeoutException
import javax.tools.ToolProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class CommandOutputLoggingTest {
    private fun withCommand(mode: String, check: (Command) -> Unit) {
        val previous = LinearExecutor.executionOptions
        val directory = Files.createTempDirectory("arara command log ").toMPPPath()
        try {
            val source = directory.resolve("OutputFixture.java")
            source.writeText("""
                public class OutputFixture {
                    public static void main(String[] args) throws Exception {
                        System.out.println("stdout regression marker");
                        System.err.println("stderr regression marker");
                        System.out.flush();
                        System.err.flush();
                        if (args[0].equals("sleep")) Thread.sleep(30000);
                        System.exit(Integer.parseInt(args[0]));
                    }
                }
            """.trimIndent())
            assertEquals(0, ToolProvider.getSystemJavaCompiler().run(
                null, null, null, "--release", "11", source.toString()
            ))
            val name = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
            val java = MPPPath(System.getProperty("java.home")).resolve("bin").resolve(name)
            val log = directory.resolve("command.log")
            LoggingUtils.init()
            LoggingUtils.setupLogging(LoggingOptions(true, false, log))
            LinearExecutor.executionOptions = ExecutionOptions(timeoutValue = 3.seconds)
            check(Command(listOf(java.toString(), "-cp", directory.toString(), "OutputFixture", mode)))
            LoggingUtils.init()
            val text = log.readText()
            assertEquals(1, Regex("stdout regression marker").findAll(text).count())
            assertEquals(1, Regex("stderr regression marker").findAll(text).count())
        } finally {
            LoggingUtils.init()
            LinearExecutor.executionOptions = previous
            directory.removeDirectory()
        }
    }

    @Test
    fun timeoutLogsBothStreamsAndRetainsItsCause() {
        withCommand("sleep") { command ->
            val failure = assertFailsWith<AraraException> { InterpreterUtils.run(command) }
            assertEquals(LanguageController.messages.ERROR_RUN_TIMEOUT_EXCEPTION, failure.message)
            assertTrue(failure.exception is TimeoutException)
        }
    }

    @Test
    fun completedCommandsLogBothStreamsRegardlessOfExitCode() {
        for (code in listOf(0, 7)) {
            withCommand(code.toString()) { command ->
                assertEquals(code, InterpreterUtils.run(command))
            }
        }
    }
}
