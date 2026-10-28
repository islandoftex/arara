// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaArguments.stringListArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.islandoftex.arara.lua.rules.LuaConversion.toLua
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

/** Register string helpers without changing their script names. */
internal object LuaStringBindings {
    fun bind(globals: Globals) {
        bindBuildString(globals)
        bindTrimSpaces(globals)
        bindReplicatePattern(globals)
    }

    private fun bindBuildString(globals: Globals) {
        bindSpelled(
            globals, "buildString"
        ) { _, args ->
            LuaValue.valueOf(
                org.islandoftex.arara.core.rules.StringRuleHelpers.buildString(args.kotlinArgs())
            )
        }
    }

    private fun bindTrimSpaces(globals: Globals) {
        bindSpelled(
            globals, "trimSpaces"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.trimSpaces(
                    args.arg(1).stringArgument(name, 1)
                )
            )
        }
    }

    private fun bindReplicatePattern(globals: Globals) {
        bindSpelled(
            globals, "replicatePattern"
        ) { name, args ->
            toLua(
                RuleMethods.replicatePattern(
                    args.arg(1).stringArgument(name, 1),
                    args.arg(LuaArgumentIndex.SECOND).stringListArgument(name, LuaArgumentIndex.SECOND)
                )
            )
        }
    }
}
