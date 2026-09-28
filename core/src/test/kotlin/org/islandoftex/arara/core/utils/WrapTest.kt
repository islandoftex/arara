// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies the shared wrapping primitive: word-boundary wrapping, the
 * single-line behavior for short and long words, and the empty input
 * that dialog bodies may carry.
 */
class WrapTest {
    @Test
    fun `wraps strings correctly`() {
        assertEquals(
            "This text\nshould be\nwrapped",
            "This text should be wrapped".wrap(10),
            "the text wraps at the word boundaries"
        )
    }

    @Test
    fun `keeps short strings on one line`() {
        assertEquals(
            "Quack",
            "Quack".wrap(65),
            "short text wraps nowhere"
        )
    }

    @Test
    fun `never breaks a word`() {
        assertTrue(
            "supercalifragilistic".wrap(10).lines()
                .all { it.length <= 20 },
            "an over-long word stays whole"
        )
    }

    @Test
    fun `wraps the empty string to nothing`() {
        assertEquals(
            "",
            "".wrap(65),
            "an empty body renders an empty line"
        )
    }
}
