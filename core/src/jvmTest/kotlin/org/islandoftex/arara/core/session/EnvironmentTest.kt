// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.util.concurrent.TimeoutException
import kotlin.time.milliseconds

class EnvironmentTest : ShouldSpec({
    should("return null on non-existent system variable") {
        Environment.getSystemPropertyOrNull("asdf") shouldBe null
    }
    should("hold a fallback value on non-existent system variable") {
        Environment.getSystemProperty("asdf", "fallback") shouldBe "fallback"
    }

    should("have a non-null value for existent variable") {
        listOf("/", "\\") shouldContain Environment.getSystemPropertyOrNull("file.separator")
    }
    should("have a non-fallback value for existent variable") {
        listOf("/", "\\") shouldContain Environment.getSystemProperty("file.separator", "fallback")
    }

    should("find system utils") {
        Environment.isOnPath("echo") shouldBe true
    }
    should("not find utils with fantasy name") {
        // hopefully no one will have such a command in the path…
        Environment.isOnPath("echoQuackForArara") shouldBe false
    }

    if (!Environment.checkOS("Windows")) {
        // if we are not on Windows execute tests with execution
        should("run successfully") {
            val (exit, output) = Environment.executeSystemCommand(Command(listOf("true")))
            exit shouldBe 0
            output shouldBe ""
        }
        should("error with exit code 1") {
            val (exit, output) = Environment.executeSystemCommand(Command(listOf("false")))
            exit shouldBe 1
            output shouldBe ""
        }
        should("error with timeout exception") {
            val (exit, output) = Environment.executeSystemCommand(
                    Command(listOf("sleep", "1s")), true, 500.milliseconds)
            exit shouldBe Environment.errorExitStatus
            output shouldContain TimeoutException::class.java.name
        }
    }
})
