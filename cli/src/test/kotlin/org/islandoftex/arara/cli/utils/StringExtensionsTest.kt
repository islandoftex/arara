// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringExtensionsTest {
    @Test
    fun shouldAbbreviateStringsCorrectly() {
        assertEquals(
            "Quack…", "Quack quack".abbreviate(6),
            "abbreviating to the ellipsis width"
        )
        assertEquals(
            "Quack Quack", "Quack Quack".abbreviate(80),
            "strings shorter than the width are left unchanged"
        )
    }
    @Test
    fun shouldFailAbbreviatingShorterThanEllipsis() {
        assertFailsWith<IllegalArgumentException>(
            "abbreviating below the ellipsis length must be rejected"
        ) {
            "Quack".abbreviate(1)
        }
    }

    @Test
    fun shouldCenterStringsCorrectly() {
        assertEquals(
            "Quack", "Quack".center(3, '-'),
            "strings longer than the width are not padded"
        )
        assertEquals(
            "--Quack--", "Quack".center(9, '-'),
            "padding is distributed to both sides"
        )
    }

    @Test
    fun shouldWrapStringsCorrectly() {
        assertEquals(
            "This text\nshould be\nwrapped",
            "This text should be wrapped".wrap(10),
            "the text wraps at the word boundaries"
        )
    }
}
