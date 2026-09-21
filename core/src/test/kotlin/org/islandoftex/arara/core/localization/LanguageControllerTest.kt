// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageControllerTest {
    @Test
    fun shouldInstantiateAllKnownLocales() {
        assertEquals(MPPLocale("en"), LanguageController.messages.providedLocale, "the default messages are English")

        listOf(
            MPPLocale("de"),
            MPPLocale("it"),
            MPPLocale("nl"),
            MPPLocale("en-QN"),
            MPPLocale("pt-BR")
        ).forEach {
            LanguageController.loadMessagesFor(it)
            assertEquals(it, LanguageController.messages.providedLocale, "the requested locale is installed")
        }

        LanguageController.loadMessagesFor(MPPLocale("en"))
    }

    @Test
    fun shouldFallBackToEnglishOnUnknownLocales() {
        assertEquals(MPPLocale("en"), LanguageController.messages.providedLocale, "the default messages are English")
        LanguageController.loadMessagesFor(MPPLocale("quack"))
        assertEquals(MPPLocale("en"), LanguageController.messages.providedLocale, "the default messages are English")
    }
}
