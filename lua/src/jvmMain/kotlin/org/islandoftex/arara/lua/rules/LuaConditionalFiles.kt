// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

internal object LuaConditionalFiles {
    fun bind(globals: Globals) {
        mapOf<String, Pair<(MPPPath) -> Boolean, (String) -> Boolean>>(
            "exists" to ({ path: MPPPath -> ConditionalMethods.exists(path) } to
                { extension: String -> ConditionalMethods.exists(extension) }),
            "missing" to ({ path: MPPPath -> ConditionalMethods.missing(path) } to
                { extension: String -> ConditionalMethods.missing(extension) }),
            "changed" to ({ path: MPPPath -> ConditionalMethods.changed(path) } to
                { extension: String -> ConditionalMethods.changed(extension) }),
            "unchanged" to ({ path: MPPPath -> ConditionalMethods.unchanged(path) } to
                { extension: String -> ConditionalMethods.unchanged(extension) })
        ).forEach { (name, checks) ->
            bindSpelled(globals, name) { bound, args ->
                LuaValue.valueOf(args.fileArgument(1, bound).fold(checks.first, checks.second))
            }
        }
        bindSpelled(globals, "found") { name, args ->
            val reference = args.fileArgument(1, name)
            val needle = args.arg(LuaArgumentIndex.SECOND).stringArgument(name, LuaArgumentIndex.SECOND)
            LuaValue.valueOf(reference.fold(
                { ConditionalMethods.found(it, needle) }, { ConditionalMethods.found(it, needle) }
            ))
        }
    }

}
