// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.lib.jse.JsePlatform
import kotlin.test.Test
import kotlin.test.assertTrue

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
}
