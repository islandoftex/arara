// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.localization

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.util.Locale
import org.islandoftex.arara.api.AraraException

class LanguageTest : ShouldSpec({
    should("instantiate with known code") {
        Language("en").locale shouldBe Locale.ENGLISH
    }

    should("throw on unknown language") {
        shouldThrow<AraraException> {
            Language("quack")
        }
    }
})
