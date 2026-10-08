// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaError
import io.github.taoguan.luaj.LuaValue
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.DialogRenderer
import org.islandoftex.arara.core.ui.TerminalDialogs

/**
 * Verifies the rule helper bindings of the Lua environment: the
 * string, boolean, file and system families, including the arity
 * branches of the interpreting helpers. The bindings that read the
 * executor state are exercised inside the lifecycle window in which
 * real directives run; the message helper's binding to the wired
 * renderer is verified here, while the dialog suites verify the
 * rendering whole.
 */
class LuaRuleEnvironmentTest {
    private val globals = LuaRuleEnvironment.newGlobals()

    private fun binding(name: String): LuaValue = globals.get(name)

    private fun evaluate(snippet: String): LuaValue =
        globals.load("return $snippet").call()

    private fun call(name: String, vararg arguments: LuaValue): LuaValue =
        binding(name).invoke(LuaValue.varargsOf(arguments)).arg1()

    @Test
    fun `binds the string helpers`() {
        assertEquals("arara", evaluate("trimSpaces('  arara  ')").tojstring())
        assertEquals("arara 6", evaluate("buildString('arara', 6)").tojstring())
        assertEquals(
            "a b",
            evaluate("join(replicatePattern('%s', {'a', 'b'}))").tojstring()
        )
    }

    @Test
    fun `branches the empty checks on the argument count`() {
        assertTrue(evaluate("isEmpty('')").toboolean())
        assertFalse(evaluate("isEmpty('quack')").toboolean())
        assertEquals("yes", evaluate("isEmpty('', 'yes', 'no')").tojstring())
        assertEquals("no", evaluate("isEmpty('quack', 'yes', 'no')").tojstring())
        assertEquals("", evaluate("isEmpty('quack', 'yes')").tojstring())
        assertFalse(evaluate("isNotEmpty('')").toboolean())
        assertTrue(evaluate("isNotEmpty('quack')").toboolean())
        assertEquals("y", evaluate("isNotEmpty('quack', 'y', 'n')").tojstring())
        assertEquals("n", evaluate("isNotEmpty('', 'y', 'n')").tojstring())
        assertEquals("", evaluate("isNotEmpty('', 'y')").tojstring())
        // empty values of every Lua shape count as empty
        assertTrue(evaluate("isEmpty(nil)").toboolean())
        assertTrue(evaluate("isEmpty({})").toboolean())
        assertFalse(evaluate("isEmpty({'x'})").toboolean())
    }

    @Test
    fun `branches the boolean interpretations on the argument count`() {
        assertTrue(evaluate("isTrue('yes')").toboolean())
        assertTrue(evaluate("isTrue(true)").toboolean())
        assertFalse(evaluate("isTrue(false)").toboolean())
        assertFalse(evaluate("isTrue(nil)").toboolean())
        assertFalse(evaluate("isTrue('')").toboolean())
        assertTrue(evaluate("isFalse('no')").toboolean())
        // the value selectors default and fall back as documented
        assertEquals("y", evaluate("isTrue('yes', 'y', 'n')").tojstring())
        assertEquals("n", evaluate("isTrue('no', 'y', 'n')").tojstring())
        assertEquals("", evaluate("isTrue('no', 'y')").tojstring())
        assertEquals("f", evaluate("isTrue('', 'y', 'n', 'f')").tojstring())
        assertEquals("f", evaluate("isTrue(nil, 'y', 'n', 'f')").tojstring())
        assertEquals("n", evaluate("isTrue('', 'y', 'n')").tojstring())
        assertEquals("y", evaluate("isFalse('no', 'y', 'n')").tojstring())
        // an empty call is rejected with a Lua error
        assertFailsWith<LuaError> { evaluate("isTrue()") }
    }

    @Test
    fun `binds the file name helpers`() {
        val file = Files.createTempFile("arara-lua-name-", ".tex")
        val name = file.fileName.toString().substringBeforeLast('.')
        assertEquals(name, call("getBasename", LuaValue.valueOf(file.toString())).tojstring())
        assertEquals("tex", call("getFiletype", LuaValue.valueOf(file.toString())).tojstring())
    }

