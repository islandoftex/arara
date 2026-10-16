// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.localization.MPPLocale

/**
 * The language controller arara's core relies on. It holds the message
 * catalog of the current run and swaps it when a locale is requested.
 */
object LanguageController {
    /**
     * The logger of the language controller.
     */
    private val logger = KotlinLogging.logger {}

    /**
     * The message catalog of the current run. Messages are typed
     * members of the catalog, so fetching one is a call that the
     * compiler checks.
     */
    var messages: AraraMessages = EnglishMessages

    /**
     * The message catalogs the controller can install, each carrying
     * the locale it is written for: a request resolves against those
     * declarations, so a catalog joins the offered languages by
     * declaring itself.
     */
    private val catalogs = listOf(
        EnglishMessages,
        GermanMessages,
        ItalianMessages,
        DutchMessages,
        BrazilianPortugueseMessages,
        NorfolkMessages
    )

    /**
     * Load messages for the specified [locale]. Alters the global state of
     * arara.
     */
    fun loadMessagesFor(locale: MPPLocale) {
        messages = catalogs.firstOrNull { it.providedLocale == locale }
            ?: run {
                logger.warn {
                    "Language ${locale.displayLanguage} not available; " +
                        "defaulting to English."
                }
                EnglishMessages
            }
    }
}
