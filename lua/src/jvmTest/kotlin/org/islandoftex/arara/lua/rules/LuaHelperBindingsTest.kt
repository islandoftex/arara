// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraExceptionWithHeader

class LuaHelperBindingsTest {
    @Test
    fun aliasesAndOpaqueCommands() {
        val globals = LuaRuleEnvironment.newGlobals()
        assertEquals("a-b", globals.load("return join({'a', 'b'}, '-')").call().tojstring())
        assertTrue(globals.load("return is_string(trim_spaces(' a '))").call().toboolean())
        val command = globals.load("return get_command_with_working_directory('.', 'echo', {'a', 'b'})").call()
        assertTrue(LuaConversion.toCommand(command) != null)
        assertTrue(globals.get("show_dropdown").isfunction())
        assertTrue(globals.get("showDropdown").isfunction())
    }

    @Test
    fun aliasesReportTheCalledNameAndPreserveAuthoredErrors() {
        val globals = LuaRuleEnvironment.newGlobals()
        val error = assertFailsWith<Exception> { globals.load("return trim_spaces(3)").call() }
        assertTrue(error.toString().contains("trim_spaces"))
        val authored = assertFailsWith<AraraExceptionWithHeader> {
            LuaConversion.withLuaErrorHandling("evaluation") {
                globals.load("throw_error('authored')").call()
            }
        }
        assertEquals("authored", authored.message)
    }

    @Test
    fun conditionalBindingsAndSessionAreShared() {
        val globals = LuaConditionalEnvironment.newGlobals()
        assertTrue(globals.load("return missing(to_file('nonexistent-arara-test-file'))").call().toboolean())
        assertTrue(globals.load("local s=get_session(); s.put('lua-bindings-test', true); " +
            "local v=s.get('lua-bindings-test'); s.remove('lua-bindings-test'); return v").call().toboolean())
        assertFailsWith<Exception> { globals.load("return exists(42)").call() }
    }
}
