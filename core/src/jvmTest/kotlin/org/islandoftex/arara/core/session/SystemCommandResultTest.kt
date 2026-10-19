// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.islandoftex.arara.api.files.toMPPPath
import java.io.IOException
import java.nio.file.Files
import org.islandoftex.arara.api.files.MPPPath
import java.util.concurrent.TimeoutException
import javax.tools.ToolProvider
import kotlin.time.Duration.Companion.seconds

class SystemCommandResultTest : ShouldSpec({
    val directory = Files.createTempDirectory("arara process results ").toMPPPath()
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
    val executable = MPPPath(System.getProperty("java.home")).resolve("bin/java").toString()
    fun command(mode: String) = Command(
        listOf(executable, "-cp", directory.toString(), "ProcessFixture", mode),
        directory
    )
    afterSpec { directory.removeDirectory() }

    should("keep nonzero exits distinct from execution failures and capture both streams") {
        val result = Environment.executeSystemCommandResult(command("exit")) as SystemCommandResult.Completed
        result.exitCode shouldBe 7
        result.output shouldContain "stdout marker"
        result.output shouldContain "stderr marker"
    }

    should("preserve partial output and the timeout exception") {
        val result = Environment.executeSystemCommandResult(
            command("sleep"), timeout = 3.seconds
        ) as SystemCommandResult.Failed
        (result.exception is TimeoutException) shouldBe true
        result.output shouldContain "stdout marker"
        result.output shouldContain "stderr marker"
    }

    should("retain the original exception on a failed launch") {
        val result = Environment.executeSystemCommandResult(
            Command(listOf(directory.resolve("missing-executable").toString()))
        ) as SystemCommandResult.Failed
        (result.exception is IOException) shouldBe true
        result.output shouldBe ""
    }

    should("keep the pair-returning rule helper compatible") {
        val (exit, output) = Environment.executeSystemCommand(command("exit"))
        exit shouldBe 7
        output shouldContain "stdout marker"
        val missing = Command(listOf(directory.resolve("missing-executable").toString()))
        val expected = Environment.executeSystemCommandResult(missing) as SystemCommandResult.Failed
        val (failure, message) = Environment.executeSystemCommand(missing)
        failure shouldBe Environment.ERROR_EXIT_STATUS
        message shouldBe expected.exception.toString()
    }
})
