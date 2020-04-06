// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.configuration.AraraSpec

class FileHandlingUtilsTest : ShouldSpec({
    should("fail generating CRC sums on inexistent files") {
        shouldThrow<AraraException> {
            FileHandlingUtils.calculateHash(File("QUACK"))
        }
    }
    should("generate correct CRC sums") {
        FileHandlingUtils.calculateHash(File("../LICENSE")) shouldBe "2396b4e2"
        FileHandlingUtils.calculateHash(File("../CODE_OF_CONDUCT.md")) shouldBe "1225d314"
    }

    should("find correct extension") {
        FileHandlingUtils.getFileExtension(File("QUACK")) shouldBe ""
        FileHandlingUtils.getFileExtension(File("a.tex")) shouldBe "tex"
        FileHandlingUtils.getFileExtension(File(".tex")) shouldBe "tex"
    }
    should("find correct basename") {
        FileHandlingUtils.getBasename(File("QUACK")) shouldBe "QUACK"
        FileHandlingUtils.getBasename(File("a.tex")) shouldBe "a"
        FileHandlingUtils.getBasename(File(".tex")) shouldBe ""
    }

    should("get subdirecotry relationship right") {
        FileHandlingUtils.isSubDirectory(File("../docs"), File("..")) shouldBe true
        FileHandlingUtils.isSubDirectory(File(".."), File("../docs")) shouldBe false
        shouldThrow<AraraException> {
            FileHandlingUtils.isSubDirectory(File("../LICENSE"), File(".."))
        }
        shouldThrow<AraraException> {
            FileHandlingUtils.isSubDirectory(File(".."), File("../LICENSE"))
        }
    }

    should("detect changes on file") {
        val file = Files.createTempFile(null, null).toFile()
        val referenceBackup = Arara.config[AraraSpec.Execution.reference]
        Arara.config[AraraSpec.Execution.reference] = file.parentFile.resolve("reference")
        FileHandlingUtils.hasChanged(file) shouldBe true
        FileHandlingUtils.hasChanged(file) shouldBe false
        file.writeText("QUACK")
        FileHandlingUtils.hasChanged(file) shouldBe true
        FileHandlingUtils.hasChanged(file) shouldBe false
        file.writeText("QUACK2")
        FileHandlingUtils.hasChanged(file) shouldBe true
        file.delete()
        FileHandlingUtils.hasChanged(file) shouldBe true
        FileHandlingUtils.hasChanged(file) shouldBe false
        Arara.config[AraraSpec.Execution.reference] = referenceBackup
    }
})
