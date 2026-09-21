// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies the boolean interpretation and the list flattening used by
 * the rule and conditional machinery.
 */
class InputHandlingTest {
    @Test
    fun `returns truthy values for common truthy input`() {
        listOf(
            "yes", "true", "1", "on", "Yes", "True", "On", "YES", "TRUE",
            "ON", "YeS", "yEs", "TrUe", "oN"
        ).forEach {
            assertTrue(
                InputHandling.checkBoolean(it),
                "$it must be recognized as a truthy value"
            )
        }
    }
    @Test
    fun `returns falsy values for common falsy input`() {
        listOf(
            "no", "false", "0", "off", "No", "False", "Off", "NO", "FALSE",
            "OFF", "nO", "oFf", "fAlsE"
        ).forEach {
            assertFalse(
                InputHandling.checkBoolean(it),
                "$it must be recognized as a falsy value"
            )
        }
    }
    @Test
    fun `throws on ambiguous boolean input`() {
        listOf("quack", "duck", "yess", "yyes", "noo", "nno").forEach {
            assertTrue(
                assertFailsWith<AraraException>(
                    "$it must be rejected as an ambiguous boolean value"
                ) {
                    InputHandling.checkBoolean(it)
                }.message?.contains("not a valid boolean") == true,
                "the rejection of $it names the boolean problem"
            )
        }
    }

    @Test
    fun `flattens nested lists correctly`() {
        assertEquals(
            setOf(1, 2, 3, 4, 5, 6) as Set<Any>,
            InputHandling.flatten(listOf(1, 2, listOf(3, 4, listOf(5, 6))))
                .toSet(),
            "the nested lists flatten into a single list of elements"
        )
    }
}
