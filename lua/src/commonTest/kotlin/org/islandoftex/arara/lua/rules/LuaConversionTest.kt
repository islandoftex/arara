// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.Command
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.common.CommonPlatform

class LuaConversionTest {
    @Test
    fun scalarValuesRoundTripWithoutCoercingStringsOrBooleans() {
        for (value in listOf(null, "text", "42", true, false, -1, 0, 42, 1.5, 10000000000000L)) {
            assertEquals(value, LuaConversion.fromLua(LuaConversion.toLua(value)))
        }
        assertEquals(1.5, LuaConversion.fromLua(LuaConversion.toLua(1.5f)))
        val original = LuaValue.valueOf("already Lua")
        assertSame(original, LuaConversion.toLua(original))
    }

    @Test
    fun integerConversionsDoNotTruncateOverflowOrLosePrecision() {
        for (value in listOf(Int.MIN_VALUE.toLong() - 1, Int.MAX_VALUE.toLong() + 1, Long.MIN_VALUE)) {
            assertEquals(value, LuaConversion.fromLua(LuaConversion.toLua(value)))
        }
        for (value in listOf(Long.MAX_VALUE, 9007199254740993L)) {
            assertFailsWith<LuaHelperException> { LuaConversion.toLua(value) }
        }
        for (value in listOf(9223372036854775808.0, 1e100, Double.POSITIVE_INFINITY)) {
            assertEquals(value, LuaConversion.fromLua(LuaValue.valueOf(value)))
        }
    }

    @Test
    fun pathsCommandsAndForeignObjectsKeepTheirIdentity() {
        val path = MPPPath("book.tex")
        val command = Command(listOf("pdflatex", "book.tex"), path.parent)
        for (value in listOf(path, command, Any())) {
            val wrapped = LuaConversion.toLua(value)
            assertTrue(wrapped.isuserdata())
            assertSame(value, LuaConversion.fromLua(wrapped))
        }
        assertSame(command, LuaValues.toCommand(LuaConversion.toLua(command)))
        assertEquals(null, LuaValues.toCommand(LuaConversion.toLua(path)))
    }

    @Test
    fun sequencesUseTheirIndicesAndMapsKeepTheirStringKeys() {
        val table = LuaTable()
        table.set(3, LuaValue.valueOf("third"))
        table.set(1, LuaValue.valueOf("first"))
        table.set(2, LuaValue.valueOf("second"))
        assertEquals(listOf("first", "second", "third"), LuaConversion.fromLua(table))
        val nested = mapOf("name" to "Book", "parts" to listOf("one", "two"))
        assertEquals(nested, LuaConversion.fromLua(LuaConversion.toLua(nested)))
        assertEquals(emptyList<Any>(), LuaConversion.fromLua(LuaTable()))
        val sparse = LuaTable().apply { set(2, LuaValue.valueOf("second")) }
        assertEquals(mapOf("2" to "second"), LuaConversion.fromLua(sparse))
    }

    @Test
    fun keyCollisionsAreReportedInsteadOfOverwritingValues() {
        assertFailsWith<LuaHelperException> { LuaConversion.toLua(mapOf(1 to "number", "1" to "string")) }
        val table = LuaTable().apply {
            set(1, LuaValue.valueOf("number"))
            set("1", LuaValue.valueOf("string"))
        }
        assertFailsWith<LuaHelperException> { LuaConversion.fromLua(table) }
    }

    @Test
    fun cyclesFailWithoutRejectingSharedNoncyclicValues() {
        val cycle = LuaTable().apply { set("self", this) }
        assertFailsWith<LuaHelperException> { LuaConversion.fromLua(cycle) }
        assertFailsWith<LuaHelperException> { LuaResults.commandResultToList(cycle) }
        val list = mutableListOf<Any>()
        list.add(list)
        assertFailsWith<LuaHelperException> { LuaConversion.toLua(list) }
        val shared = listOf("shared")
        assertEquals(listOf(shared, shared), LuaConversion.fromLua(LuaConversion.toLua(listOf(shared, shared))))
    }

    @Test
    fun commandAndFlagResultsPreserveOrderAndOpaqueHandles() {
        val command = Command(listOf("pdflatex"))
        val values = LuaConversion.toLua(listOf(false, listOf(command, true)))
        assertEquals(listOf(false, command, true), LuaResults.commandResultToList(values))
        assertEquals(listOf("--draft", "--synctex=1"), LuaResults.flagResultToList(
            LuaConversion.toLua(listOf("--draft", listOf("--synctex=1")))
        ))
        assertEquals(emptyList(), LuaResults.flagResultToList(LuaValue.NIL))
        assertEquals(emptyList(), LuaResults.commandResultToList(LuaValue.NIL))
    }

    @Test
    fun referencesExposeLuaDataAndFunctionFallbackRemainsAString() {
        val path = MPPPath("./folder//../book.tex")
        val reference = LuaValues.referenceToLua(path)
        assertEquals("book.tex", reference["file_name"].tojstring())
        assertEquals(path.toString(), reference["path"].tojstring())
        assertEquals(path.parent.toString(), reference["parent"].tojstring())
        assertEquals("./folder//../book.tex", reference["supplied_spelling"].tojstring())
        val function = CommonPlatform.standardGlobals().load("return function() end").call()
        assertTrue(LuaConversion.fromLua(function) is String)
    }
}
