// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

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

    private data class Project(
        val name: String,
        val workingDirectory: String,
        val files: Set<ProjectFile>,
        val dependencies: Set<String> = setOf()
    )

    private data class ProjectFile(
        val path: String,
        val fileType: String,
        val priority: Int,
    )

    private fun extractProjectFile(path: String, table: LuaTable): ProjectFile {
        val fileType = table["fileType"].takeIf { it is LuaString }?.toString()
            ?: "TeX"
        val priority = table["priority"].takeIf { it is LuaNumber }?.toint()
            ?: 0

        return ProjectFile(path, fileType, priority)
    }

    private fun extractProject(table: LuaTable): Project {
        val name = table["name"].takeIf { it is LuaString }?.toString()
            ?: "Untitled project"
        val workingDirectory = table["workingDirectory"].takeIf { it is LuaString }?.toString()
            ?: "."

        val files = table["files"].takeIf { it is LuaTable }?.let {
            // TODO: check that keys are actual files
            val fileTable = it as LuaTable
            fileTable.keys()
                .mapNotNull { key ->
                    fileTable[key]
                        .takeIf { value -> value is LuaTable }
                        ?.let { fileSpec -> extractProjectFile(key.toString(), fileSpec as LuaTable) }
                }
                .toSet()
        } ?: setOf()
        if (files.isEmpty()) {
            throw IllegalArgumentException("A project must contain at least one file")
        }

        val dependencies = table["dependencies"].takeIf { it is LuaTable }?.let {
            (it as LuaTable)
                .keys()
                .mapNotNull { key ->
                    table[key].takeIf { value -> value is LuaString }?.toString()
                }
                .toSet()
        } ?: setOf()

        return Project(name, workingDirectory, files, dependencies)
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
