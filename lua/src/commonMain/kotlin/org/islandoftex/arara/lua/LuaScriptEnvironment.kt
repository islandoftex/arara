// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.MPPPath
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.common.CommonPlatform

/** Isolated globals shared by configuration and project scripts; `cwd` belongs to the caller. */
internal object LuaScriptEnvironment {
    fun newGlobals(workingDirectory: MPPPath): Globals = CommonPlatform.standardGlobals().apply {
        set("cwd", LuaValue.valueOf(workingDirectory.normalize().toString()))
        set("user", LuaTable().apply {
            scriptUserProperties().forEach { (name, value) -> set(name, LuaValue.valueOf(value)) }
        })
    }
}

/** Platform properties exposed as plain Lua strings, without engine-specific rule helpers. */
internal expect fun scriptUserProperties(): Map<String, String>
