// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import io.github.taoguan.luaj.LuaNumber
import io.github.taoguan.luaj.LuaString
import io.github.taoguan.luaj.LuaTable
import io.github.taoguan.luaj.LuaValue
import io.github.taoguan.luaj.lib.jse.JsePlatform
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.configuration.DefaultConfiguration
import org.islandoftex.arara.core.files.UNKNOWN_TYPE

/**
 * Interprets Lua project specification files, i.e. Lua scripts
 * returning a project table or a list of project tables.
 *
 * @param appWorkingDir The working directory to resolve relative file
 * references against.
 */
class LuaInterpreter(private val appWorkingDir: MPPPath) {
    /**
     * Extract an optional string entry: a missing or nil value yields
     * null, any other non-string value is a parse error (Lua numbers do
     * not silently become strings at the data boundary).
     */
    private fun optionalString(value: LuaValue, typeError: String): String? =
        when {
            value.isnil() -> null
            value is LuaString -> value.toString()
            else -> throw ProjectParseException(typeError)
        }

    /**
     * Extract an optional table entry: a missing or nil value yields
     * null, any other non-table value is a parse error.
     */
    private fun optionalTable(value: LuaValue, typeError: String): LuaTable? =
        when {
            value.isnil() -> null
            value is LuaTable -> value
            else -> throw ProjectParseException(typeError)
        }

    private fun extractFileType(fileType: LuaTable): FileType {
        val extension = fileType["extension"].takeIf { it is LuaString }?.toString()
            ?: throw ProjectParseException("An extension is required to identify the file type.")
        val pattern = optionalString(
            fileType["pattern"],
            "The file type 'pattern' key requires a string value."
        ) ?: DefaultConfiguration.defaultFileTypePatterns[extension]
            ?: throw ProjectParseException("A pattern is missing (unknown and not in the default file types).")

        return org.islandoftex.arara.core.files.FileType(extension, pattern)
    }

    /**
     * Extracts the file name key of a project file entry, which has to
     * be a Lua string.
     */
    private fun projectFileKey(key: LuaValue): LuaString =
            key as? LuaString
                ?: throw ProjectParseException(
                    "Project file entries have to be keyed by " +
                        "their file name."
                )

    /**
     * Extracts the specification table of a project file entry.
     */
    private fun projectFileSpec(
        fileTable: LuaTable,
        key: LuaValue,
    ): LuaTable =
            fileTable[key] as? LuaTable
                ?: throw ProjectParseException(
                    "A project file specification has to be a table."
                )

    /**
     * Extracts a single project file entry from its key and its
     * specification table, rejecting directories on the way.
     */
    private fun projectFileEntry(
        fileTable: LuaTable,
        key: LuaValue,
        workingDirectory: MPPPath,
    ): ProjectFile {
        val path = workingDirectory.resolve(projectFileKey(key).toString())
        // note: verify that the path exists and warn otherwise
        if (path.isDirectory) {
            throw ProjectParseException("A project file must not be a directory.")
        }
        return extractProjectFile(path, projectFileSpec(fileTable, key))
    }

        /**
     * Extracts a project file from the Lua table the project
     * specification provides, resolving the file type and the
     * priority from the table's fields.
     *
     * @param path The path the specification names.
     * @param table The Lua table describing the project file.
     * @return The extracted project file.
     */
private fun extractProjectFile(path: MPPPath, table: LuaTable): ProjectFile {
        val fileType = optionalTable(
            table["file_type"],
            "The 'file_type' key requires a table."
        )?.let { extractFileType(it) } ?: FileType.UNKNOWN_TYPE

        val priority = table["priority"].let { value ->
            when {
                value.isnil() -> null
                value is LuaNumber -> value.toint()
                else -> throw ProjectParseException(
                    "The 'priority' key requires a number."
                )
            }
        } ?: org.islandoftex.arara.core.files.ProjectFile.DEFAULT_PRIORITY

        val projectFile = LuaProjectFile(path, fileType, priority)
        optionalTable(
            table["directives"],
            "The 'directives' key requires a table."
        )?.let { luaDirectives ->
            projectFile.directives = luaDirectives.keys()
                .map { key -> luaDirectives[key] }
                .map { value ->
                    (value as? LuaString)?.toString()
                        ?: throw ProjectParseException(
                            "Every directive entry has to be a string."
                        )
                }
                .toList()
        }
        return projectFile
    }

    private fun extractProject(table: LuaTable): Project {
        val name = optionalString(
            table["name"],
            "The project 'name' key requires a string value."
        ) ?: "Untitled"
        val workingDirectory = optionalString(
            table["working_directory"],
            "The 'working_directory' key requires a string value."
        )?.let { MPPPath(it) } ?: appWorkingDir

        val files = optionalTable(
            table["files"],
            "The 'files' key requires a table of file specifications."
        )?.let { fileTable ->
            fileTable.keys().map { key ->
                projectFileEntry(fileTable, key, workingDirectory)
            }.toSet()
        } ?: setOf()

        if (files.isEmpty()) {
            throw ProjectParseException("A project must contain at least one file")
        }

        val dependencies = optionalTable(
            table["dependencies"],
            "The 'dependencies' key requires a list of strings."
        )?.let { dependencyTable ->
            dependencyTable.keys().map { key ->
                (dependencyTable[key] as? LuaString)?.toString()
                    ?: throw ProjectParseException(
                        "Every dependency entry has to be a string."
                    )
            }.toSet()
        } ?: setOf()

        return org.islandoftex.arara.core.files.Project(name, workingDirectory, files, dependencies)
    }

    /**
     * Execute a lua script to get a list of projects.
     *
     * A root table holding a 'files' table denotes a single project;
     * any other root is read as a list of project tables. The parse
     * enforces the documented project invariants and reports every
     * violation where it is detected.
     *
     * @param luaScript The Lua string to be interpreted.
     * @throws ProjectParseException The Lua script is invalid or does not
     * return a project table.
     * @throws ProjectValidationException The returned project specification
     * violates the documented invariants.
     * @return List of projects extracted from the Lua run.
     */
    @Suppress("ThrowsCount")
    fun parseProjectsFromLua(luaScript: String): List<Project> {
        val luaTable = runCatching {
            JsePlatform.standardGlobals()
                .load(luaScript)
                .call() as? LuaTable
        }.getOrElse {
            throw ProjectParseException(
                "Detected invalid Lua: ${it.message}",
                it as? Exception ?: RuntimeException(it.toString())
            )
        }
            ?: throw ProjectParseException(
                "The return type of the Lua project specification has to be a table " +
                    "(project or list of projects)."
            )

        if (luaTable["files"] is LuaTable) {
            val project = extractProject(luaTable)
            if (project.dependencies.isNotEmpty()) {
                throw ProjectValidationException("Single project has dependencies which are not declared.")
            }
            return listOf(project)
        }

        val projects = luaTable.keys()
            .map { key ->
                luaTable[key] as? LuaTable
                    ?: throw ProjectParseException(
                        "A project list entry has to be a table."
                    )
            }
            .map { extractProject(it) }
            .toList()
        return projects.takeIf { projectList ->
            projectList.all { project ->
                project.dependencies.all { dep ->
                    projectList.find { it.name == dep } != null
                }
            }
        } ?: throw ProjectValidationException("Project dependencies have not been resolved successfully.")
    }
}
