// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException

/**
 * Verifies the Lua interpolation of directive options: literal text
 * preservation, multiple tags, nil results, quoted and nested braces,
 * the conditional helper access and the error paths.
 */
class LuaInterpolationTest {
    @Test
    fun `keeps plain strings unchanged`() {
        assertEquals(
            "no tags here", LuaInterpolation.interpolate("no tags here"),
            "plain strings pass through unchanged"
        )
    }

    @Test
    fun `interpolates expressions with surrounding text`() {
        assertEquals(
            "pre2post", LuaInterpolation.interpolate("pre{{1 + 1}}post"),
            "the tag result is spliced into the surrounding text"
        )
    }

    @Test
    fun `interpolates multiple tags`() {
        assertEquals(
            "1 and 2", LuaInterpolation.interpolate("{{1}} and {{1 + 1}}"),
            "multiple tags are all interpolated"
        )
    }

    @Test
    fun `interpolates nil as an empty string`() {
        assertEquals(
            "xy", LuaInterpolation.interpolate("x{{nil}}y"),
            "a nil result interpolates as an empty string"
        )
    }

    @Test
    fun `honors braces inside quoted strings`() {
        assertEquals(
            "a}b", LuaInterpolation.interpolate("{{'a}b'}}"),
            "braces inside quoted strings do not terminate the tag"
        )
    }

    @Test
    fun `honors braces inside the expression`() {
        assertEquals(
            "2", LuaInterpolation.interpolate("{{#({1, 2})}}"),
            "nested braces belong to the expression, not the delimiter"
        )
    }

    @Test
    fun `exposes the conditional helpers`() {
        // the conditional registry is available to tag expressions
        assertEquals(
            "false",
            LuaInterpolation.interpolate(
                "{{exists(toFile('quack.tex'))}}"
            )
        )
    }

    @Test
    fun `rejects the orb tag of the other engine`() {
        assertFailsWith<AraraException>(
            "the deprecated YAML engine's tag syntax must be rejected"
        ) {
            LuaInterpolation.interpolate("pre@{1 + 1}post")
        }
    }

    @Test
    fun `rejects unterminated tags`() {
        assertFailsWith<AraraException>(
            "an unterminated tag must be rejected"
        ) {
            LuaInterpolation.interpolate("pre{{1 + 1")
        }
    }

    @Test
    fun `wraps evaluation failures`() {
        val exception = assertFailsWith<AraraException>(
            "a malformed tag expression must be rejected"
        ) {
            LuaInterpolation.interpolate("{{1 +}}")
        }
        assertTrue(exception.message != null, "the rejection carries a message")
    }
}
