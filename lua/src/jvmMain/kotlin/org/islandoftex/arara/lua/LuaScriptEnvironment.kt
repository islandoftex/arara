// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.Environment
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.common.CommonPlatform

/** Fresh Lua globals for configuration scripts, without rule helpers or shared state. */
internal object LuaScriptEnvironment {
    fun newGlobals(workingDirectory: MPPPath): Globals = CommonPlatform.standardGlobals().apply {
        set("cwd", LuaValue.valueOf(workingDirectory.normalize().toString()))
        set("user", LuaTable().apply {
            set("home", LuaValue.valueOf(Environment.getSystemPropertyOrNull("user.home") ?: ""))
            set("name", LuaValue.valueOf(Environment.getSystemPropertyOrNull("user.name") ?: ""))
        })
    }
}
