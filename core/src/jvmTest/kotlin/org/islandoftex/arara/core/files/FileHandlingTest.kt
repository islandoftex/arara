// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

class FileHandlingTest : ShouldSpec({

    should("change extension of file with extension") {
        MPPPath("quack.log").fileName shouldBe
            FileHandling.changeExtension(MPPPath("quack.tex"), "log").fileName
    }

    should("change extension of file without extension") {
        MPPPath("quack.log").fileName shouldBe
            FileHandling.changeExtension(MPPPath("quack"), "log").fileName
    }

    should("get subdirectory relationship right") {
        FileHandling.isSubDirectory(MPPPath("../docs"), MPPPath("..")) shouldBe true
        FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../docs")) shouldBe false
    }

    should("not treat files as subdirectories") {
        FileHandling.isSubDirectory(MPPPath("../LICENSE"), MPPPath("..")) shouldBe false
        FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../LICENSE")) shouldBe false
    }

    should("fail generating CRC sum on inexistent files") {
        shouldThrow< AraraException> {
            FileHandling.calculateHash(MPPPath("QUACK"))
        }
    }

    should("generate correct CRC sum") {
        1616727774 shouldBe FileHandling.calculateHash(MPPPath("../LICENSE"))
    }

})
