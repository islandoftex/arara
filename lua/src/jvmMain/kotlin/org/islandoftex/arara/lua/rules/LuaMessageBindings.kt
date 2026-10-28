// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaArguments.numberArgument
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

/** Register message helpers without changing their script names. */
internal object LuaMessageBindings {
    fun bind(globals: Globals) {
        bindShowMessage(globals)
    }

    private fun bindShowMessage(globals: Globals) {
        bindSpelled(
            globals, "showMessage"
        ) { name, args ->
            RuleMethods.showMessage(
                type = args.arg(1).numberArgument(name, 1),
                title = args.arg(LuaArgumentIndex.SECOND).stringArgument(name, LuaArgumentIndex.SECOND),
                text = args.arg(LuaArgumentIndex.THIRD).stringArgument(name, LuaArgumentIndex.THIRD)
            )
            LuaValue.NIL
        }
    }
}
