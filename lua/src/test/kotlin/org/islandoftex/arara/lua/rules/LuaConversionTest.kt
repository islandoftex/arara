// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.lib.jse.JsePlatform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.Session

/**
 * Verifies the conversion helpers between Kotlin and Lua values,
 * including the deliberately lenient fallback branches.
 */
class LuaConversionTest {
    @Test
    fun `converts function values through the string fallback`() {
        val function = JsePlatform.standardGlobals()
            .load("return function() end")
            .call()
        // functions cannot cross into the Kotlin world as objects, so
        // the fallback renders them as strings instead of crashing
        val converted = LuaConversion.fromLua(function)
        assertTrue(
            converted is String && converted.contains("function"),
            "a Lua function converts through the fallback branch, " +
                "named as such: $converted"
        )
    }

    @Test
    fun `converts scalar values in both directions`() {
        assertEquals("text", LuaConversion.fromLua(LuaConversion.toLua("text")))
        assertEquals(true, LuaConversion.fromLua(LuaConversion.toLua(true)))
        assertEquals(6, LuaConversion.fromLua(LuaConversion.toLua(6)))
        // out-of-range integrals stay exact as longs, as an int would
        // silently wrap them into unrelated values
        assertEquals(
            10000000000000L,
            LuaConversion.fromLua(LuaConversion.toLua(10000000000000L))
        )
        assertEquals(1.5, LuaConversion.fromLua(LuaConversion.toLua(1.5)))
    }

    @Test
    fun `wraps file objects as userdata`() {
        val wrapped = LuaConversion.toLua(MPPPath("quack.tex"))
        assertTrue(wrapped.isuserdata())
        assertEquals(MPPPath("quack.tex"), LuaConversion.fromLua(wrapped))
    }

    @Test
    fun `exposes the session operations including the clear`() {
        Session.put("arara-lua-conversion-test", "value")
        val table = LuaConversion.sessionTable()
        assertTrue(Session.contains("arara-lua-conversion-test"))
        table.get("clear").call()
        assertFalse(Session.contains("arara-lua-conversion-test"))
        // the clear wiped the shared session of the whole machine;
        // the environment import brings its bulk back for the
        // suites that follow
        Session.updateEnvironmentVariables()
    }
}
