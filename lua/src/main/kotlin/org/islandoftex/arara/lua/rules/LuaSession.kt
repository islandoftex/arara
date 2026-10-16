// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.session.Session
import org.islandoftex.arara.lua.rules.LuaConversion.luaFunction
import org.islandoftex.arara.lua.rules.LuaConversion.stringArgument
import org.luaj.vm2.LuaValue

/** Session operations use Lua values while preserving opaque runtime handles. */
internal object LuaSession {
    fun table(session: Session): LuaValue = LuaValue.tableOf().also { table ->
        table.set("get", luaFunction { args ->
            LuaConversion.toLua(session[args.arg(1).stringArgument("session.get", 1)])
        })
        table.set("put", luaFunction { args ->
            val key = args.arg(1).stringArgument("session.put", 1)
            val value = LuaConversion.fromLua(args.arg(2))
                ?: throw LuaConversion.badArgument("session.put", 2, "value", args.arg(2))
            session.put(key, value)
            LuaValue.NIL
        })
        table.set("contains", luaFunction { args ->
            LuaValue.valueOf(args.arg(1).stringArgument("session.contains", 1) in session)
        })
        table.set("remove", luaFunction { args ->
            session.remove(args.arg(1).stringArgument("session.remove", 1))
            LuaValue.NIL
        })
        table.set("clear", luaFunction {
            session.clear()
            LuaValue.NIL
        })
    }
}
