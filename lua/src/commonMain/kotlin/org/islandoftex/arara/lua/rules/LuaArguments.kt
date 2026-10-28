// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.lua.rules.LuaConversion.sequence
import org.luaj.vm2.LuaBoolean
import org.luaj.vm2.LuaNumber
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

internal object LuaArguments {
    fun badArgument(helper: String, index: Int, expected: String, actual: LuaValue): LuaHelperException =
        LuaHelperException(
            LanguageController.messages.errorLuaBadArgument(index, helper, expected, actual.typename())
        )

    fun LuaValue.stringArgument(helper: String, index: Int): String =
        (this as? LuaString)?.tojstring() ?: throw badArgument(helper, index, "string", this)

    fun LuaValue.numberArgument(helper: String, index: Int): Int =
        (this as? LuaNumber)?.takeIf { it.isint() }?.toint()
            ?: throw badArgument(helper, index, "32-bit integer", this)

    fun LuaValue.booleanArgument(helper: String, index: Int): Boolean =
        (this as? LuaBoolean)?.toboolean() ?: throw badArgument(helper, index, "boolean", this)

    fun LuaValue.pathArgument(helper: String, index: Int): MPPPath = when {
        isuserdata() -> touserdata() as? MPPPath ?: throw badArgument(helper, index, "path", this)
        this is LuaString -> MPPPath(tojstring())
        else -> throw badArgument(helper, index, "path", this)
    }

    fun LuaValue.stringListArgument(helper: String, index: Int): List<String> {
        val values = (this as? LuaTable)?.let { sequence(it) }
            ?: throw badArgument(helper, index, "list of strings", this)
        return values.map {
            (it as? LuaString)?.tojstring() ?: throw badArgument(helper, index, "list of strings", this)
        }
    }

}
