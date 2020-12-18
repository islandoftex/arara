// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import java.util.Locale
import org.islandoftex.arara.api.localization.CoreLocale

@DoNotParallelize
class LanguageControllerTest : ShouldSpec({
    should("instantiate all known locales") {
        LanguageController.messages.providedLocale shouldBe CoreLocale.ENGLISH

        listOf(
                Locale.GERMAN to CoreLocale.GERMAN,
                Locale.ITALIAN to CoreLocale.ITALIAN,
                Locale.forLanguageTag("nl") to CoreLocale.DUTCH,
                Locale.forLanguageTag("en-QN") to CoreLocale.NORFOLK,
                Locale("en", "QN") to CoreLocale.NORFOLK,
                Locale.forLanguageTag("pt-BR") to CoreLocale.BRAZILIAN,
                Locale("pt", "BR") to CoreLocale.BRAZILIAN
        ).forAll {
            LanguageController.loadMessagesFor(it.first)
            LanguageController.messages.providedLocale shouldBe it.second
        }

        LanguageController.loadMessagesFor(Locale.ENGLISH)
    }
    should("fall back to english on unknown locales") {
        LanguageController.messages.providedLocale shouldBe CoreLocale.ENGLISH
        LanguageController.loadMessagesFor(Locale("quack"))
        LanguageController.messages.providedLocale shouldBe CoreLocale.ENGLISH
    }
})
