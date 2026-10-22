// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.session.Session
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RuleValueContractTest {
    @Test
    fun `trims spaces from strings`() {
        assertEquals("arara", RuleMethods.trimSpaces("  arara  "))
    }

    @Test
    fun `retrieves list elements safely`() {
        val list = listOf("first", "second")
        assertEquals("first", RuleMethods.getOrNull(list))
        assertEquals("second", RuleMethods.getOrNull(list, 1))
        assertNull(RuleMethods.getOrNull(list, 5))
        assertNull(RuleMethods.getOrNull(emptyList()))
    }

    @Test
    fun `builds strings from mixed objects`() {
        assertEquals("arara 6", RuleMethods.buildString("arara", 6))
        assertEquals("arara", RuleMethods.buildString("arara", null, ""))
        assertEquals("", RuleMethods.buildString(null, ""))
    }

    @Test
    fun `replicates patterns over values`() {
        assertEquals(listOf<Any>("a", "b"), RuleMethods.replicatePattern("%s", listOf("a", "b")))
        assertEquals(listOf<Any>("1", "2"), RuleMethods.replicatePattern("%s", listOf(1, 2)))
    }

    @Test
    fun `rejects patterns with missing format specifiers`() {
        assertFailsWith<AraraException> {
            RuleMethods.replicatePattern("%s %s", listOf("lonely"))
        }
    }

    @Test
    fun `throws the header exception on demand`() {
        val exception = assertFailsWith<AraraException> { RuleMethods.throwError("boom") }
        assertEquals("boom", exception.message)
    }

    @Test
    fun `exposes the session`() {
        assertSame(Session, RuleMethods.getSession())
    }

    @Test
    fun `checks string emptiness`() {
        assertTrue(RuleMethods.isEmpty(""))
        assertFalse(RuleMethods.isEmpty("quack"))
        assertFalse(RuleMethods.isNotEmpty(""))
        assertTrue(RuleMethods.isNotEmpty("quack"))
        assertEquals("yes", RuleMethods.isEmpty("", "yes", "no"))
        assertEquals("no", RuleMethods.isEmpty("quack", "yes", "no"))
        assertEquals("", RuleMethods.isEmpty("quack", "yes"))
        assertEquals("yes", RuleMethods.isNotEmpty("quack", "yes", "no"))
        assertEquals("no", RuleMethods.isNotEmpty("", "yes", "no"))
    }

    @Test
    fun `interprets boolean strings`() {
        assertTrue(RuleMethods.isTrue("yes"))
        assertTrue(RuleMethods.isTrue("on"))
        assertFalse(RuleMethods.isTrue("no"))
        assertTrue(RuleMethods.isFalse("no"))
        assertFalse(RuleMethods.isTrue(""))
        assertFalse(RuleMethods.isFalse(""))
        assertFailsWith<AraraException> { RuleMethods.isTrue("quack") }
        assertFailsWith<AraraException> { RuleMethods.isFalse("quack") }
    }

    @Test
    fun `maps boolean strings to values`() {
        assertEquals("yes", RuleMethods.isTrue("yes", "yes", "no"))
        assertEquals("no", RuleMethods.isTrue("no", "yes", "no"))
        assertEquals("", RuleMethods.isTrue("no", "yes"))
        assertEquals("yes", RuleMethods.isFalse("no", "yes", "no"))
        assertEquals("no", RuleMethods.isFalse("yes", "yes", "no"))
        assertEquals("", RuleMethods.isFalse("yes", "yes"))
        assertEquals("fallback", RuleMethods.isTrue("", "yes", "no", "fallback"))
        assertEquals("yes", RuleMethods.isTrue("yes", "yes", "no", "fallback"))
        assertEquals("fallback", RuleMethods.isFalse("", "yes", "no", "fallback"))
        assertEquals("yes", RuleMethods.isFalse("no", "yes", "no", "fallback"))
    }

    @Test
    fun `maps boolean expressions to values`() {
        assertEquals("yes", RuleMethods.isTrue(true, "yes", "no"))
        assertEquals("no", RuleMethods.isTrue(false, "yes", "no"))
        assertEquals("", RuleMethods.isTrue(false, "yes"))
        assertEquals("yes", RuleMethods.isFalse(false, "yes", "no"))
        assertEquals("no", RuleMethods.isFalse(true, "yes", "no"))
        assertEquals("", RuleMethods.isFalse(true, "yes"))
    }

    @Test
    fun `checks types by class membership`() {
        assertTrue(RuleMethods.checkClass(String::class.java, "quack"))
        assertFalse(RuleMethods.checkClass(String::class.java, 42))
        assertTrue(RuleMethods.isString("quack"))
        assertFalse(RuleMethods.isString(42))
        assertTrue(RuleMethods.isList(listOf(1)))
        assertFalse(RuleMethods.isList("quack"))
        assertTrue(RuleMethods.isMap(mapOf(1 to 2)))
        assertFalse(RuleMethods.isMap(listOf(1)))
        // booleans live in the boxed class, so a primitive check would
        // always report false
        assertTrue(RuleMethods.isBoolean(true))
        assertFalse(RuleMethods.isBoolean(42))
        assertFalse(RuleMethods.isBoolean("true"))
    }

    @Test
    fun `looks commands up on the system path`() {
        assertFalse(RuleMethods.isOnPath("arara-not-a-real-command-quack"))
    }

    @Test
    fun `the system checks mirror each other`() {
        val windows = RuleMethods.isWindows()
        val linux = RuleMethods.isLinux()
        val mac = RuleMethods.isMac()
        assertEquals(linux || mac, RuleMethods.isUnix())
        assertEquals(if (windows) "yes" else "no", RuleMethods.isWindows("yes", "no"))
        assertEquals(
            if (RuleMethods.isCygwin()) "yes" else "no",
            RuleMethods.isCygwin("yes", "no")
        )
        assertEquals(if (linux) "yes" else "no", RuleMethods.isLinux("yes", "no"))
        assertEquals(if (mac) "yes" else "no", RuleMethods.isMac("yes", "no"))
        assertEquals(if (RuleMethods.isUnix()) "yes" else "no", RuleMethods.isUnix("yes", "no"))
    }
}
