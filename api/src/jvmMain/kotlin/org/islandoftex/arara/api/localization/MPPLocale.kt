// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

import java.util.Locale

public actual class MPPLocale actual constructor(tag: String) {
    private val locale: Locale = Locale.forLanguageTag(tag)

    public actual val displayLanguage: String = locale.displayLanguage

    public fun toJVMLocale(): Locale = locale

    override fun equals(other: Any?): Boolean =
            locale == other
    override fun hashCode(): Int =
            locale.hashCode()
    override fun toString(): String =
            locale.toString()
}
