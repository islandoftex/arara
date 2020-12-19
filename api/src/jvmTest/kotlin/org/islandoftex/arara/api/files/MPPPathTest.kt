// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.islandoftex.arara.api.AraraIOException

class MPPPathTest : ShouldSpec({
    context("path normalization") {
        should("normalize dots") {
            MPPPath("/tmp/./quack/..").normalize().toString() shouldBe
                    MPPPath("/tmp/").normalize().toString()
        }
        should("not exceed root") {
            MPPPath("/tmp/../../..").normalize().toString() shouldBe "/"
        }
    }
    context("parenting") {
        should("use root as parent of root") {
            MPPPath("/").parent.toString() shouldBe
                    MPPPath("/").toString()
        }
        should("determine parent of correctly") {
            MPPPath("/tmp/./quack/..").parent.normalize().toString() shouldBe
                    MPPPath("/tmp/quack").toString()
            MPPPath("/tmp/./quack/..").normalize().parent.toString() shouldBe
                    MPPPath("/").toString()
        }
    }
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
            val tmp = tempfile().mkdir().toString()
            shouldThrow<AraraIOException> {
                MPPPath(tmp).readText()
            }.message shouldContain "read text from file"
        }
        should("fail writing to directory") {
            val tmp = tempfile().mkdir().toString()
            shouldThrow<AraraIOException> {
                MPPPath(tmp).writeText("quack")
            }.message shouldContain "write text to file"
        }
    }
})
