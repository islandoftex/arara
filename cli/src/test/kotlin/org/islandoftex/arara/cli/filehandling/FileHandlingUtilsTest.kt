// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.io.File

class FileHandlingUtilsTest : ShouldSpec({
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
})
