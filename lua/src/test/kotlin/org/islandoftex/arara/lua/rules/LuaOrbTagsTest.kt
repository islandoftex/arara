// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException

/**
 * Verifies the Lua orb tag interpolation for directive options: literal
 * text preservation, multiple tags, nil results, quoted braces, the
 * conditional helper access and the error paths.
 */
class LuaOrbTagsTest {
    @Test
    fun `keeps plain strings unchanged`() {
        assertEquals(
            "no tags here", LuaOrbTags.interpolate("no tags here"),
            "plain strings pass through unchanged"
        )
    }

    @Test
    fun `interpolates expressions with surrounding text`() {
        assertEquals(
            "pre2post", LuaOrbTags.interpolate("pre@{1 + 1}post"),
            "the orb tag result is spliced into the surrounding text"
        )
    }

    @Test
    fun `interpolates multiple tags`() {
        assertEquals(
            "1 and 2", LuaOrbTags.interpolate("@{1} and @{1 + 1}"),
            "multiple orb tags are all interpolated"
        )
    }

    @Test
    fun `interpolates nil as an empty string`() {
        assertEquals(
            "xy", LuaOrbTags.interpolate("x@{nil}y"),
            "a nil result interpolates as an empty string"
        )
    }

    @Test
    fun `honors braces inside quoted strings`() {
        assertEquals(
            "a}b", LuaOrbTags.interpolate("@{'a}b'}"),
            "braces inside quoted strings do not terminate the tag"
        )
    }

    @Test
    fun `exposes the conditional helpers`() {
        // the conditional registry is available to orb tag expressions
        assertEquals(
            "false",
            LuaOrbTags.interpolate("@{exists(toFile('quack.tex'))}")
        )
    }

    @Test
    fun `rejects unterminated tags`() {
        assertFailsWith<AraraException>(
            "an unterminated orb tag must be rejected"
        ) {
            LuaOrbTags.interpolate("pre@{1 + 1")
        }
    }

    @Test
    fun `wraps evaluation failures`() {
        val exception = assertFailsWith<AraraException>(
            "a malformed orb tag expression must be rejected"
        ) {
            LuaOrbTags.interpolate("@{1 +}")
        }
        assertTrue(exception.message != null, "the rejection carries a message")
    }
}
