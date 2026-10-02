// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies the shared wrapping primitive: word-boundary wrapping, the
 * single-line behavior for short and long words, the empty input
 * that dialog bodies may carry, and the embedded line breaks that
 * act as hard breaks.
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

    @Test
    fun `treats embedded line breaks as hard breaks`() {
        assertEquals(
            "one item\nsecond item wraps\nalone here",
            "one item\nsecond item wraps alone here".wrap(19),
            "each embedded line wraps within its own width"
        )
    }

    @Test
    fun `keeps an indented dash list intact`() {
        val message =
            "in order of priority:\n  - /first/path\n  - /second/path"
        assertEquals(
            message,
            message.wrap(30),
            "the list items ride their own lines under the wrapping"
        )
    }

    @Test
    fun `wraps the line after a hard break from its own edge`() {
        val message =
            "an introduction line that is fairly long\n" +
                "  - /the/first/path"
        assertEquals(
            "an introduction line\nthat is fairly long\n  - /the/first/path",
            message.wrap(21),
            "the line after the break starts from the full width again"
        )
    }
}
