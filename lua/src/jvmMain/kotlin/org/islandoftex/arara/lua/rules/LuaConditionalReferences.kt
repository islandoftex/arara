// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.islandoftex.arara.lua.rules.LuaBindings.sessionTable
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

internal object LuaConditionalReferences {
    fun bind(globals: Globals) {
        bindSpelled(
            globals, "toFile"
        ) { name, args ->
            LuaConversion.toLua(
                ConditionalMethods.toFile(
                    args.arg(1).stringArgument(name, 1)
                )
            )
        }
        bindSpelled(
            globals, "currentFile"
        ) { _, _ ->
            LuaConversion.toLua(ConditionalMethods.currentFile())
        }
        bindSpelled(
            globals, "getOriginalReference"
        ) { _, _ ->
            LuaConversion.toLua(RuleMethods.originalReference)
        }
        bindSpelled(
            globals, "getBasename"
        ) { name, args ->
            val reference = args.fileArgument(1, name)
            reference.fold(RuleMethods::getBasename, RuleMethods::getBasename).let { LuaValue.valueOf(it) }
        }

        // the session is exposed as a table of operations, as Lua cannot
        // call methods on the underlying object directly
        bindSpelled(
            globals, "getSession"
        ) { _, _ -> sessionTable() }

    }

}
