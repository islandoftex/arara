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
 * AraraLocalization: This class contains a few helper methods in order to ease
 * the localization process during the execution. This class is a singleton
 * reference.
 */
// package definition
package com.github.arara.utils;

// needed imports
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contains a few helper methods in order to ease the localization process
 * during the execution. This class is a singleton reference.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class AraraLocalization {

    // the singleton reference
    private static AraraLocalization selfRef;
    // the resource bundle for the whole application
    private ResourceBundle resources;

    /**
     * Constructs the singleton instance.
     */
    private AraraLocalization() {

        // set the self-reference
        selfRef = this;

        // instead of relying in the default locale from the
        // operating system, always fallback to english
        Locale.setDefault(Locale.ENGLISH);
        
        // get the resource bundle
        resources = ResourceBundle.getBundle("com.github.arara.localization.Messages");
    }

    /**
     * Provides reference to the singleton object.
     *
     * @return The singleton instance.
     */
    public static final AraraLocalization getInstance() {

        // if there's no current object, call the private constructor
        if (selfRef == null) {
            selfRef = new AraraLocalization();
        }

        // return the actual object
        return selfRef;
    }

    /**
     * Returns a simple message from the resource bundle.
     *
     * @param message The message identifier.
     * @return A string from the resource bundle.
     */
    public String getMessage(String message) {
        return resources.getString(message);
    }

    /**
     * Returns a message containing parameters from the resource bundle.
     *
     * @param message The message identifier.
     * @param parameters A list of parameters to be replaced in the message.
     * @return A string from the resource bundle, with the correct parameters.
     */
    public String getMessage(String message, Object... parameters) {
        return MessageFormat.format(resources.getString(message), parameters);
    }

    /**
     * Refresh the resource bundle reference, if by any chance, the application
     * language is changed in runtime.
     */
    public void refresh() {

        // clear cache and reload the resource bundle
        // ResourceBundle.clearCache(); // not supported by Java 5
        resources = ResourceBundle.getBundle("com.github.arara.localization.Messages");
    }
}
