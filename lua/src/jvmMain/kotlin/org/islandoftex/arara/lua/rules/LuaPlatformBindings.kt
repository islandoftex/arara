// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

/** Register platform helpers without changing their script names. */
internal object LuaPlatformBindings {
    fun bind(globals: Globals) {
        bindIsVerboseMode(globals)
        bindIsOnPath(globals)
        bindOperatingSystems(globals)
    }

    private fun bindIsVerboseMode(globals: Globals) {
        bindSpelled(
            globals, "isVerboseMode"
        ) { _, _ ->
            LuaValue.valueOf(RuleMethods.isVerboseMode())
        }
    }

    private fun bindIsOnPath(globals: Globals) {
        bindSpelled(
            globals, "isOnPath"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.isOnPath(
                    args.arg(1).stringArgument(name, 1)
                )
            )
        }
    }

    private fun bindOperatingSystems(globals: Globals) {
        listOf(
            "isWindows" to { RuleMethods.isWindows() },
            "isLinux" to { RuleMethods.isLinux() },
            "isMac" to { RuleMethods.isMac() },
            "isUnix" to { RuleMethods.isUnix() },
            "isCygwin" to { RuleMethods.isCygwin() }
        ).forEach { (name, check) ->
            bindSpelled(globals, name) { _, args ->
                when (args.narg()) {
                    0 -> LuaValue.valueOf(check())
                    else -> if (check()) args.arg(1) else args.arg(LuaArgumentIndex.SECOND)
                }
            }
        }
    }
}
