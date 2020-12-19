// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.localization.Messages
import org.slf4j.LoggerFactory

/**
 * The language controller arara's core relies on.
 *
 * Please note that this relies on [org.islandoftex.arara.api.localization.AraraMessages].
 */
object LanguageController {
    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(LanguageController::class.java)

    /**
     * The messages object. This will be used to fetch messages and format them
     * (using [String.format]).
     */
    @JvmStatic
    var messages = Messages()

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
                logger.warn("Language ${locale.displayLanguage} not available; " +
                        "defaulting to English.")
                Messages()
            }
        }
    }
}
