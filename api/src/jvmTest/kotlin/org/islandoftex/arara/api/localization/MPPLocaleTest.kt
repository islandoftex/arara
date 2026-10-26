// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MPPLocaleTest {

    @Test
    fun languageTagsPreserveLocaleIdentityAndFormatting() {
        val french = MPPLocale("fr-FR")
        val same = MPPLocale("fr-FR")
        val english = MPPLocale("en-US")

        assertEquals(Locale.FRANCE, french.toJVMLocale())
        assertEquals(Locale.FRANCE.displayLanguage, french.displayLanguage)
        assertEquals(',', french.decimalSeparator)
        assertEquals('.', english.decimalSeparator)
        assertEquals(Locale.FRANCE.toString(), french.toString())
        assertEquals(french, french)
        assertEquals(french, same)
        assertEquals(french.hashCode(), same.hashCode())
        assertNotEquals(french, english)
        assertNotEquals<Any?>(french, null)
        assertNotEquals<Any>(french, Locale.FRANCE)
    }
}
