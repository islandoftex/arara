// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.localization.MPPLocale

class LanguageControllerTest {
    @Test
    fun shouldInstantiateAllKnownLocales() {
        assertEquals(LanguageController.messages.providedLocale, MPPLocale("en"))

        listOf(
                MPPLocale("de"),
                MPPLocale("it"),
                MPPLocale("nl"),
                MPPLocale("en-QN"),
                MPPLocale("pt-BR")
        ).forEach {
            LanguageController.loadMessagesFor(it)
            assertEquals(LanguageController.messages.providedLocale, it)
        }

        LanguageController.loadMessagesFor(MPPLocale("en"))
    }

    @Test
    fun shouldFallBackToEnglishOnUnknownLocales() {
        assertEquals(LanguageController.messages.providedLocale, MPPLocale("en"))
        LanguageController.loadMessagesFor(MPPLocale("quack"))
        assertEquals(LanguageController.messages.providedLocale, MPPLocale("en"))
    }
}
