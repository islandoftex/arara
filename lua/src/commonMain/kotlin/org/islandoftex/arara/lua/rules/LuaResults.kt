// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.lua.rules.LuaConversion.fromLua
import org.islandoftex.arara.lua.rules.LuaConversion.sequence
import org.islandoftex.arara.lua.rules.LuaConversion.within
import org.luaj.vm2.LuaBoolean
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

internal object LuaResults {
    fun booleanResult(value: LuaValue, source: String): Boolean =
        (value as? LuaBoolean)?.toboolean() ?: throw AraraException(
            LanguageController.messages.errorInterpreterWrongExitClosureReturn,
            LuaHelperException(
                LanguageController.messages.errorLuaBooleanResult(source, value.typename())
            )
        )

    fun flagResultToList(value: LuaValue): List<String> = when {
        value.isnil() -> emptyList()
        value is LuaTable -> flatten(value, mutableListOf()).map { it.toString() }
        else -> listOf(value.tojstring())
    }

    fun commandResultToList(value: LuaValue): List<Any> = when {
        value.isnil() -> emptyList()
        value is LuaTable -> flatten(value, mutableListOf())
        else -> listOfNotNull(fromLua(value))
    }

    private fun flatten(table: LuaTable, visiting: MutableList<Any>): List<Any> = within(table, visiting) {
        val values = sequence(table) ?: table.keys().map { table[it] }
        values.flatMap { value ->
            if (value is LuaTable) flatten(value, visiting) else listOfNotNull(fromLua(value))
        }
    }

}
