/**
 * \cond LICENSE
 * Arara -- the cool TeX automation tool
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
 * \endcond
 * 
 * LanguageController: This class holds the language model controller for arara.
 */
// package definition
package com.github.arara.utils;

// needed imports
import com.github.arara.model.AraraLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Holds the language model controller for arara.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class LanguageController {

    // list of available languages
    private List<AraraLanguage> languages;
    // the localization singleton instance
    final static AraraLocalization localization = AraraLocalization.getInstance();

    /**
     * Constructor.
     */
    public LanguageController() {

        // create a new list of languages
        languages = new ArrayList<AraraLanguage>();

        // add the resources
        languages.add(new AraraLanguage("English", "en", new Locale("en")));
        languages.add(new AraraLanguage("Brazilian Portuguese", "ptbr", new Locale("pt", "BR")));
        languages.add(new AraraLanguage("Italian", "it", new Locale("it")));
        languages.add(new AraraLanguage("French", "fr", new Locale("fr")));
        languages.add(new AraraLanguage("Spanish", "es", new Locale("es")));
        languages.add(new AraraLanguage("German", "de", new Locale("de")));
        languages.add(new AraraLanguage("Turkish", "tr", new Locale("tr", "TR")));
        languages.add(new AraraLanguage("Russian", "ru", new Locale("ru")));

    }

    /**
     * Sets the current language according to the provided country code.
     * 
     * @param code The country code.
     * @return A boolean value indicating if the new language was properly
     * applied.
     */
    public boolean setLanguage(String code) {
        
        // get the index of the language
        int i = getIndex(code);
        
        // if it's valid
        if (i != -1) {
            
            // set locale and refresh the resource bundle
            Locale.setDefault(languages.get(i).getLocale());
            localization.refresh();
            
            // everything went fine
            return true;
        }
        
        // the country code is invalid
        return false;
    }

    /**
     * Returns a string containing the list of available languages.
     * 
     * @return A string containing the list of available languages.
     */
    public String getLanguagesList() {
        
        // get the first one in the list
        String result = languages.get(0).toString();
        
        // iterate through the rest
        for (int i = 1; i < languages.size(); i++) {
            
            // separate them by commas
            result = result.concat(", ").concat(languages.get(i).toString());
        }
        
        // return the string
        return result;
    }

    /**
     * Prints the language help.
     */
    public void printLanguageHelp() {
        
        // print message
        System.out.println(AraraUtils.wrap(localization.getMessage("Error_InvalidLanguage").concat("\n\n").concat(getLanguagesList()).concat("\n")));
    }

    /**
     * Gets the index for the country code.
     * @param code The country code.
     * @return An integer representing the index.
     */
    private int getIndex(String code) {
        
        // for every language
        for (int i = 0; i < languages.size(); i++) {
            
            // if the codes are equal
            if (languages.get(i).getCode().equalsIgnoreCase(code)) {
                
                // return the current counter
                return i;
            }
        }
        
        // nothing was found, return a negative value
        return -1;
    }
}
