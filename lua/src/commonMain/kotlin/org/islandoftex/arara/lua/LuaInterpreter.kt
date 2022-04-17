// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.files.UNKNOWN_TYPE
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaNumber
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.common.CommonPlatform

object LuaInterpreter {
    /**
     * Dump the lua value to a string. Useful for debugging.
     */
    private fun dumpLuaValue(value: LuaValue): String {
        return if (value is LuaTable) {
            "{${
            value.keys().joinToString(", ") { key ->
                val k = if (key is LuaNumber) {
                    key.toString()
                } else {
                    "\"${key}\""
                }
                "[$k] = ${dumpLuaValue(value[key])}"
            }
            }} "
        } else {
            value.toString()
        }
    }

    private fun extractFileType(fileType: LuaTable): FileType {
        val extension = requireNotNull(fileType["extension"].takeIf { it is LuaString }?.toString()) {
            "An extension is required to identify the file type."
        }
        val pattern = requireNotNull(fileType["extension"].takeIf { it is LuaString }?.toString()) {
            // TODO: follow documentation and only require it for non-default filetypes
            "A pattern is missing (unknown and not in the default file types)."
        }

        return org.islandoftex.arara.core.files.FileType(extension, pattern)
    }
    private fun extractProjectFile(path: String, table: LuaTable): ProjectFile {
        val fileType = table["fileType"]
            .takeIf { it is LuaTable }
            ?.let { extractFileType(it as LuaTable) }
            ?: FileType.UNKNOWN_TYPE
        val priority = table["priority"]
            .takeIf { it is LuaNumber }
            ?.toint()
            ?: org.islandoftex.arara.core.files.ProjectFile.DEFAULT_PRIORITY

        return org.islandoftex.arara.core.files.ProjectFile(MPPPath(path), fileType, priority)
    }

    private fun extractProject(table: LuaTable): Project {
        val name = table["name"].takeIf { it is LuaString }?.toString()
            ?: "Untitled project"
        val workingDirectory = table["workingDirectory"].takeIf { it is LuaString }?.toString()
            ?: "."

        val files = table["files"].takeIf { it is LuaTable }?.let {
            // TODO: check that keys are actual files resolved against working directory
            val fileTable = it as LuaTable
            fileTable.keys()
                .mapNotNull { key ->
                    fileTable[key]
                        .takeIf { value -> value is LuaTable }
                        ?.let { fileSpec -> extractProjectFile(key.toString(), fileSpec as LuaTable) }
                }
                .toSet()
        } ?: setOf()

        require(files.isEmpty()) {
            "A project must contain at least one file"
        }

        val dependencies = table["dependencies"].takeIf { it is LuaTable }?.let {
            (it as LuaTable)
                .keys()
                .mapNotNull { key ->
                    table[key].takeIf { value -> value is LuaString }?.toString()
                }
                .toSet()
        } ?: setOf()

        return org.islandoftex.arara.core.files.Project(name, MPPPath(workingDirectory), files, dependencies)
    }

    /**
     * Try a toy example for Lua interpretation. To be extended to the
     * real project parsing.
     */
    fun tryProjectToyExample() {
        val globals = CommonPlatform.Companion.standardGlobals()
        val script = """
            return {
              name = "My awesome book",
              workingDirectory = ".",
              files = {
                ["a.mp"] = { directives = { "metapost" } },
                ["file.tex"] = {
                  dependencies = { "a.mp" }
                }
              },
              dependencies = { "project b" }
            }
        """.trimIndent()
        val f = globals.load(script) as LuaFunction
        val c = f.checkclosure()!!
        val t = c.call() as? LuaTable
        println(extractProject(t!!))
    }
}
