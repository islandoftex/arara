// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.localization.Messages
import kotlin.jvm.JvmStatic

/**
 * The language controller arara's core relies on.
 *
 * Please note that this relies on [org.islandoftex.arara.api.localization.AraraMessages].
 */
object LanguageController {
    private val logger = KotlinLogging.logger {}

    /**
     * The messages object. This will be used to fetch messages and format them
     * (using string formatting).
     */
    @JvmStatic
    var messages = Messages()

    /**
     * Load messages for the specified [locale]. Alters the global state of
     * arara.
     */
    @JvmStatic
    fun loadMessagesFor(locale: MPPLocale) {
        messages = when (locale) {
            MPPLocale("de") -> GermanLanguage()
            MPPLocale("it") -> ItalianLanguage()
            MPPLocale("nl") -> DutchLanguage()
            MPPLocale("pt-BR") -> BrazilianLanguage()
            MPPLocale("en-QN") -> NorfolkLanguage()
            MPPLocale("en") -> Messages()
            else -> {
                logger.warn {
                    "Language ${locale.displayLanguage} not available; " +
                        "defaulting to English."
                }
                Messages()
            }
        }
    }
}
