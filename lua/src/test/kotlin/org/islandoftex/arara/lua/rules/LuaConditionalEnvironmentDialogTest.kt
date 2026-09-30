// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaValue
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.ui.DialogRenderer
import org.islandoftex.arara.core.ui.HeadlessDialogRenderer
import org.islandoftex.arara.core.ui.TerminalDialogs

/**
 * Verifies the dialog bindings of the Lua conditional environment
 * as a pass-through: the arguments of every binding flow to the
 * dialog renderer the application wired, and the answers flow back
 * to Lua. The rendering, the re-prompt and the answer semantics of
 * the terminal dialogs live with the renderer in the application,
 * so a recording renderer stands in for one here and the binding
 * path is what these tests pin.
 */
class LuaConditionalEnvironmentDialogTest {
    private val globals = LuaConditionalEnvironment.newGlobals()

    /**
     * A renderer that records every conversation and answers from a
     * script, standing in for whatever the host wired.
     */
    private class RecordingRenderer : DialogRenderer {
        val calls = mutableListOf<String>()
        var optionsAnswer = 2
        var inputAnswer = "Paulo"
        var dropdownAnswer = 0

        override fun showMessage(type: Int, title: String, text: String) {
            calls += "showMessage:$type:$title:$text"
        }

        override fun showOptions(
            type: Int,
            title: String,
            text: String,
            buttons: Array<out Any>
        ): Int {
            calls += "showOptions:$type:$title:$text:" + buttons.joinToString(",")
            return optionsAnswer
        }

        override fun showInput(type: Int, title: String, text: String): String {
            calls += "showInput:$type:$title:$text"
            return inputAnswer
        }

        override fun showDropdown(
            type: Int,
            title: String,
            text: String,
            elements: Array<out Any>
        ): Int {
            calls += "showDropdown:$type:$title:$text:" + elements.joinToString(",")
            return dropdownAnswer
        }
    }

    private val recorder = RecordingRenderer()

    init {
        TerminalDialogs.renderer = recorder
    }

    @AfterTest
    fun restoreRenderer() {
        // the headless default is the state between applications, so
        // the wiring never leaks past this suite
        TerminalDialogs.renderer = HeadlessDialogRenderer
    }

    private fun call(name: String, vararg arguments: LuaValue): LuaValue =
        globals.get(name).invoke(LuaValue.varargsOf(arguments)).arg1()

    @Test
    fun `the input binding passes the dialog and returns the answer`() {
        val result = call(
            "showInput",
            LuaValue.valueOf(4),
            LuaValue.valueOf("Your name"),
            LuaValue.valueOf("What should I call you?")
        )
        assertEquals("Paulo", result.tojstring(), "the answer flows back to Lua")
        assertEquals(
            "showInput:4:Your name:What should I call you?",
            recorder.calls.single(),
            "the dialog reaches the wired renderer with its arguments"
        )
    }

    @Test
    fun `the options binding resolves the choice back to Lua`() {
        val result = call(
            "showOptions",
            LuaValue.valueOf(4),
            LuaValue.valueOf("Keep going?"),
            LuaValue.valueOf("Shall we finish?"),
            LuaValue.valueOf("Yes"),
            LuaValue.valueOf("No")
        )
        assertEquals(2, result.toint(), "the choice flows back to Lua")
        assertEquals(
            "showOptions:4:Keep going?:Shall we finish?:Yes,No",
            recorder.calls.single(),
            "the buttons reach the wired renderer"
        )
    }

    @Test
    fun `the dropdown binding honors the no-selection`() {
        recorder.dropdownAnswer = 0
        val result = call(
            "showDropdown",
            LuaValue.valueOf(2),
            LuaValue.valueOf("Pick one"),
            LuaValue.valueOf("Choose an engine"),
            LuaValue.valueOf("pdflatex"),
            LuaValue.valueOf("lualatex"),
            LuaValue.valueOf("xelatex")
        )
        assertEquals(0, result.toint(), "the no-selection flows back to Lua")
        assertEquals(
            "showDropdown:2:Pick one:Choose an engine:pdflatex,lualatex,xelatex",
            recorder.calls.single(),
            "the elements reach the wired renderer"
        )
    }

    @Test
    fun `the dialogs stay available in safe run mode`() {
        // the safe mode rejects arbitrary execution and class loading,
        // never the dialogs: a dialog is a conversation with the user,
        // the very person who asked for the safe run - and a host
        // without dialog support meets the not-configured error
        // instead, by design
        val original = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions(
            executionMode = ExecutionMode.SAFE_RUN
        )
        try {
            val result = call(
                "showInput",
                LuaValue.valueOf(4),
                LuaValue.valueOf("Your name"),
                LuaValue.valueOf("What should I call you?")
            )
            assertEquals(
                "Paulo",
                result.tojstring(),
                "the dialog answers in safe mode"
            )
            assertEquals(1, recorder.calls.size, "the binding routes in safe mode")
        } finally {
            LinearExecutor.executionOptions = original
        }
    }
}
