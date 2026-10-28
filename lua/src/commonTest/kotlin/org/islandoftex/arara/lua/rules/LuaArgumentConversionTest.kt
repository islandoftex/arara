// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.lua.rules.LuaArguments.booleanArgument
import org.islandoftex.arara.lua.rules.LuaArguments.numberArgument
import org.islandoftex.arara.lua.rules.LuaArguments.pathArgument
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaArguments.stringListArgument
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

class LuaArgumentConversionTest {
    @Test
    fun typedArgumentsDoNotApplyLuasImplicitCoercions() {
        assertEquals("1", LuaValue.valueOf("1").stringArgument("probe", 1))
        assertEquals(false, LuaValue.valueOf(false).booleanArgument("probe", 1))
        assertEquals(1, LuaValue.valueOf(1).numberArgument("probe", 1))
        assertFailsWith<LuaHelperException> { LuaValue.valueOf(1).stringArgument("probe", 1) }
        assertFailsWith<LuaHelperException> { LuaValue.valueOf("1").numberArgument("probe", 1) }
        for (value in listOf(LuaValue.valueOf("true"), LuaValue.valueOf(0), LuaValue.NIL)) {
            assertFailsWith<LuaHelperException> { value.booleanArgument("probe", 1) }
        }
        for (number in listOf(1.5, 2147483648.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertFailsWith<LuaHelperException> { LuaValue.valueOf(number).numberArgument("probe", 1) }
        }
    }

    @Test
    fun pathsAcceptStringsAndPathHandlesButRejectOtherUserdata() {
        val path = MPPPath("book.tex")
        assertEquals(path, LuaValue.valueOf("book.tex").pathArgument("probe", 1))
        assertSame(path, LuaConversion.toLua(path).pathArgument("probe", 1))
        for (value in listOf(LuaConversion.toLua(Any()), LuaValue.valueOf(42), LuaValue.NIL)) {
            assertFailsWith<LuaHelperException> { value.pathArgument("probe", 1) }
        }
    }

    @Test
    fun stringListsMustBeContiguousAndContainOnlyRealStrings() {
        assertEquals(listOf("a", "b"), LuaConversion.toLua(listOf("a", "b")).stringListArgument("probe", 1))
        val invalid = listOf(
            LuaValue.valueOf("a"), LuaConversion.toLua(listOf("a", 2)),
            LuaTable().apply { set(2, LuaValue.valueOf("a")) }, LuaConversion.toLua(mapOf("key" to "a"))
        )
        for (value in invalid) {
            val error = assertFailsWith<LuaHelperException> { value.stringListArgument("probe", 2) }
            assertTrue(error.message.orEmpty().contains("#2 to 'probe'"))
        }
    }

    @Test
    fun exitPredicatesMustReturnBooleansRatherThanTruthyValues() {
        assertEquals(false, LuaResults.booleanResult(LuaValue.valueOf(false), "exit"))
        assertEquals(true, LuaResults.booleanResult(LuaValue.valueOf(true), "exit"))
        for (value in listOf(LuaValue.valueOf(0), LuaValue.valueOf("true"), LuaValue.NIL)) {
            val error = assertFailsWith<AraraException> { LuaResults.booleanResult(value, "exit") }
            assertTrue(error.exception is LuaHelperException)
            assertTrue(error.exception!!.message.orEmpty().contains("exit"))
        }
    }
}
