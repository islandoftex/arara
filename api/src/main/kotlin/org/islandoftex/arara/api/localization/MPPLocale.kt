// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

import java.text.DecimalFormatSymbols
import java.util.Locale

public class MPPLocale constructor(tag: String) {
    private val locale: Locale = Locale.forLanguageTag(tag)

    public val displayLanguage: String = locale.displayLanguage

    public val decimalSeparator: Char = DecimalFormatSymbols(locale)
        .decimalSeparator

    /**
     * Convert the multiplatform locale to a JVM [Locale].
     */
    public fun toJVMLocale(): Locale = locale

    override fun hashCode(): Int =
        locale.hashCode()
    override fun toString(): String =
        locale.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPLocale

        if (locale != other.locale) return false

        return true
    }
}
