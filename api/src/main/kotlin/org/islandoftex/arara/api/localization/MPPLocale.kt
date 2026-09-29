// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * A locale representation, identified by an IETF BCP 47 language tag such
 * as `en` or `pt-BR`.
 *
 * @author Island of TeX
 */
public class MPPLocale constructor(tag: String) {
    private val locale: Locale = Locale.forLanguageTag(tag)

    /**
     * The name of the locale's language, localized for display.
     */
    public val displayLanguage: String = locale.displayLanguage

    /**
     * The decimal separator character used by the locale, e.g. `,` for
     * German.
     */
    public val decimalSeparator: Char = DecimalFormatSymbols(locale)
        .decimalSeparator

    /**
     * Convert the multiplatform locale to a JVM [Locale].
     */
    public fun toJVMLocale(): Locale = locale

    /**
     * The hash code is derived from the wrapped JVM locale, consistent
     * with [equals].
     */
    override fun hashCode(): Int =
        locale.hashCode()
    /**
     * The string representation is the one of the wrapped JVM locale.
     */
    override fun toString(): String =
        locale.toString()

    /**
     * Two locales are equal if they wrap the same JVM locale.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPLocale

        if (locale != other.locale) return false

        return true
    }
}
