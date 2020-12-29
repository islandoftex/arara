// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

class FileHandlingTest : ShouldSpec({
    context("path handling") {
        should("change extension of file with extension") {
            FileHandling.changeExtension(MPPPath("quack.tex"), "log")
                    .fileName shouldBe MPPPath("quack.log").fileName
        }
        should("change extension of file without extension") {
            FileHandling.changeExtension(MPPPath("quack"), "log")
                    .fileName shouldBe MPPPath("quack.log").fileName
        }
    }

    context("subdirectories") {
        should("get subdirecotry relationship right") {
            FileHandling.isSubDirectory(MPPPath("../docs"), MPPPath("..")) shouldBe true
            FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../docs")) shouldBe false
        }
        should("not treat files as directories") {
            FileHandling.isSubDirectory(MPPPath("../LICENSE"), MPPPath("..")) shouldBe false
            FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../LICENSE")) shouldBe false
        }
    }

    context("hashing and database") {
        should("fail generating CRC sums on inexistent files") {
            shouldThrow<AraraException> {
                FileHandling.calculateHash(MPPPath("QUACK"))
            }
        }
        should("generate correct CRC sums") {
            FileHandling.calculateHash(MPPPath("../LICENSE")) shouldBe 608305299
            FileHandling.calculateHash(MPPPath("../CODE_OF_CONDUCT.md")) shouldBe 3856623865
        }

        should("detect changes on file") {
            withContext(Dispatchers.IO) {
                val file = MPPPath(tempfile().toPath())
                val databaseFile = MPPPath(tempfile().toPath())
                databaseFile.toJVMPath().deleteIfExists()
                FileHandling.hasChanged(file, databaseFile) shouldBe true
                FileHandling.hasChanged(file, databaseFile) shouldBe false
                file.writeText("QUACK")
                FileHandling.hasChanged(file, databaseFile) shouldBe true
                FileHandling.hasChanged(file, databaseFile) shouldBe false
                file.writeText("QUACK2")
                FileHandling.hasChanged(file, databaseFile) shouldBe true
                file.toJVMPath().deleteExisting()
                FileHandling.hasChanged(file, databaseFile) shouldBe true
                FileHandling.hasChanged(file, databaseFile) shouldBe false
            }
        }
    }
})
