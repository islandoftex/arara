// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeoutException
import javax.tools.ToolProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.junit.jupiter.api.io.TempDir

class SystemCommandResultTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    private val directory get() = temporaryDirectory.toMPPPath()

    @BeforeTest
    fun prepareFixture() {
        val source = directory.resolve("ProcessFixture.java")
        source.writeText("""
            public class ProcessFixture {
                public static void main(String[] args) throws Exception {
                    System.out.println("stdout marker");
                    System.err.println("stderr marker");
                    System.out.flush();
                    System.err.flush();
                    if (args[0].equals("sleep")) Thread.sleep(30000);
                    System.exit(7);
                }
            }
        """.trimIndent())
        check(ToolProvider.getSystemJavaCompiler().run(null, null, null, "--release", "11", source.toString()) == 0)
    }

    private val executable = MPPPath(System.getProperty("java.home")).resolve("bin/java").toString()
    private fun command(mode: String) = Command(
        listOf(executable, "-cp", directory.toString(), "ProcessFixture", mode),
        directory
    )

    @Test
    fun `keep nonzero exits distinct from execution failures and capture both streams`() {
        val result = Environment.executeSystemCommandResult(command("exit")) as SystemCommandResult.Completed
        assertEquals(7, result.exitCode)
        assertContains(result.output, "stdout marker")
        assertContains(result.output, "stderr marker")
    }

    @Test
    fun `preserve partial output and the timeout exception`() {
        val result = Environment.executeSystemCommandResult(
            command("sleep"), timeout = 3.seconds
        ) as SystemCommandResult.Failed
        assertTrue(result.exception is TimeoutException)
        assertContains(result.output, "stdout marker")
        assertContains(result.output, "stderr marker")
    }

    @Test
    fun `preserve partial output through the pair-returning compatibility API`() {
        val (status, output) = Environment.executeSystemCommand(command("sleep"), timeout = 3.seconds)
        assertEquals(Environment.ERROR_EXIT_STATUS, status)
        assertContains(output, "stdout marker")
        assertContains(output, "stderr marker")
    }

    @Test
    fun `retain the original exception on a failed launch`() {
        val result = Environment.executeSystemCommandResult(
            Command(listOf(directory.resolve("missing-executable").toString()))
        ) as SystemCommandResult.Failed
        assertTrue(result.exception is IOException)
        assertEquals("", result.output)
    }

    @Test
    fun `keep the pair-returning rule helper compatible`() {
        val (exit, output) = Environment.executeSystemCommand(command("exit"))
        assertEquals(7, exit)
        assertContains(output, "stdout marker")
        val missing = Command(listOf(directory.resolve("missing-executable").toString()))
        val expected = Environment.executeSystemCommandResult(missing) as SystemCommandResult.Failed
        val (failure, message) = Environment.executeSystemCommand(missing)
        assertEquals(Environment.ERROR_EXIT_STATUS, failure)
        assertEquals(expected.exception.toString(), message)
    }
}
