// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Session
import org.islandoftex.arara.core.session.Command
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.common.CommonPlatform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LuaSessionTest {
    private class MemorySession : Session {
        val values = mutableMapOf<String, Any>()
        override fun get(key: String): Any = values.getValue(key)
        override fun put(key: String, value: Any) { values[key] = value }
        override fun remove(key: String) { values.remove(key) }
        override fun contains(key: String): Boolean = key in values
        override fun clear() = values.clear()
    }

    @Test
    fun luaCallbacksPreserveSessionValuesAndOpaqueHandles() {
        val session = MemorySession()
        val path = MPPPath("book.tex")
        val command = Command(listOf("pdflatex"))
        val globals = CommonPlatform.standardGlobals()
        globals.set("session", LuaSession.table(session))
        globals.set("path", LuaConversion.toLua(path))
        globals.set("command", LuaConversion.toLua(command))
        globals.load("""
            session.put('path', path)
            session.put('command', command)
            session.put('flags', { '--draft', '--synctex=1' })
            assert(session.contains('path'))
            assert(session.get('path') == path)
            assert(session.get('flags')[2] == '--synctex=1')
            session.put('state', { mode = 'draft', enabled = false })
            assert(session.get('state').mode == 'draft')
            assert(session.get('state').enabled == false)
            session.remove('flags')
            assert(not session.contains('flags'))
        """.trimIndent()).call()
        assertSame(path, session["path"])
        assertSame(command, session["command"])
        assertSame(command, LuaConversion.fromLua(globals.get("session").get("get").call(LuaValue.valueOf("command"))))
        globals.load("session.clear(); assert(not session.contains('path'))").call()
        assertTrue(session.values.isEmpty())
    }

    @Test
    fun sessionArgumentErrorsAreWrappedWithTheirHelperDetails() {
        val globals = CommonPlatform.standardGlobals()
        globals.set("session", LuaSession.table(MemorySession()))
        for (script in listOf("session.put('nil', nil)", "session.get(1)")) {
            val error = assertFailsWith<AraraException> {
                LuaConversion.withLuaErrorHandling("rule context") { globals.load(script).call() }
            }
            assertEquals("rule context", error.message)
            assertTrue(error.exception is LuaHelperException)
            assertTrue(error.exception!!.message.orEmpty().contains("session."))
        }
    }

    @Test
    fun luakWrappedErrorsKeepHelperAndAuthoredFailuresDistinct() {
        val helper = LuaHelperException("helper sentinel")
        val converted = assertFailsWith<AraraException> {
            LuaConversion.withLuaErrorHandling("context") { throw LuaError(LuaError(helper)) }
        }
        assertSame(helper, converted.exception)
        val authored = AraraExceptionWithHeader("authored sentinel")
        val preserved = assertFailsWith<AraraExceptionWithHeader> {
            LuaConversion.withLuaErrorHandling("context") { throw LuaError(authored) }
        }
        assertSame(authored, preserved)
        val direct = assertFailsWith<AraraExceptionWithHeader> {
            LuaConversion.withLuaErrorHandling("context") { throw authored }
        }
        assertSame(authored, direct)
        val globals = CommonPlatform.standardGlobals()
        globals.set("fail", LuaConversion.luaFunction { throw authored })
        val callback = assertFailsWith<AraraExceptionWithHeader> {
            LuaConversion.withLuaErrorHandling("context") { globals.load("fail()").call() }
        }
        assertSame(authored, callback)
    }

    @Test
    fun ordinaryLuaErrorsCarryTheirSourceAsTheUnderlyingFailure() {
        for ((script, detail) in listOf("error('sentinel')" to "sentinel", "return {" to "probe.lua")) {
            val error = assertFailsWith<AraraException> {
                LuaConversion.withLuaErrorHandling("rule context") {
                    CommonPlatform.standardGlobals().load(script, "probe.lua").call()
                }
            }
            assertEquals("rule context", error.message)
            assertTrue(error.exception!!.cause is LuaError)
            assertTrue(error.exception!!.message.orEmpty().contains("probe.lua"))
            assertTrue(error.exception!!.message.orEmpty().contains(detail))
        }
    }
}
