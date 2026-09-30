// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Verifies the choice interpretation of the terminal dialogs: valid
 * numbers resolve to themselves, the no-selection contract of the
 * dropdown, and everything else asks again.
 */
class PlainDialogRendererTest {
    @Test
    fun `valid choices resolve to their number`() {
        listOf("1", "2", " 3 ", "1 ").forEach { raw ->
            assertEquals(1, PlainDialogRenderer.parseSelection("1", 3, false))
        }
        assertEquals(3, PlainDialogRenderer.parseSelection("3", 3, false))
        assertEquals(2, PlainDialogRenderer.parseSelection(" 2 ", 3, true))
    }

    @Test
    fun `the dropdown honors a deliberate no-selection`() {
        assertEquals(0, PlainDialogRenderer.parseSelection("0", 3, true))
        assertEquals(0, PlainDialogRenderer.parseSelection("", 3, true))
        assertEquals(0, PlainDialogRenderer.parseSelection("   ", 3, true))
    }

    @Test
    fun `out-of-range and malformed input asks again`() {
        assertNull(PlainDialogRenderer.parseSelection("0", 3, false))
        assertNull(PlainDialogRenderer.parseSelection("4", 3, false))
        assertNull(PlainDialogRenderer.parseSelection("-1", 3, false))
        assertNull(PlainDialogRenderer.parseSelection("yes", 3, false))
        assertNull(PlainDialogRenderer.parseSelection("1.5", 3, false))
        assertNull(PlainDialogRenderer.parseSelection("", 3, false))
        assertNull(PlainDialogRenderer.parseSelection("2x", 3, true))
    }
}
