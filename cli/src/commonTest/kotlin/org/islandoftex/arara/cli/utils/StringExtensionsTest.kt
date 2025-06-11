// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringExtensionsTest {
    @Test
    fun shouldAbbreviateStringsCorrectly() {
        assertEquals("Quack…", "Quack quack".abbreviate(6))
        assertEquals("Quack Quack", "Quack Quack".abbreviate(80))
    }

    @Test
    fun shouldFailAbbreviatingShorterThanEllipsis() {
        assertFailsWith<IllegalArgumentException> {
            "Quack".abbreviate(1)
        }
    }

    @Test
    fun shouldCenterStringsCorrectly() {
        assertEquals("Quack", "Quack".center(3, '-'))
        assertEquals("--Quack--", "Quack".center(9, '-'))
    }

    @Test
    fun shouldWrapStringsCorrectly() {
        assertEquals(
            "This text\nshould be\nwrapped",
            "This text should be wrapped".wrap(10),
        )
    }
}
