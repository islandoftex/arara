// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaValue
import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Verifies the typed argument contract of the conditional helpers:
 * arguments have to match the declared type — Lua's implicit coercions
 * apply to operators, not to arara's library functions, and mismatches
 * are reported as Lua-style bad argument errors.
 */
class LuaConditionalEnvironmentArgumentTest {
    private fun binding(name: String): LuaValue =
        LuaConditionalEnvironment.newGlobals().get(name)

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
    fun `rejects numbers where a file or extension is expected`() {
        assertBadArgument("exists", 1, "file or string") {
            binding("exists").call(LuaValue.valueOf(123))
        }
        assertBadArgument("toFile", 1, "string") {
            binding("toFile").call(LuaValue.valueOf(123))
        }
    }

    @Test
    fun `rejects booleans where a file or extension is expected`() {
        assertBadArgument("exists", 1, "file or string") {
            binding("exists").call(LuaValue.TRUE)
        }
    }

    @Test
    fun `rejects non strings where strings are expected`() {
        assertBadArgument("found", 2, "string") {
            binding("found").call(LuaValue.valueOf("log"), LuaValue.TRUE)
        }
        assertBadArgument("session.put", 2, "value") {
            binding("getSession").call().checktable()
                .get("put")
                .invoke(
                    LuaValue.varargsOf(
                        arrayOf(LuaValue.valueOf("k"), LuaValue.NIL)
                    )
                )
        }
    }

    @Test
    fun `rejects strings where numbers are expected`() {
        assertBadArgument("showOptions", 1, "number") {
            binding("showOptions").invoke(
                LuaValue.varargsOf(
                    arrayOf(
                        LuaValue.valueOf("400"),
                        LuaValue.valueOf(1),
                        LuaValue.valueOf("title"),
                        LuaValue.valueOf("text")
                    )
                )
            )
        }
    }

    @Test
    fun `accepts real strings and file references`() {
        val globals = LuaConditionalEnvironment.newGlobals()
        val file = kotlin.io.path.createTempFile("arara", ".tex")
        try {
            assertEquals(
                file.fileName.toString().substringBeforeLast("."),
                globals.load("return getBasename(toFile('$file'))")
                    .call()
                    .tojstring(),
                "a file reference from toFile flows through the helpers"
            )
            val stored = globals
                .load(
                    "local s = getSession(); " +
                        "s.put('k', 'v'); return s.get('k')"
                )
                .call()
            assertEquals(
                "v",
                stored.tojstring(),
                "the session round-trips real strings"
            )
        } finally {
            file.toFile().delete()
            globals.load("getSession().remove('k')").call()
        }
    }

    @Test
    fun `round-trips files through the session as file references`() {
        val globals = LuaConditionalEnvironment.newGlobals()
        val file = kotlin.io.path.createTempFile("arara", ".tex")
        try {
            val result = globals
                .load(
                    "local s = getSession(); " +
                        "s.put('f', toFile('$file')); " +
                        "local f = s.get('f'); " +
                        "return type(f) .. ' ' .. tostring(exists(f))"
                )
                .call()
                .tojstring()
            assertEquals(
                "userdata true",
                result,
                "a stored file comes back as a file reference and " +
                    "exists checks the file, not an extension"
            )
        } finally {
            file.toFile().delete()
            globals.load("getSession().remove('f')").call()
        }
    }
}
