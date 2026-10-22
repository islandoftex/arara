// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.localization.MPPLocale
import kotlin.jvm.JvmStatic

/**
 * The language controller arara's core relies on.
 */
object LanguageController {
    private val logger = KotlinLogging.logger {}

    /**
     * The selected message catalog.
     */
    @JvmStatic
    var messages: AraraMessages = EnglishMessages

    /**
     * Load messages for the specified [locale]. Alters the global state of
     * arara.
     */
    @JvmStatic
    fun loadMessagesFor(locale: MPPLocale) {
        messages = when (locale) {
            MPPLocale("de") -> GermanMessages
            MPPLocale("it") -> ItalianMessages
            MPPLocale("nl") -> DutchMessages
            MPPLocale("pt-BR") -> BrazilianPortugueseMessages
            MPPLocale("en-QN") -> NorfolkMessages
            MPPLocale("en") -> EnglishMessages
            else -> {
                logger.warn {
                    "Language ${locale.displayLanguage} not available; " +
                        "defaulting to English."
                }
                EnglishMessages
            }
        }
    }
}
