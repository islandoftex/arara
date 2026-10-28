// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaArguments.numberArgument
import org.islandoftex.arara.lua.rules.LuaArguments.pathArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.islandoftex.arara.lua.rules.LuaBindings.sessionTable
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

/** Register reference helpers without changing their script names. */
internal object LuaReferenceBindings {
    fun bind(globals: Globals) {
        bindGetBasename(globals)
        bindGetFiletype(globals)
        bindGetOriginalFile(globals)
        bindGetOriginalReference(globals)
        bindHalt(globals)
        bindGetSession(globals)
    }

    private fun bindGetBasename(globals: Globals) {
        bindSpelled(
            globals, "getBasename"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.getBasename(
                    args.arg(1).pathArgument(name, 1)
                )
            )
        }
    }

    private fun bindGetFiletype(globals: Globals) {
        bindSpelled(
            globals, "getFiletype"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.getFiletype(
                    args.arg(1).pathArgument(name, 1)
                )
            )
        }
    }

    private fun bindGetOriginalFile(globals: Globals) {
        bindSpelled(
            globals, "getOriginalFile"
        ) { _, _ ->
            LuaValue.valueOf(RuleMethods.originalFile)
        }
    }

    private fun bindGetOriginalReference(globals: Globals) {
        bindSpelled(
            globals, "getOriginalReference"
        ) { _, _ ->
            LuaValue.valueOf(RuleMethods.originalReference.toString())
        }
    }

    private fun bindHalt(globals: Globals) {
        bindSpelled(
            globals, "halt"
        ) { name, args ->
            RuleMethods.halt(
                args.arg(1).takeUnless { it.isnil() }
                    ?.numberArgument(name, 1) ?: 0
            )
            LuaValue.NIL
        }
        // the session is exposed as a table of operations (the very same
        // shape as the directive conditional environment), as Lua cannot
        // call methods on the underlying object directly
    }

    private fun bindGetSession(globals: Globals) {
        bindSpelled(
            globals, "getSession"
        ) { _, _ -> sessionTable() }
    }
}
