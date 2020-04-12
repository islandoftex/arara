// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import java.util.Locale
import org.islandoftex.arara.api.localization.Messages

/**
 * The language controller arara's core relies on.
 *
 * Please note that this relies on [org.islandoftex.arara.api.localization.AraraMessages].
 */
object LanguageController {
    /**
     * The messages object. This will be used to fetch messages and format them
     * (using [String.format]).
     */
    var messages = Messages()
        private set

    fun setLocale(locale: Locale) {
        messages = when (locale) {
            Locale.GERMAN -> GermanLanguage()
            Locale.ITALIAN -> ItalianLanguage()
            Locale.forLanguageTag("nl") -> DutchLanguage()
            Locale.forLanguageTag("pt-BR") -> BrazilianLanguage()
            Locale.forLanguageTag("en-QN") -> NorfolkLanguage()
            else -> Messages()
        }
    }
}
