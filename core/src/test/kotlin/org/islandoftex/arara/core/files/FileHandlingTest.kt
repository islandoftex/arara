// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import java.nio.file.Paths
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.islandoftex.arara.api.AraraException

class FileHandlingTest : ShouldSpec({
    context("path handling") {
        should("normalize dots") {
            FileHandling.normalize(Paths.get("/tmp/./quack/..")).toString() shouldBe
                    FileHandling.normalize(Paths.get("/tmp/")).toString()
        }
        should("not exceed root") {
            FileHandling.normalize(Paths.get("/tmp/../../..")).toString() shouldBe
                    Paths.get("/..").toAbsolutePath().normalize().toString()
        }

        should("change extension of file with extension") {
            FileHandling.changeExtension(Paths.get("quack.tex"), "log")
                    .fileName.toAbsolutePath().toString() shouldBe
                    Paths.get("quack.log").toAbsolutePath().toString()
        }
        should("change extension of file without extension") {
            FileHandling.changeExtension(Paths.get("quack"), "log")
                    .fileName.toAbsolutePath().toString() shouldBe
                    Paths.get("quack.log").toAbsolutePath().toString()
        }
    }

    context("subdirectories") {
        should("get subdirecotry relationship right") {
            FileHandling.isSubDirectory(Paths.get("../docs"), Paths.get("..")) shouldBe true
            FileHandling.isSubDirectory(Paths.get(".."), Paths.get("../docs")) shouldBe false
        }
        should("not treat files as directories") {
            FileHandling.isSubDirectory(Paths.get("../LICENSE"), Paths.get("..")) shouldBe false
            FileHandling.isSubDirectory(Paths.get(".."), Paths.get("../LICENSE")) shouldBe false
        }
    }

    context("hashing and database") {
        should("fail generating CRC sums on inexistent files") {
            shouldThrow<AraraException> {
                FileHandling.calculateHash(Paths.get("QUACK"))
            }
        }
        should("generate correct CRC sums") {
            FileHandling.calculateHash(Paths.get("../LICENSE")) shouldBe 608305299
            FileHandling.calculateHash(Paths.get("../CODE_OF_CONDUCT.md")) shouldBe 3856623865
        }

        should("detect changes on file") {
            withContext(Dispatchers.IO) {
                val file = tempfile().toPath()
                val databaseFile = tempfile().toPath()
                databaseFile.deleteIfExists()
                FileHandling.hasChanged(file, databaseFile) shouldBe true
                FileHandling.hasChanged(file, databaseFile) shouldBe false
                file.writeText("QUACK")
                FileHandling.hasChanged(file, databaseFile) shouldBe true
                FileHandling.hasChanged(file, databaseFile) shouldBe false
                file.writeText("QUACK2")
                FileHandling.hasChanged(file, databaseFile) shouldBe true
                file.deleteExisting()
                FileHandling.hasChanged(file, databaseFile) shouldBe true
                FileHandling.hasChanged(file, databaseFile) shouldBe false
            }
        }
    }
})
