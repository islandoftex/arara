// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Verifies the terminal output string helpers of the command line
 * application: abbreviation with its ellipsis and exact-fit edges,
 * and the centering of the short and the long string.
 */
class StringExtensionsTest {
    @Test
    fun `abbreviates strings correctly`() {
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
    fun `an exact fit never abbreviates`() {
        assertEquals(
            "Quack quack here", "Quack quack here".abbreviate(16),
            "a string that exactly fills the width is returned unchanged"
        )
    }
    @Test
    fun `fails when abbreviating below the ellipsis length`() {
        assertFailsWith<IllegalArgumentException>(
            "abbreviating below the ellipsis length must be rejected"
        ) {
            "Quack".abbreviate(1)
        }
    }

    @Test
    fun `centers strings correctly`() {
        assertEquals(
            "Quack", "Quack".center(3, '-'),
            "strings longer than the width are not padded"
        )
        assertEquals(
            "--Quack--", "Quack".center(9, '-'),
            "padding is distributed to both sides"
        )
    }
}
