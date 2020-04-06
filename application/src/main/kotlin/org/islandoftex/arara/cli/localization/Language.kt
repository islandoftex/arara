// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.localization

import java.util.Locale
import org.islandoftex.arara.api.AraraException

/**
 * Implements the language model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class Language(code: String) {
    // the language code, based on
    // ISO 639-1 and language variants
    private val code: String

    /**
     * Gets the language name.
     *
     * @return A string representing the language name.
     */
    val name: String
        get() = languages.getValue(code).first

    /**
     * Gets the language locale.
     *
     * @return The language locale.
     */
    val locale: Locale
        get() = languages.getValue(code).second

    // throws an exception on invalid language
    init {
        if (languages.containsKey(code)) {
            this.code = code
        } else {
            throw AraraException(
                LanguageController.getMessage(
                    Messages.ERROR_LANGUAGE_INVALID_CODE,
                    languagesList
                )
            )
        }
    }

    companion object {
        // map containing all languages
        // supported by arara
        private val languages = mapOf(
                "en" to Pair("English", Locale("en")),
                "de" to Pair("German", Locale("de")),
                "nl" to Pair("Dutch", Locale("nl")),
                "qn" to Pair("Broad Norfolk", Locale("en", "QN")),
                "ptbr" to Pair("Brazilian Portuguese", Locale("pt", "BR")),
                "it" to Pair("Italian", Locale("it"))
        )

        /**
         * String representing the list of available languages
         * because they don't change initialized with the string
         */
        val languagesList: String = "(" + languages.map { (key, value) ->
            value.first + ": " + key
        }.joinToString(", ") + ")"
    }
}
