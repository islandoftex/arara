// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Command
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction

internal object LuaValues {
    fun referenceToLua(path: MPPPath): LuaTable = LuaValue.tableOf().also { table ->
        table.set("file_name", LuaValue.valueOf(path.fileName))
        table.set("path", LuaValue.valueOf(path.toString()))
        table.set("parent", LuaValue.valueOf(path.parent.toString()))
    }

    fun luaFunction(body: (Varargs) -> LuaValue): LuaValue = object : VarArgFunction() {
        override fun invoke(args: Varargs): Varargs = body(args)
    }

    fun toCommand(value: LuaValue): Command? = value.takeIf { it.isuserdata() }?.touserdata() as? Command
}
