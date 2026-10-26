// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.files.toMPPPath

class LuaHelperBindingsTest {
    @Test
    fun aliasesAndOpaqueCommands() {
        val globals = LuaRuleEnvironment.newGlobals()
        assertEquals("a-b", globals.load("return join({'a', 'b'}, '-')").call().tojstring())
        assertTrue(globals.load("return is_string(trim_spaces(' a '))").call().toboolean())
        val command = globals.load("return get_command_with_working_directory('.', 'echo', {'a', 'b'})").call()
        assertTrue(LuaConversion.toCommand(command) != null)
        assertTrue(globals.get("show_list").isfunction())
        assertTrue(globals.get("showList").isfunction())
    }

    @Test
    fun namedCommandsPreserveInvocationAndLeaveTheOriginalUnchanged() {
        val globals = LuaRuleEnvironment.newGlobals()
        val original = globals.load("return get_command_with_working_directory('.', 'echo', 'a b')").call()
        globals.set("original", original)
        val command = LuaConversion.toCommand(original)!!
        for (helper in listOf("named_command", "namedCommand")) {
            val named = LuaConversion.toCommand(globals.load("return $helper('Cleaning aux', original)").call())!!
            assertEquals("Cleaning aux", named.displayName)
            assertEquals(command.elements, named.elements)
            assertEquals(command.workingDirectory, named.workingDirectory)
            assertEquals(null, command.displayName)
            assertFailsWith<Exception> { globals.load("return $helper('label', true)").call() }
            assertFailsWith<Exception> { globals.load("return $helper(42, original)").call() }
        }
    }

    @Test
    fun listHelpersReplaceTheOldDropdownNamesInBothEnvironments() {
        for (globals in listOf(LuaRuleEnvironment.newGlobals(), LuaConditionalEnvironment.newGlobals())) {
            assertTrue(globals.get("showList").isfunction())
            assertTrue(globals.get("show_list").isfunction())
            assertTrue(globals.get("showDropdown").isnil())
            assertTrue(globals.get("show_dropdown").isnil())
        }
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

    @Test
    fun consolidatedHelpersKeepBothSpellingsAndPathRepresentations() {
        val root = Files.createTempDirectory("arara Lua helpers ").toMPPPath()
        val target = root.resolve("output.txt")
        try {
            val globals = LuaRuleEnvironment.newGlobals()
            for ((write, read, replicate) in listOf(
                Triple("writeToFile", "readFromFile", "replicatePattern"),
                Triple("write_to_file", "read_from_file", "replicate_pattern")
            )) {
                for (value in listOf(target, target.toString())) {
                    globals.set("target", LuaConversion.toLua(value))
                    assertTrue(globals.load(
                        "return $write(target, $replicate('%s!', {'one', 'two'}), false)"
                    ).call().toboolean())
                    assertEquals(listOf("one!", "two!"), LuaConversion.fromLua(
                        globals.load("return $read(target)").call()
                    ))
                    assertTrue(globals.load("return $write(target, 'three', false)").call().toboolean())
                    assertEquals("three", target.readText())
                }
            }
            for (environment in listOf(globals, LuaConditionalEnvironment.newGlobals())) {
                environment.set("target", LuaConversion.toLua(target))
                assertTrue(environment.load("return found(target, 'three')").call().toboolean())
                assertEquals(false, environment.load("return found(target, 'absent')").call().toboolean())
            }
            globals.set("target", LuaConversion.toLua(root))
            assertEquals(false, globals.load("return write_to_file(target, 'nope', false)").call().toboolean())
        } finally {
            root.removeDirectory()
        }
    }
}
