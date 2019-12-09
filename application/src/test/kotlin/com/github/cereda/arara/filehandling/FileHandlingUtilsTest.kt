package com.github.cereda.arara.filehandling

import io.kotlintest.shouldBe
import io.kotlintest.specs.ShouldSpec
import java.io.File

class FileHandlingUtilsTest : ShouldSpec({
    should("generate correct CRC sums") {
        FileHandlingUtils.calculateHash(File("../LICENSE")) shouldBe "17f430a5"
        FileHandlingUtils.calculateHash(File("../CODE_OF_CONDUCT.md")) shouldBe "536c426f"
    }
})
