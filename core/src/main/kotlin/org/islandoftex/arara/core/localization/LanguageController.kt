// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmStatic
import org.islandoftex.arara.api.localization.MPPLocale

/**
 * The language controller arara's core relies on. It holds the message
 * catalog of the current run and swaps it when a locale is requested.
 */
object LanguageController {
    private val logger = KotlinLogging.logger {}

    /**
     * The message catalog of the current run. Messages are typed
     * members of the catalog, so fetching one is a call that the
     * compiler checks.
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
