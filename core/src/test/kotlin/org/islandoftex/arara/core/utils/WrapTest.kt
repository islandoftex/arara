// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the shared wrapping primitive: word-boundary wrapping, the
 * single-line behavior for short and long words, the empty input
 * that dialog bodies may carry, the embedded line breaks that
 * act as hard breaks, and the narrower first-line budget a prose
 * opening beside a leader fills.
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
        assertEquals(
            listOf("supercalifragilistic"),
            "supercalifragilistic".wrap(10).lines(),
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

    @Test
    fun `budgets the first line narrower than the rest`() {
        assertEquals(
            "This text\nshould be wrapped across\nthe lines",
            "This text should be wrapped across the lines".wrap(25, 10),
            "the first line fills the leader's budget, the rest the width"
        )
    }

    @Test
    fun `a first-line budget equal to the width changes nothing`() {
        val message = "a long message of several words"
        assertEquals(
            message.wrap(10),
            message.wrap(10, 10),
            "the single-width call and the equal two-width call agree"
        )
    }

    @Test
    fun `a hard break resets the budget to the full width`() {
        val message =
            "one item that is long\nsecond item wraps alone here"
        assertEquals(
            "one item\nthat is long\nsecond item wraps\nalone here",
            message.wrap(19, 9),
            "only the text's first line takes the narrow budget"
        )
    }
}
