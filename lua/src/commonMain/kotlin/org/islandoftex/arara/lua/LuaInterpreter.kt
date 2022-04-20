// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.files.UNKNOWN_TYPE
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaNumber
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.common.CommonPlatform

class LuaInterpreter(private val appWorkingDir: MPPPath) {
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
        val extension = fileType["extension"].takeIf { it is LuaString }?.toString()
            ?: throw ProjectParseException("An extension is required to identify the file type.")
        val pattern = fileType["pattern"]
            .takeIf { it is LuaString }
            ?.toString()
            ?: ConfigurationUtils.defaultFileTypePatterns[extension]
            ?: throw ProjectParseException("A pattern is missing (unknown and not in the default file types).")

        return org.islandoftex.arara.core.files.FileType(extension, pattern)
    }

    private fun extractProjectFile(path: MPPPath, table: LuaTable): ProjectFile {
        val fileType = table["fileType"]
            .takeIf { it is LuaTable }
            ?.let { extractFileType(it as LuaTable) }
            ?: FileType.UNKNOWN_TYPE
        val priority = table["priority"]
            .takeIf { it is LuaNumber }
            ?.toint()
            ?: org.islandoftex.arara.core.files.ProjectFile.DEFAULT_PRIORITY

        return org.islandoftex.arara.core.files.ProjectFile(path, fileType, priority)
    }

    private fun extractProject(table: LuaTable): Project {
        val name = table["name"].takeIf { it is LuaString }?.toString()
            ?: "Untitled project"
        val workingDirectory = table["workingDirectory"]
            .takeIf { it is LuaString }
            ?.toString()
            ?.let { MPPPath(it) }
            ?: appWorkingDir

        val files = table["files"].takeIf { it is LuaTable }?.let {
            val fileTable = it as LuaTable
            fileTable.keys()
                .mapNotNull { key ->
                    val path = workingDirectory.resolve(key.toString())
                    // TODO: check if path exists and issue warning otherwise
                    if (path.isDirectory) {
                        throw ProjectParseException("A project file must not be a directory.")
                    }

                    fileTable[key]
                        .takeIf { value -> value is LuaTable }
                        ?.let { fileSpec ->
                            extractProjectFile(
                                path,
                                fileSpec as LuaTable
                            )
                        }
                }
                .toSet()
        } ?: setOf()

        if (files.isEmpty()) {
            throw ProjectParseException("A project must contain at least one file")
        }

        val dependencies = table["dependencies"].takeIf { it is LuaTable }?.let {
            (it as LuaTable)
                .keys()
                .mapNotNull { key ->
                    it[key].takeIf { value -> value is LuaString }?.toString()
                }
                .toSet()
        } ?: setOf()

        return org.islandoftex.arara.core.files.Project(name, workingDirectory, files, dependencies)
    }

    /**
     * Execute a lua script to get a list of projects.
     *
     * This tries to resolve the return value of the lua script/closure to
     * a single project and, if that fails, to a list of projects.
     *
     * @param luaScript The Lua string to be interpreted.
     * @throws IllegalArgumentException if the parsing fails.
     * @return List of projects extracted from the Lua run.
     */
    fun parseProjectsFromLua(luaScript: String): List<Project> {
        val globals = CommonPlatform.Companion.standardGlobals()
        val f = globals.load(luaScript) as LuaFunction
        val c = f.checkclosure()!!
        val t = c.call() as? LuaTable
            ?: throw ProjectParseException(
                "The return type of the Lua project specification has to be a table " +
                    "(project or list of projects)."
            )

        // try extracting a single project from the Lua result and if that
        // fails try to extract a list of projects; only if that fails, fail
        // parsing
        return kotlin.runCatching {
            extractProject(t)
                .takeIf { it.dependencies.isEmpty() }
                ?.let { listOf(it) }
                ?: throw ProjectValidationException("Single project has dependencies which are not declared.")
        }.getOrElse {
            // TODO: log "illegal" values in the list which are filtered
            t.keys()
                .mapNotNull { key -> t[key].takeIf { it is LuaTable } }
                .map { extractProject(it as LuaTable) }
                .toList()
                .takeIf { projects ->
                    projects.all { project ->
                        project.dependencies.all { dep ->
                            projects.find { it.name == dep } != null
                        }
                    }
                }
                ?: throw ProjectValidationException("Project dependencies have not been resolved successfully.")
        }
    }
}
