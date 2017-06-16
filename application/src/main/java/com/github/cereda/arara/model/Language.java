/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda 
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
package com.github.cereda.arara.model;

import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.utils.CommonUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Implements the language model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Language {

    // the language code, based on
    // ISO 639-1 and language variants
    private final String code;
    
    // map containing all languages
    // supported by nightingale
    private static final Map<String, Pair<String, Locale>> languages =
            new HashMap<String, Pair<String, Locale>>();

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Initialize the language model. All supported languages are added in here.
     */
    public static void init() {
        languages.put("en", new Pair<String, Locale>(
                "English",
                new Locale("en")
        ));
        languages.put("de", new Pair<String, Locale>(
                "German",
                new Locale("de")
        ));
        languages.put("nl", new Pair<String, Locale>(
                "Dutch",
                new Locale("nl")
        ));
        languages.put("qn", new Pair<String, Locale>(
                "Broad Norfolk",
                new Locale("en", "QN")
        ));
    }

    /**
     * Creates a new language object. It might raise an exception if the
     * provided language does not exist in the map.
     * @param code The language code, based on ISO 639-1 and language variants.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public Language(String code) throws AraraException {
        if (languages.containsKey(code)) {
            this.code = code;
        } else {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_LANGUAGE_INVALID_CODE,
                            getLanguagesList()
                    )
            );
        }
    }

    /**
     * Gets the language name.
     * @return A string representing the language name.
     */
    public String getName() {
        return languages.get(code).getFirstElement();
    }

    /**
     * Gets the language locale.
     * @return The language locale.
     */
    public Locale getLocale() {
        return languages.get(code).getSecondElement();
    }

    /**
     * Gets a string representing the list of available languages.
     * @return String representing the list of available languages.
     */
    public static String getLanguagesList() {
        List<String> entries = new ArrayList<String>();
        for (String key : languages.keySet()) {
            entries.add(languages.get(key).
                    getFirstElement().
                    concat(": ").
                    concat(key)
            );
        }
        return CommonUtils.getCollectionElements(entries, "(", ")", ", ");
    }

}
