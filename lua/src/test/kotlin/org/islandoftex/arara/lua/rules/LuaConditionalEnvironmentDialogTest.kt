// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaValue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the dialog bindings of the Lua conditional environment end
 * to end through redirected streams: the choices flow to the terminal
 * dialogs, the answers flow back to Lua, and the empty-line and range
 * semantics match the MVEL side.
 */
class LuaConditionalEnvironmentDialogTest {
    private val globals = LuaConditionalEnvironment.newGlobals()
    private val originalIn = System.`in`
    private val originalOut = System.out

    @AfterTest
    fun restoreStreams() {
        System.setIn(originalIn)
        System.setOut(originalOut)
    }

    private fun withStreams(
        input: String,
        block: () -> LuaValue
    ): Pair<LuaValue, String> {
        val sink = ByteArrayOutputStream()
        System.setIn(ByteArrayInputStream(input.toByteArray()))
        System.setOut(PrintStream(sink, true))
        val result = block()
        System.setOut(originalOut)
        System.setIn(originalIn)
        return result to sink.toString("UTF-8")
    }
    private fun call(name: String, vararg arguments: LuaValue): LuaValue =
        globals.get(name).invoke(LuaValue.varargsOf(arguments)).arg1()

    @Test
    fun `the input binding trims and returns the answer`() {
        val (result, output) = withStreams("  Paulo  \n") {
            call(
                "showInput",
                LuaValue.valueOf(4),
                LuaValue.valueOf("Your name"),
                LuaValue.valueOf("What should I call you?")
            )
        }
        assertEquals("Paulo", result.tojstring(), "the answer flows back trimmed")
        assertTrue("QUESTION — Your name" in output, "the block renders")
        assertTrue("What should I call you?" in output, "the body renders")
    }

    @Test
    fun `the options binding re-prompts and resolves`() {
        val (result, output) = withStreams("9\n2\n") {
            call(
                "showOptions",
                LuaValue.valueOf(4),
                LuaValue.valueOf("Keep going?"),
                LuaValue.valueOf("Shall we finish?"),
                LuaValue.valueOf("Yes"),
                LuaValue.valueOf("No")
            )
        }
        assertEquals(2, result.toint(), "the valid choice flows back")
        assertEquals(
            2,
            Regex("Choose an option \\(1-2\\): ").findAll(output).count(),
            "the binding re-prompts on the invalid choice"
        )
    }

    @Test
    fun `the dropdown binding honors the no-selection`() {
        val (result, output) = withStreams("0\n") {
            call(
                "showDropdown",
                LuaValue.valueOf(2),
                LuaValue.valueOf("Pick one"),
                LuaValue.valueOf("Choose an engine"),
                LuaValue.valueOf("pdflatex"),
                LuaValue.valueOf("lualatex"),
                LuaValue.valueOf("xelatex")
            )
        }
        assertEquals(0, result.toint(), "the no-selection flows back")
        assertTrue("(0-3)" in output, "the range offers the no-selection")
    }

    @Test
    fun `the dialogs stay available in safe run mode`() {
        // the safe mode rejects arbitrary execution and class loading,
        // never the dialogs: a dialog is a conversation with the user,
        // the very person who asked for the safe run - and a headless
        // safe run meets the closed-input error instead, by design
        val original = LinearExecutor.executionOptions
        LinearExecutor.executionOptions =
            org.islandoftex.arara.core.configuration.ExecutionOptions(
                executionMode =
                    org.islandoftex.arara.api.configuration.ExecutionMode.SAFE_RUN
            )
        try {
            val (result, _) = withStreams("Paulo\n") {
                call(
                    "showInput",
                    LuaValue.valueOf(4),
                    LuaValue.valueOf("Your name"),
                    LuaValue.valueOf("What should I call you?")
                )
            }
            assertEquals("Paulo", result.tojstring(), "the dialog answers in safe mode")
        } finally {
            LinearExecutor.executionOptions = original
        }
    }
}
