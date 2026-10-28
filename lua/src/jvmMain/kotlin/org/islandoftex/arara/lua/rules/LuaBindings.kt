// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.rules.RuleMethods
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs

internal object LuaBindings {
    fun sessionTable(): LuaValue = LuaSession.table(RuleMethods.getSession())

    fun bindSpelled(globals: Globals, name: String, body: (String, Varargs) -> LuaValue) {
        val snake = name.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()
        setOf(name, snake).forEach { spelling ->
            globals.set(spelling, LuaValues.luaFunction { body(spelling, it) })
        }
    }
}
