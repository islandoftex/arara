// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.io.File

class MethodUtilsTest : ShouldSpec({
    should("find correct extension") {
        MethodUtils.getFileExtension(File("QUACK")) shouldBe ""
        MethodUtils.getFileExtension(File("a.tex")) shouldBe "tex"
        MethodUtils.getFileExtension(File(".tex")) shouldBe "tex"
    }
    should("find correct basename") {
        MethodUtils.getBasename(File("QUACK")) shouldBe "QUACK"
        MethodUtils.getBasename(File("a.tex")) shouldBe "a"
        MethodUtils.getBasename(File(".tex")) shouldBe ""
    }
})