    @Test
    fun `binds the stateless queries`() {
        assertFalse(evaluate("isVerboseMode()").toboolean())
        assertFalse(evaluate("isOnPath('arara-not-a-real-command-quack')").toboolean())
        // the plain forms mirror the shared helpers, the ternary forms
        // select between the provided values
        assertEquals(RuleMethods.isLinux(), evaluate("isLinux()").toboolean())
        assertEquals(
            if (RuleMethods.isLinux()) "y" else "n",
            evaluate("isLinux('y', 'n')").tojstring()
        )
        assertEquals(
            if (RuleMethods.isWindows()) "y" else "n",
            evaluate("isWindows('y', 'n')").tojstring()
        )
        assertEquals(
            if (RuleMethods.isUnix()) "y" else "n",
            evaluate("isUnix('y', 'n')").tojstring()
        )
    }

    @Test
    fun `binds the file operations`() {
        val directory = Files.createTempDirectory("arara-lua-files")
        val first = directory.resolve("first.txt")
        assertTrue(
            call(
                "writeToFile", LuaValue.valueOf(first.toString()),
                LuaValue.valueOf("one"), LuaValue.FALSE
            ).toboolean()
        )
        // appending is reserved for existing files, so the second call
        // accumulates instead of creating
        assertTrue(
            call(
                "writeToFile", LuaValue.valueOf(first.toString()),
                LuaValue.valueOf("two"), LuaValue.TRUE
            ).toboolean()
        )
        val second = directory.resolve("second.txt")
        val lines = LuaValue.tableOf().also {
            it.set(1, LuaValue.valueOf("x"))
            it.set(2, LuaValue.valueOf("y"))
        }
        assertTrue(
            call(
                "writeToFile", LuaValue.valueOf(second.toString()),
                lines, LuaValue.FALSE
            ).toboolean()
        )
        val readBack = call("readFromFile", LuaValue.valueOf(second.toString()))
        assertEquals(2, readBack.length())
        assertEquals("x", readBack.get(1).tojstring())
        // missing files fall back to an empty table, the probe semantics
        val missing = call("readFromFile", LuaValue.valueOf("$first.missing"))
        assertEquals(0, missing.length())
    }

    @Test
    fun `the path helpers accept the handle`() {
        val directory = Files.createTempDirectory("arara-lua-handles")
        val file = directory.resolve("handled.txt")
        // the handle from the path helper feeds the whole family: the
        // doctrine's token round-trips through the writers, the
        // readers and the name extractors alike
        val handle = call("toFile", LuaValue.valueOf(file.toString()))
        assertTrue(
            call(
                "writeToFile", handle,
                LuaValue.valueOf("handled"), LuaValue.FALSE
            ).toboolean()
        )
        val readBack = call("readFromFile", handle)
        assertEquals(1, readBack.length())
        assertEquals("handled", readBack.get(1).tojstring())
        assertEquals(
            "handled",
            call("getBasename", handle).tojstring()
        )
        assertEquals(
            "txt",
            call("getFiletype", handle).tojstring(),
            "the file type extracts from the handle"
        )
    }

    @Test
    fun `binds the file listings`() {
        val tree = Files.createTempDirectory("arara-lua-listing")
        Files.writeString(tree.resolve("a.tex"), "a")
        Files.writeString(tree.resolve("b.log"), "b")
        Files.createDirectories(tree.resolve("nested"))
        Files.writeString(tree.resolve("nested/c.tex"), "c")
        val extensions = LuaValue.tableOf().also { it.set(1, LuaValue.valueOf("tex")) }
        val flat = call(
            "listFilesByExtensions", LuaValue.valueOf(tree.toString()),
            extensions, LuaValue.FALSE
        )
        assertEquals(1, flat.length())
        assertTrue(flat.get(1).tojstring().endsWith("a.tex"))
        val deep = call(
            "listFilesByExtensions", LuaValue.valueOf(tree.toString()),
            extensions, LuaValue.TRUE
        )
        assertEquals(2, deep.length())
        val patterns = LuaValue.tableOf().also { it.set(1, LuaValue.valueOf("*.tex")) }
        val byPattern = call(
            "listFilesByPatterns", LuaValue.valueOf(tree.toString()),
            patterns, LuaValue.TRUE
        )
        assertEquals(2, byPattern.length())
    }

    @Test
    fun `binds the directory operations`() {
        val directory = Files.createTempDirectory("arara-lua-directories")
        val nested = directory.resolve("nested/deep")
        assertTrue(call("createDirectories", LuaValue.valueOf(nested.toString())).toboolean())
        assertTrue(Files.exists(nested))
        assertTrue(call("removeDirectory", LuaValue.valueOf(nested.toString())).toboolean())
        assertFalse(Files.exists(nested))
        assertTrue(
            call("normalizePath", LuaValue.valueOf("a/../b.txt")).tojstring()
                .endsWith("b.txt")
        )
    }

