// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.Globals
import io.github.taoguan.luaj.LuaError
import io.github.taoguan.luaj.LuaValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.lua.rules.LuaConversion.snakeCase

/**
 * Verifies the snake_case spellings of the helper registry: every
 * camelCase binding of both Lua environments is additionally bound
 * under its derived snake_case alias - the Lua-idiomatic spelling
 * available throughout the lifetime of the deprecated engine - the
 * aliases call through to the very helpers, and argument errors name
 * the spelling the rule actually called.
 */
class LuaHelperSpellingTest {
    private fun camelCaseBindings(globals: Globals): List<String> =
        globals.keys()
            .map { it.tojstring() }
            .filter { name -> name.any(Char::isUpperCase) }
            // the Lua runtime's own _G and _VERSION are conventions
            // of the standard library, not arara helpers
            .filter { name -> !name.startsWith("_") }

    private fun assertSnakeTwins(globals: Globals, world: String) {
        val helpers = camelCaseBindings(globals)
        assertTrue(
            helpers.isNotEmpty(),
            "the $world environment binds camelCase helpers"
        )
        helpers.forEach { name ->
            val twin = name.snakeCase()
            assertTrue(
                globals.get(twin).isfunction(),
                "the helper $name of the $world environment has no " +
                    "snake_case twin $twin"
            )
        }
    }

    @Test
    fun `every rule helper binding has its snake twin`() {
        assertSnakeTwins(LuaRuleEnvironment.newGlobals(), "rule")
    }

    @Test
    fun `every conditional helper binding has its snake twin`() {
        assertSnakeTwins(
            LuaConditionalEnvironment.newGlobals(),
            "conditional"
        )
    }

    @Test
    fun `the snake spellings call through`() {
        val globals = LuaRuleEnvironment.newGlobals()
        assertEquals(
            "arara",
            globals.load("return trim_spaces('  arara  ')")
                .call().tojstring(),
            "trim_spaces trims like trimSpaces"
        )
        assertEquals(
            "arara 6",
            globals.load("return build_string('arara', 6)")
                .call().tojstring(),
            "build_string joins like buildString"
        )
        assertEquals(
            "a b",
            globals.load(
                "return join(replicate_pattern('%s', {'a', 'b'}))"
            ).call().tojstring(),
            "the snake spellings compose with the prelude"
        )
        assertTrue(
            globals.load("return is_true('true')").call().toboolean(),
            "is_true interprets like isTrue"
        )
    }

    @Test
    fun `the prelude type checks offer the snake spellings`() {
        val globals = LuaRuleEnvironment.newGlobals()
        assertTrue(
            globals.load("return is_string('arara')").call().toboolean(),
            "is_string checks like isString"
        )
        assertTrue(
            globals.load("return is_list({})").call().toboolean(),
            "is_list checks like isList"
        )
    }

    @Test
    fun `argument errors name the spelling actually called`() {
        val globals = LuaRuleEnvironment.newGlobals()
        val snake = assertFailsWith<AraraException> {
            globals.get("trim_spaces").call(LuaValue.valueOf(123))
        }
        assertTrue(
            snake.message?.contains(
                "bad argument #1 to 'trim_spaces'"
            ) == true,
            "the error names the snake spelling: ${snake.message}"
        )
        val camel = assertFailsWith<AraraException> {
            globals.get("trimSpaces").call(LuaValue.valueOf(123))
        }
        assertTrue(
            camel.message?.contains(
                "bad argument #1 to 'trimSpaces'"
            ) == true,
            "the error names the historic spelling: ${camel.message}"
        )
    }

    @Test
    fun `the arity guard names the spelling actually called`() {
        val globals = LuaRuleEnvironment.newGlobals()
        val error = assertFailsWith<LuaError> {
            globals.get("is_true").call()
        }
        assertTrue(
            error.message?.contains("is_true requires at least one argument")
                == true,
            "the guard names the snake spelling: ${error.message}"
        )
    }
}
