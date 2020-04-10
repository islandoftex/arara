// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.localization

import ch.qos.cal10n.IMessageConveyor
import ch.qos.cal10n.MessageConveyor
import java.util.Locale
import org.islandoftex.arara.cli.configuration.AraraSpec

/**
 * Implements the language controller. This controller provides a singleton
 * object that holds the application messages, easily available to all model
 * and utilitary classes.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object LanguageController {
    // the message conveyor helps us to get localized messages
    // according to the provided locale
    // The fallback language is set to English for all
    // messages in arara.
    private var conveyor: IMessageConveyor = MessageConveyor(Locale(
            AraraSpec.userInterfaceOptions.default.languageCode))

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
