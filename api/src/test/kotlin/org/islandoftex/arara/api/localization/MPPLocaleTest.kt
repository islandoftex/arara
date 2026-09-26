// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Verifies the value members of the multiplatform locale: equality,
 * hashing, the string form and the JVM passthrough.
 */
class MPPLocaleTest {
    @Test
    fun `the value members hold the locale contract`() {
        val english = MPPLocale("en")
        val same = MPPLocale("en")
        val other = MPPLocale("pt")
        assertEquals(english, same, "identical locales compare equal")
        assertEquals(english.hashCode(), same.hashCode())
        assertNotEquals(english, other, "different locales compare unequal")
        assertFalse(english.equals("not a locale"))
        assertTrue(english.toString().isNotBlank(), "the string form names the locale")
        assertEquals(
            "en",
            same.toJVMLocale().language,
            "the JVM locale passes through"
        )
    }
}
