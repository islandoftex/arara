// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.luaj.vm2.Globals
import org.luaj.vm2.lib.common.CommonPlatform

internal object LuaConditionalEnvironment {
    fun newGlobals(): Globals = CommonPlatform.standardGlobals().also(::bindConditionalHelpers)

    internal fun bindConditionalHelpers(globals: Globals) {
        LuaConditionalFiles.bind(globals)
        LuaConditionalReferences.bind(globals)
        LuaConditionalDialogs.bind(globals)
        LuaConditionalLoading.bind(globals)
    }
}
