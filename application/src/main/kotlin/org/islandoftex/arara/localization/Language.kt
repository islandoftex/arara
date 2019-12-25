/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.islandoftex.arara.localization

import org.islandoftex.arara.model.AraraException
import java.util.*

/**
 * Implements the language model.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
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
        
        // string representing the list of available languages
        // because they don't change initialized with the string
        val languagesList: String = "(" + languages.map { (key, value) ->
            value.first + ": " + key
        }.joinToString(", ") + ")"
    }
}