    @Test
    fun `exposes the session operations`() {
        val result = globals.load(
            """
            local session = getSession()
            session.put('arara-lua-binding-test', 'quack')
            local contains = session.contains('arara-lua-binding-test')
            local value = session.get('arara-lua-binding-test')
            session.remove('arara-lua-binding-test')
            return value .. '/' .. tostring(contains)
            """.trimIndent()
        ).call().tojstring()
        assertEquals("quack/true", result)
        assertFalse(Session.contains("arara-lua-binding-test"))
    }

    @Test
    fun `the executor state feeds the stateful bindings`() {
        val outside = Files.createTempDirectory("arara-lua-outside")
        executeWithinProject(listOf("main.tex" to 0)) { directory, file ->
            val nested = Files.createDirectories(directory.resolve("nested"))
            assertEquals("main.tex", evaluate("getOriginalFile()").tojstring())
            assertTrue(
                evaluate("getOriginalReference()").tojstring().endsWith("main.tex")
            )
            call("halt", LuaValue.valueOf(7))
            assertEquals(7, Session.get(LinearExecutor.haltKeyFor(file)))
            call("halt")
            assertEquals(0, Session.get(LinearExecutor.haltKeyFor(file)))
            assertTrue(
                call("isSubdirectory", LuaValue.valueOf(nested.toString())).toboolean()
            )
            assertFalse(
                call("isSubdirectory", LuaValue.valueOf(outside.toString())).toboolean()
            )
            val command = call(
                "getCommand", LuaValue.valueOf("pdflatex"),
                LuaValue.valueOf("-opt")
            )
            val unwrapped = LuaConversion.toCommand(command)
            assertEquals(listOf("pdflatex", "-opt"), unwrapped?.elements)
            assertEquals(MPPPath(directory.toString()), unwrapped?.workingDirectory)
        }
    }

    @Test
    fun `executes system commands through the command bindings`() {
        val java = ProcessHandle.current().info().command().orElse("java")
        executeWithinProject(listOf("main.tex" to 0)) { directory, _ ->
            val command = call(
                "getCommand", LuaValue.valueOf(java), LuaValue.valueOf("-version")
            )
            val completed = call("unsafelyExecuteSystemCommand", command)
            assertEquals(0, completed.get("first").toint())
            val failing = call(
                "getCommandWithWorkingDirectory", LuaValue.valueOf(directory.toString()),
                LuaValue.valueOf("arara-not-a-real-command-quack")
            )
            val failed = call("unsafelyExecuteSystemCommand", failing)
            assertEquals(Environment.ERROR_EXIT_STATUS, failed.get("first").toint())
        }
    }

    @Test
    fun `rejects system commands in safe mode`() {
        val original = LinearExecutor.executionOptions
        try {
            LinearExecutor.executionOptions =
                ExecutionOptions.from(original).copy(executionMode = ExecutionMode.SAFE_RUN)
            val directory = Files.createTempDirectory("arara-lua-saferun")
            val command = call(
                "getCommandWithWorkingDirectory",
                LuaValue.valueOf(directory.toString()), LuaValue.valueOf("pdflatex")
            )
            assertFailsWith<AraraException> {
                call("unsafelyExecuteSystemCommand", command)
            }
        } finally {
            LinearExecutor.executionOptions = original
        }
    }

    /**
     * A renderer that records the message conversations and throws
     * on every other dialog, standing in for whatever the host wired.
     */
    private class MessageRecorder : DialogRenderer {
        val calls = mutableListOf<String>()
        override fun showMessage(type: Int, title: String, text: String) {
            calls += "showMessage:$type:$title:$text"
        }
        override fun showOptions(
            type: Int,
            title: String,
            text: String,
            buttons: Array<out Any>
        ): Int = error("no options dialog in this pin")
        override fun showInput(
            type: Int,
            title: String,
            text: String
        ): String = error("no input dialog in this pin")
        override fun showDropdown(
            type: Int,
            title: String,
            text: String,
            elements: Array<out Any>
        ): Int = error("no dropdown in this pin")
    }

    @Test
    fun `the message helper reaches the wired renderer`() {
        val original = TerminalDialogs.renderer
        val recorder = MessageRecorder()
        TerminalDialogs.renderer = recorder
        try {
            call(
                "show_message",
                LuaValue.valueOf(1),
                LuaValue.valueOf("Title"),
                LuaValue.valueOf("Text")
            )
            assertEquals(
                "showMessage:1:Title:Text",
                recorder.calls.single(),
                "the message dialog reaches the wired renderer"
            )
        } finally {
            TerminalDialogs.renderer = original
        }
    }
}
