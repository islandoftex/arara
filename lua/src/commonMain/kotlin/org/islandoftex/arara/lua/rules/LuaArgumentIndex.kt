// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.lua.rules.LuaConversion.fromLua
import org.luaj.vm2.Varargs

/** Script helper signatures use one-based argument positions. */
internal object LuaArgumentIndex {
    const val FIRST = 1
    const val SECOND = 2
    const val THIRD = 3
    const val FOURTH = 4
}

internal fun Varargs.kotlinArgs(from: Int = LuaArgumentIndex.FIRST): List<Any?> =
    (from..narg()).map { fromLua(arg(it)) }
