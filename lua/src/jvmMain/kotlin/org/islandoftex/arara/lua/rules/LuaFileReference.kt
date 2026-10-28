// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.lua.rules.LuaArguments.badArgument
import org.luaj.vm2.LuaString
import org.luaj.vm2.Varargs

internal sealed interface LuaFileReference {
    fun <T> fold(path: (MPPPath) -> T, text: (String) -> T): T

    class Path(private val value: MPPPath) : LuaFileReference {
        override fun <T> fold(path: (MPPPath) -> T, text: (String) -> T): T = path(value)
    }

    class Text(private val value: String) : LuaFileReference {
        override fun <T> fold(path: (MPPPath) -> T, text: (String) -> T): T = text(value)
    }
}

internal fun Varargs.fileArgument(index: Int, helper: String): LuaFileReference {
    val value = arg(index)
    return when {
        value.isuserdata() -> (value.touserdata() as? MPPPath)?.let(LuaFileReference::Path)
            ?: throw badArgument(helper, index, "file or string", value)
        value is LuaString -> LuaFileReference.Text(value.tojstring())
        else -> throw badArgument(helper, index, "file or string", value)
    }
}
