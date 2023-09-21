// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputHandlingTest {
    @Test
    fun shouldReturnTruthyValuesForCommonTruthyInput() {
        listOf(
            "yes", "true", "1", "on", "Yes", "True", "On", "YES", "TRUE",
            "ON", "YeS", "yEs", "TrUe", "oN",
        ).forEach {
            assertTrue(InputHandling.checkBoolean(it))
        }
    }

    @Test
    fun shouldReturnFalsyValuesForCommonFalsyInput() {
        listOf(
            "no", "false", "0", "off", "No", "False", "Off", "NO", "FALSE",
            "OFF", "nO", "oFf", "fAlsE",
        ).forEach {
            assertFalse(InputHandling.checkBoolean(it))
        }
    }

    @Test
    fun shouldThrowOnAmbiguousBooleanInput() {
        listOf("quack", "duck", "yess", "yyes", "noo", "nno").forEach {
            assertTrue(
                assertFailsWith<AraraException> {
                    InputHandling.checkBoolean(it)
                }.message?.contains("not a valid boolean") == true,
            )
        }
    }

    @Test
    fun shouldFlattenListsCorrectly() {
        assertEquals(
            InputHandling.flatten(listOf(1, 2, listOf(3, 4, listOf(5, 6))))
                .toSet(),
            (setOf(1, 2, 3, 4, 5, 6) as Set<Any>),
        )
    }
}
