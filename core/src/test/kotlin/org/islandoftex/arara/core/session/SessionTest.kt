// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies the session map wrapper: insertion, retrieval, removal,
 * clearing and the environment variable import.
 */
class SessionTest {
    @Test
    fun `includes all environment variables`() {
        Session.clear()
        Session.updateEnvironmentVariables()
        // on Linux it is called PATH, on Windows it may be Path
        assertTrue(
            Session.contains("environment:PATH") ||
                Session.contains("environment:Path"),
            "the session holds the system path variable"
        )
    }

    @Test
    fun `properly checks the existence of keys`() {
        Session.clear()
        Session.put("A", "B")
        assertTrue(Session.contains("A"), "the session contains the inserted key")
        assertFalse(Session.contains("C"), "the session omits an unknown key")
    }

    @Test
    fun `supports the insertion of keys`() {
        Session.clear()
        assertFalse(Session.contains("A"), "the key is absent before insertion")
        Session.put("A", "B")
        assertTrue(Session.contains("A"), "the key is present after insertion")
    }

    @Test
    fun `gets the stored value for a key`() {
        Session.clear()
        Session.put("A", "B")
        assertEquals("B", Session["A"], "the stored value of the key")
    }

    @Test
    fun `supports the removal of keys`() {
        Session.clear()
        Session.put("A", "B")
        Session.put("C", "D")
        assertTrue(Session.contains("A"), "the first key is present")
        assertTrue(Session.contains("C"), "the second key is present")
        Session.remove("A")
        assertFalse(Session.contains("A"), "the removed key is absent")
        assertTrue(Session.contains("C"), "the other key survives the removal")
    }

    @Test
    fun `supports clearing the whole session`() {
        Session.clear()
        Session.put("A", "B")
        Session.put("C", "D")
        assertTrue(Session.contains("A"), "the first key is present")
        assertTrue(Session.contains("C"), "the second key is present")
        Session.clear()
        assertFalse(Session.contains("A"), "the first key is gone after clearing")
        assertFalse(Session.contains("C"), "the second key is gone after clearing")
    }

    @Test
    fun `does not throw on the removal of a known key`() {
        Session.clear()
        Session.put("A", "B")
        // any exception thrown here fails the test naturally
        Session.remove("A")
    }
    @Test
    fun `throws on the removal of an unknown key`() {
        Session.clear()
        assertFailsWith<AraraException>(
            "removing an unknown key must be rejected"
        ) {
            Session.remove("A")
        }
    }

    @Test
    fun `throws on reading an unknown key`() {
        Session.clear()
        Session.put("A", "B")
        assertEquals("B", Session["A"], "the stored value of the key")
        assertFailsWith<AraraException>(
            "reading an unknown key must be rejected"
        ) {
            Session["C"]
        }
    }
}
