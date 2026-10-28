// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectParseException
import org.islandoftex.arara.api.files.WatchOptions
import org.islandoftex.arara.core.localization.LanguageController
import org.luaj.vm2.LuaTable

internal object ProjectWatchFields {
    fun parse(project: LuaTable, directory: MPPPath, context: String): WatchOptions {
        val field = "$context.watch"
        val table = ProjectFields.table(project["watch"], field) ?: return WatchOptions()
        ProjectFields.keys(table, setOf("paths", "include", "exclude"), field)
        fun strings(key: String): List<String> = ProjectFields.table(table[key], "$field.$key")?.let {
            ProjectFields.strings(it, "$field.$key").onEach { value ->
                if (value.isBlank()) throw ProjectParseException(
                    LanguageController.messages.errorScriptNonblank("$field.$key"))
            }
        }.orEmpty()
        return WatchOptions(strings("paths").map { directory.resolve(it).normalize() },
            strings("include"), strings("exclude"))
    }
}
