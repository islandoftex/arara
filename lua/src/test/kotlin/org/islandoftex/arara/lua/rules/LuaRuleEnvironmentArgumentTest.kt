// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaValue
import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Verifies the typed argument contract of the rule helpers: arguments
 * have to match the declared type — Lua's implicit coercions apply to
 * operators, not to arara's library functions, and mismatches are
 * reported as Lua-style bad argument errors. The named conversion
 * helpers (`isTrue` and friends) are exempt: interpreting values is
 * their documented purpose.
 */
class LuaRuleEnvironmentArgumentTest {
    private fun binding(name: String): LuaValue =
        LuaRuleEnvironment.newGlobals().get(name)

    /**
     * Assert that the call fails with the bad argument error naming the
     * helper, the position and the expected type.
     */
    private fun assertBadArgument(
        helper: String,
        index: Int,
        expected: String,
        call: () -> Unit
    ) {
        val exception = assertFailsWith<AraraException>(
            "a wrongly typed argument must be rejected"
        ) { call() }
        assertTrue(
            exception.message?.contains(
                "bad argument #$index to '$helper' ($expected expected"
            ) == true,
            "the error names helper, position and expected type: " +
                "${exception.message}"
        )
    }

    @Test
    fun `rejects numbers where strings are expected`() {
        assertBadArgument("trimSpaces", 1, "string") {
            binding("trimSpaces").call(LuaValue.valueOf(123))
        }
        assertBadArgument("throwError", 1, "string") {
            binding("throwError").call(LuaValue.valueOf(123))
        }
    }

    @Test
    fun `rejects strings where numbers are expected`() {
        assertBadArgument("halt", 1, "number") {
            binding("halt").call(LuaValue.valueOf("1"))
        }
        assertBadArgument("showMessage", 1, "number") {
            binding("showMessage").invoke(
                LuaValue.varargsOf(
                    arrayOf(
                        LuaValue.valueOf("1"),
                        LuaValue.valueOf("title"),
                        LuaValue.valueOf("text")
                    )
                )
            )
        }
    }

    @Test
    fun `rejects strings where booleans are expected`() {
        assertBadArgument("listFilesByExtensions", 3, "boolean") {
            val extensions = LuaValue.tableOf()
                .also { it.set(1, LuaValue.valueOf("tex")) }
            binding("listFilesByExtensions").invoke(
                LuaValue.varargsOf(
                    arrayOf(
                        LuaValue.valueOf("."),
                        extensions,
                        LuaValue.valueOf("yes")
                    )
                )
            )
        }
    }

    @Test
    fun `rejects non command values in the system command helper`() {
        assertBadArgument("unsafelyExecuteSystemCommand", 1, "command") {
            binding("unsafelyExecuteSystemCommand")
                .call(LuaValue.valueOf("ls"))
        }
    }

    @Test
    fun `keeps the named conversion helpers interpreting values`() {
        val globals = LuaRuleEnvironment.newGlobals()
        assertTrue(
            globals.load("return isTrue('true')").call().toboolean(),
            "string values are interpreted by isTrue, not type-checked"
        )
        assertTrue(
            globals.load("return isTrue(true)").call().toboolean(),
            "booleans flow through isTrue unchanged"
        )
        assertTrue(
            !globals.load("return isTrue(false)").call().toboolean(),
            "a false boolean stays false"
        )
    }
}
