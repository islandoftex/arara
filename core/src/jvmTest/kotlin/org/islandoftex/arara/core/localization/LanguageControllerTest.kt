// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.api.localization.CoreLocale
import org.islandoftex.arara.api.localization.MPPLocale

@DoNotParallelize
class LanguageControllerTest : ShouldSpec({
    should("instantiate all known locales") {
        LanguageController.messages.providedLocale shouldBe CoreLocale.ENGLISH

        listOf(
                MPPLocale("de") to CoreLocale.GERMAN,
                MPPLocale("it") to CoreLocale.ITALIAN,
                MPPLocale("nl") to CoreLocale.DUTCH,
                MPPLocale("en-QN") to CoreLocale.NORFOLK,
                MPPLocale("pt-BR") to CoreLocale.BRAZILIAN
        ).forAll {
            LanguageController.loadMessagesFor(it.first)
            LanguageController.messages.providedLocale shouldBe it.second
        }

        LanguageController.loadMessagesFor(MPPLocale("en"))
    }
    should("fall back to english on unknown locales") {
        LanguageController.messages.providedLocale shouldBe CoreLocale.ENGLISH
        LanguageController.loadMessagesFor(MPPLocale("quack"))
        LanguageController.messages.providedLocale shouldBe CoreLocale.ENGLISH
    }
})
