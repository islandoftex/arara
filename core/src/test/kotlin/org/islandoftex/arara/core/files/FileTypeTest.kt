// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.islandoftex.arara.api.AraraException

class FileTypeTest : ShouldSpec({
    should("only depend on extension for equality") {
        FileType("test", "^\\s*") shouldBe FileType("test", "^\\s*%\\s+")
        FileType("test", "^\\s*%\\s+") shouldBe FileType("test", "^\\s*%\\s+")
    }
    should("only depend on extension of inequalityy") {
        FileType("test", "^\\s*") shouldNotBe FileType("tes", "^\\s*%\\s+")
        FileType("test", "^\\s*%\\s+") shouldNotBe FileType("tes", "^\\s*%\\s+")
    }

    should("expect valid pattern") {
        shouldThrow<AraraException> {
            FileType("test", "[a")
        }
    }
})
