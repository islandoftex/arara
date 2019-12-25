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

import ch.qos.cal10n.IMessageConveyor
import ch.qos.cal10n.MessageConveyor
import org.islandoftex.arara.configuration.AraraSpec
import java.util.*

/**
 * Implements the language controller. This controller provides a singleton
 * object that holds the application messages, easily available to all model
 * and utilitary classes.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object LanguageController {
    // the message conveyor helps us to get localized messages
    // according to the provided locale
    // The fallback language is set to English for all
    // messages in arara.
    private var conveyor: IMessageConveyor = MessageConveyor(Locale(
            AraraSpec.Application.defaultLanguageCode.default))

    /**
     * Sets the current locale. This method actually resets the language
     * conveyor in order to use the new locale. It's quite simple.
     * @param locale The new locale for localized messages through the language
     * conveyor.
     */
    fun setLocale(locale: Locale) {
        conveyor = MessageConveyor(locale)
    }

    /**
     * Gets the localized message indexed by the provided enumeration key,
     * applying an array of objects as parameters. This method is a wrapper to
     * the conveyor's method of the same name.
     * @param E Enumeration type that represents the conveyor messages.
     * @param key Key set in the provided enumeration type.
     * @param parameters Array of objects to be used as parameters.
     * @return A string containing a localized message indexed by the provided
     * enumeration key and applied the array of objects as parameters.
     */
    @Suppress("SpreadOperator")
    fun <E : Enum<*>> getMessage(key: E, vararg parameters: Any): String =
        conveyor.getMessage(key, *parameters)

    /**
     * Gets the localized message indexed by the provided enumeration key. This
     * method is a wrapper to the conveyor's method of the same name.
     * @param E Enumeration type that represents the conveyor messages.
     * @param key Key set in the provided enumeration type.
     * @return A string containing a localized message indexed by the provided
     * enumeration key.
     */
    fun <E : Enum<*>> getMessage(key: E): String = conveyor.getMessage(key)
}
