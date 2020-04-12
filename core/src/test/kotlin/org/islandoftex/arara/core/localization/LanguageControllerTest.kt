// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import java.util.Locale

@DoNotParallelize
class LanguageControllerTest : ShouldSpec({
    should("instantiate all known locales") {
        LanguageController.messages.providedLocale shouldBe Locale.ENGLISH

        listOf(
                Locale.GERMAN, Locale.ITALIAN,
                Locale.forLanguageTag("nl"),
                Locale.forLanguageTag("en-QN"),
                Locale("en", "QN"),
                Locale.forLanguageTag("pt-BR"),
                Locale("pt", "BR")
        ).forAll {
            LanguageController.setLocale(it)
            LanguageController.messages.providedLocale shouldBe it
        }

        LanguageController.setLocale(Locale.ENGLISH)
    }
    should("fall back to english on unknown locales") {
        LanguageController.messages.providedLocale shouldBe Locale.ENGLISH
        LanguageController.setLocale(Locale("quack"))
        LanguageController.messages.providedLocale shouldBe Locale.ENGLISH
    }
})
