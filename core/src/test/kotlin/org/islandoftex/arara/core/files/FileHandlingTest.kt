// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class FileHandlingTest : ShouldSpec({
    "path handling" {
        should("normalize dots") {
            FileHandling.normalize(Paths.get("/tmp/./quack/..")).toString() shouldBe
                    FileHandling.normalize(Paths.get("/tmp/")).toString()
        }
        should("not exceed root") {
            FileHandling.normalize(Paths.get("/tmp/../../..")) shouldBe Paths.get("/")
        }

        should("change extension of file with extension") {
            FileHandling.changeExtension(Paths.get("quack.tex"), "log")
                    .fileName shouldBe Paths.get("quack.log")
        }
        should("change extension of file without extension") {
            FileHandling.changeExtension(Paths.get("quack"), "log")
                    .fileName shouldBe Paths.get("quack.log")
        }
    }

    "subdirectories" {
        should("get subdirecotry relationship right") {
            FileHandling.isSubDirectory(Paths.get("../docs"), Paths.get("..")) shouldBe true
            FileHandling.isSubDirectory(Paths.get(".."), Paths.get("../docs")) shouldBe false
        }
        should("not treat files as directories") {
            FileHandling.isSubDirectory(Paths.get("../LICENSE"), Paths.get("..")) shouldBe false
            FileHandling.isSubDirectory(Paths.get(".."), Paths.get("../LICENSE")) shouldBe false
        }
    }
})
