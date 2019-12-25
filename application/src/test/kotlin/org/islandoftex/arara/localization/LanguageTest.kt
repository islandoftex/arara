package org.islandoftex.arara.localization

import org.islandoftex.arara.model.AraraException
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec
import java.util.*

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
