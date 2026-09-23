// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaFunction
import io.github.taoguan.luaj.lib.jse.JsePlatform
import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Verifies the exit protocol of Lua rule commands: the `exit` closure
 * has to return a real boolean — numbers and strings are not silently
 * truth-tested, which would otherwise make every exit code pass. Non
 * boolean results report the same localized error the YAML engine
 * produces, with the type detail available as the cause.
 */
class LuaRuleCommandExitTest {
    private fun function(source: String): LuaFunction =
        JsePlatform.standardGlobals()
            .load("return $source")
            .call()
            .checkfunction()

    @Test
    fun `rejects non boolean results from the exit closure`() {
        val command = LuaRuleCommand(
            null,
            function("function() end"),
            function("function(code) return code end")
        )
        val exception = assertFailsWith<AraraException>(
            "a number is not a boolean and must not be truth-tested"
        ) { command.exitCheck(1) }
        assertTrue(
            exception.message?.contains(
                "must always return a boolean value"
            ) == true,
            "the primary message is the localized exit error: " +
                "${exception.message}"
        )
        assertTrue(
            exception.cause?.message?.contains(
                "bad result from 'exit' (boolean expected, got number)"
            ) == true,
            "the type detail is available as the cause: " +
                "${exception.cause?.message}"
        )
    }

    @Test
    fun `accepts boolean results from the exit closure`() {
        val command = LuaRuleCommand(
            null,
            function("function() end"),
            function("function(code) return code == 0 end")
        )
        assertTrue(command.exitCheck(0), "zero passes the exit check")
        assertTrue(!command.exitCheck(1), "non zero codes fail the check")
    }

    @Test
    fun `defaults to zero meaning success without an exit closure`() {
        val command = LuaRuleCommand(null, function("function() end"), null)
        assertTrue(command.exitCheck(0), "zero means success by default")
        assertTrue(!command.exitCheck(1), "non zero codes fail by default")
    }
}
