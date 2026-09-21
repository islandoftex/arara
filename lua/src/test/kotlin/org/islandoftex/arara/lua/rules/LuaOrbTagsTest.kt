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
            "no tags here", LuaOrbTags.interpolate("no tags here")
        )
    }

    @Test
    fun interpolates_expressions_with_surrounding_text() {
        assertEquals(
            "pre2post", LuaOrbTags.interpolate("pre@{1 + 1}post")
        )
    }

    @Test
    fun interpolates_multiple_tags() {
        assertEquals(
            "1 and 2", LuaOrbTags.interpolate("@{1} and @{1 + 1}")
        )
    }

    @Test
    fun interpolates_nil_as_an_empty_string() {
        assertEquals(
            "xy", LuaOrbTags.interpolate("x@{nil}y")
        )
    }

    @Test
    fun honors_braces_inside_quoted_strings() {
        assertEquals(
            "a}b", LuaOrbTags.interpolate("@{'a}b'}")
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
        assertFailsWith<AraraException> {
            LuaOrbTags.interpolate("pre@{1 + 1")
        }
    }

    @Test
    fun wraps_evaluation_failures() {
        val exception = assertFailsWith<AraraException> {
            LuaOrbTags.interpolate("@{1 +}")
        }
        assertTrue(exception.message != null)
    }
}
