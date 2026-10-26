// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.BuildDirectory
import org.islandoftex.arara.api.files.BuildInput
import org.islandoftex.arara.api.files.BuildSelection
import org.islandoftex.arara.api.files.BuildSymlinks
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

internal object ProjectBuildFields {
    private val messages get() = LanguageController.messages

    fun parse(table: LuaTable, source: MPPPath, context: String): BuildDirectory {
        ProjectFields.keys(table, setOf("directory", "inputs", "mappings", "symlinks"), context)
        val directory = required(table, "directory", "$context.directory")
        val links = ProjectFields.string(table["symlinks"], "$context.symlinks") ?: "preserve"
        val mode = when (links) {
            "preserve" -> BuildSymlinks.PRESERVE
            "copy" -> BuildSymlinks.COPY
            else -> throw ProjectParseException(messages.errorScriptChoice("$context.symlinks", "preserve, copy"))
        }
        val mappings = ProjectFields.table(table["mappings"], "$context.mappings")?.let { entries ->
            ProjectFields.list(entries, "$context.mappings").mapIndexed { index, value ->
                mapping(value, source, "$context.mappings[${index + 1}]")
            }
        }.orEmpty().toMutableList()
        val inputs = ProjectFields.table(table["inputs"], "$context.inputs")?.let { entries ->
            ProjectFields.list(entries, "$context.inputs").mapIndexedNotNull { index, value ->
                val field = "$context.inputs[${index + 1}]"
                if (value is LuaString) {
                    selection(value.toString(), field)
                } else {
                    mappings += mapping(value, source, field)
                    null
                }
            }
        }
        return BuildDirectory(source.resolve(directory).normalize(), inputs, mappings, mode)
    }

    private fun selection(value: String, field: String): BuildSelection {
        if (value.isBlank()) throw ProjectParseException(messages.errorScriptNonblank(field))
        return if (value.any { it in "*?[" }) BuildSelection.Glob(value) else BuildSelection.Path(MPPPath(value))
    }

    private fun required(table: LuaTable, key: String, field: String): String =
        ProjectFields.string(table[key], field)?.takeIf { it.isNotBlank() }
            ?: throw ProjectParseException(messages.errorScriptNonblank(field))

    private fun mapping(value: LuaValue, source: MPPPath, context: String): BuildInput {
        val table = ProjectFields.table(value, context)
            ?: throw ProjectParseException(messages.errorScriptTable(context))
        ProjectFields.keys(table, setOf("source", "destination"), context)
        return BuildInput(source.resolve(required(table, "source", "$context.source")).normalize(),
            required(table, "destination", "$context.destination"))
    }
}
