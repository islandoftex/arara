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
 * AraraLanguage: This class holds the data model for the arara language
 * specification.
 */
// package definition.
package com.github.arara.model;

// needed import
import java.util.Locale;

/**
 * Holds the data model for the arara language specification.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class AraraLanguage {

    // language name
    private String name;
    // language code
    private String code;
    // language locale
    private Locale locale;

    /**
     * Getter for the language name.
     *
     * @return The language name.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the language name.
     *
     * @param name The language name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the language code.
     *
     * @return The language code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter for the language code.
     *
     * @param code The language code.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter for the language locale.
     *
     * @return The language locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Setter for the language locale.
     *
     * @param locale The language locale.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Constructor.
     *
     * @param name The language name.
     * @param code The language code.
     * @param locale The language locale.
     */
    public AraraLanguage(String name, String code, Locale locale) {
        this.name = name;
        this.code = code;
        this.locale = locale;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return The string representation of the object.
     */
    @Override
    public String toString() {
        return name.concat(" [").concat(code).concat("]");
    }
}
