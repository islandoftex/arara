// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Verifies the language controller: all known locales load their
 * message catalogs, and unknown locales fall back to English.
 */
class LanguageControllerTest {
    @Test
    fun `instantiates all known locales`() {
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
    fun `falls back to English on unknown locales`() {
        assertEquals(MPPLocale("en"), LanguageController.messages.providedLocale, "the default messages are English")
        LanguageController.loadMessagesFor(MPPLocale("quack"))
        assertEquals(MPPLocale("en"), LanguageController.messages.providedLocale, "the default messages are English")
    }
}
