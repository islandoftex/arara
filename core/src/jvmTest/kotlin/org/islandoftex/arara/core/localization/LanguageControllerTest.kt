// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import io.kotest.core.spec.DoNotParallelize
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.api.localization.MPPLocale

@DoNotParallelize
class LanguageControllerTest : ShouldSpec({
    should("instantiate all known locales") {
        LanguageController.messages.providedLocale shouldBe MPPLocale("en")

        listOf(
                MPPLocale("de"),
                MPPLocale("it"),
                MPPLocale("nl"),
                MPPLocale("en-QN"),
                MPPLocale("pt-BR")
        ).forAll {
            LanguageController.loadMessagesFor(it)
            LanguageController.messages.providedLocale shouldBe it
        }

        LanguageController.loadMessagesFor(MPPLocale("en"))
    }
    should("fall back to english on unknown locales") {
        LanguageController.messages.providedLocale shouldBe MPPLocale("en")
        LanguageController.loadMessagesFor(MPPLocale("quack"))
        LanguageController.messages.providedLocale shouldBe MPPLocale("en")
    }
})
