// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionTest {
    @Test
    fun shouldIncludeAllEnvironmentVariables() {
        Session.clear()
        Session.updateEnvironmentVariables()
        // on Linux it is called PATH, on Windows it may be Path
        assertTrue(
            Session.contains("environment:PATH") ||
                Session.contains("environment:Path")
        )
    }

    @Test
    fun shouldProperlyCheckExistence() {
        Session.clear()
        Session.put("A", "B")
        assertTrue(Session.contains("A"))
        assertFalse(Session.contains("C"))
    }

    @Test
    fun shouldSupportInsertion() {
        Session.clear()
        assertFalse(Session.contains("A"))
        Session.put("A", "B")
        assertTrue(Session.contains("A"))
    }

    @Test
    fun shouldSupportGet() {
        Session.clear()
        Session.put("A", "B")
        assertEquals(Session["A"], "B")
    }

    @Test
    fun shouldSupportRemoval() {
        Session.clear()
        Session.put("A", "B")
        Session.put("C", "D")
        assertTrue(Session.contains("A"))
        assertTrue(Session.contains("C"))
        Session.remove("A")
        assertFalse(Session.contains("A"))
        assertTrue(Session.contains("C"))
    }

    @Test
    fun shouldSupportClearing() {
        Session.clear()
        Session.put("A", "B")
        Session.put("C", "D")
        assertTrue(Session.contains("A"))
        assertTrue(Session.contains("C"))
        Session.clear()
        assertFalse(Session.contains("A"))
        assertFalse(Session.contains("C"))
    }

    @Test
    fun shouldNotThrowOnKnownRemoval() {
        Session.clear()
        Session.put("A", "B")
        try {
            Session.remove("A")
            assertTrue(true)
        } catch (_: AraraException) {
            assertTrue(false)
        }
    }
    @Test
    fun shouldThrowOnUnknownRemoval() {
        Session.clear()
        assertFailsWith<AraraException> {
            Session.remove("A")
        }
    }

    @Test
    fun shouldThrowOnUnknownGetter() {
        Session.clear()
        Session.put("A", "B")
        assertEquals(Session["A"], "B")
        assertFailsWith<AraraException> {
            Session["C"]
        }
    }
}
