// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LuaOrbTagsTest {
    @Test
    fun keeps_plain_strings_unchanged() {
        assertEquals(
            "no tags here", LuaOrbTags.interpolate("no tags here"),
            "plain strings pass through unchanged"
        )
    }

    @Test
    fun interpolates_expressions_with_surrounding_text() {
        assertEquals(
            "pre2post", LuaOrbTags.interpolate("pre@{1 + 1}post"),
            "the orb tag result is spliced into the surrounding text"
        )
    }

    @Test
    fun interpolates_multiple_tags() {
        assertEquals(
            "1 and 2", LuaOrbTags.interpolate("@{1} and @{1 + 1}"),
            "multiple orb tags are all interpolated"
        )
    }

    @Test
    fun interpolates_nil_as_an_empty_string() {
        assertEquals(
            "xy", LuaOrbTags.interpolate("x@{nil}y"),
            "a nil result interpolates as an empty string"
        )
    }

    @Test
    fun honors_braces_inside_quoted_strings() {
        assertEquals(
            "a}b", LuaOrbTags.interpolate("@{'a}b'}"),
            "braces inside quoted strings do not terminate the tag"
        )
    }

    @Test
    fun exposes_the_conditional_helpers() {
        // the conditional registry is available to orb tag expressions
        assertEquals(
            "false",
            LuaOrbTags.interpolate("@{exists(toFile('quack.tex'))}")
        )
    }

    @Test
    fun rejects_unterminated_tags() {
        assertFailsWith<AraraException>(
            "an unterminated orb tag must be rejected"
        ) {
            LuaOrbTags.interpolate("pre@{1 + 1")
        }
    }

    @Test
    fun wraps_evaluation_failures() {
        val exception = assertFailsWith<AraraException>(
            "a malformed orb tag expression must be rejected"
        ) {
            LuaOrbTags.interpolate("@{1 +}")
        }
        assertTrue(exception.message != null, "the rejection carries a message")
    }
}
