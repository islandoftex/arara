// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Verifies the choice interpretation of the terminal dialogs: valid
 * numbers resolve to themselves, the no-selection contract of the
 * dropdown, and everything else asks again.
 */
class TerminalDialogsTest {
    @Test
    fun `valid choices resolve to their number`() {
        listOf("1", "2", " 3 ", "1 ").forEach { raw ->
            assertEquals(1, TerminalDialogs.parseSelection("1", 3, false))
        }
        assertEquals(3, TerminalDialogs.parseSelection("3", 3, false))
        assertEquals(2, TerminalDialogs.parseSelection(" 2 ", 3, true))
    }

    @Test
    fun `the dropdown honors a deliberate no-selection`() {
        assertEquals(0, TerminalDialogs.parseSelection("0", 3, true))
        assertEquals(0, TerminalDialogs.parseSelection("", 3, true))
        assertEquals(0, TerminalDialogs.parseSelection("   ", 3, true))
    }

    @Test
    fun `out-of-range and malformed input asks again`() {
        assertNull(TerminalDialogs.parseSelection("0", 3, false))
        assertNull(TerminalDialogs.parseSelection("4", 3, false))
        assertNull(TerminalDialogs.parseSelection("-1", 3, false))
        assertNull(TerminalDialogs.parseSelection("yes", 3, false))
        assertNull(TerminalDialogs.parseSelection("1.5", 3, false))
        assertNull(TerminalDialogs.parseSelection("", 3, false))
        assertNull(TerminalDialogs.parseSelection("2x", 3, true))
    }
}
