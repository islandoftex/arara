// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.islandoftex.arara.api.AraraIOException
import java.io.File
import java.nio.file.Files

class JVMMPPPathTest : ShouldSpec({
    context("read and write operations") {
        should("read from file") {
            val tmp = tempfile()
            tmp.writeText("quack")
            MPPPath(tmp.toString()).readText() shouldBe "quack"
            MPPPath(tmp.toString()).readLines() shouldBe listOf("quack")
        }
        should("write to file") {
            val tmp = tempfile()
            MPPPath(tmp.toString()).writeText("quack")
            tmp.readText() shouldBe "quack"
        }
        should("fail reading from directory") {
            val tmp = Files.createTempDirectory("read")
            shouldThrow<AraraIOException> {
                tmp.toMPPPath().readText()
            }.message shouldContain "read text from file"
        }
        should("fail writing to directory") {
            val tmp = Files.createTempDirectory("write")
            shouldThrow<AraraIOException> {
                tmp.toMPPPath().writeText("quack")
            }.message shouldContain "write text to file"
        }
    }
    context("value semantics") {
        should("treat different spellings of the same location as equal") {
            val relative = MPPPath("equality-probe.txt")
            val absolute = MPPPath(File("equality-probe.txt").absolutePath)

            relative shouldBe absolute
            relative.hashCode() shouldBe absolute.hashCode()
        }
        should("deduplicate equal paths in sets") {
            val relative = MPPPath("equality-probe.txt")
            val absolute = MPPPath(File("equality-probe.txt").absolutePath)

            setOf(relative, absolute).size shouldBe 1
        }
        should("preserve equality across normalization") {
            MPPPath("equality-probe.txt") shouldBe
                MPPPath("equality-probe.txt").normalize()
        }
        should("answer isAbsolute from the provided, not the reference path") {
            val path = MPPPath("equality-probe.txt")

            // the duality of MPPPath: isAbsolute reflects what the caller
            // wrote, while toString operates on the absolutized reference
            path.isAbsolute shouldBe false
            path.toString() shouldBe File(
                "equality-probe.txt"
            ).toPath().toAbsolutePath().normalize().toString()
        }
    }
})
