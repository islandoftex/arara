// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.luaj.vm2.LuaValue

internal object ProjectFileFields {
    private val messages get() = LanguageController.messages

    fun requiredKey(value: LuaValue, context: String): String = ProjectFields.nonblank(
        ProjectFields.string(value, "$context key")
            ?: throw ProjectParseException(messages.errorScriptStringKeys(context)), "$context key"
    )

    fun validatePath(path: MPPPath, paths: MutableSet<MPPPath>, context: String, reference: String) {
        if (!paths.add(path)) throw ProjectParseException(messages.errorScriptDuplicate(context, path.toString()))
        if (path.isDirectory) throw ProjectParseException(messages.errorScriptDirectory("$context[$reference]"))
    }
}
