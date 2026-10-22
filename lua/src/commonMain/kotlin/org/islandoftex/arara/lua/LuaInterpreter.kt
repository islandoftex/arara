// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.files.UNKNOWN_TYPE
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.lua.rules.LuaConversion
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaNumber
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

/**
 * Interprets project tables without coercing malformed fields. Scripts receive
 * `user.home`, `user.name` and the invocation working directory as `cwd`, in fresh globals.
 */
class LuaInterpreter(private val appWorkingDir: MPPPath) {
    // Each throw identifies a distinct field violation at the format boundary.
    @Suppress("ThrowsCount")
    private fun fileType(table: LuaTable, context: String): FileType {
        ProjectFields.keys(table, setOf("extension", "pattern"), context)
        val extension = ProjectFields.string(table["extension"], "$context.extension")
            ?: throw ProjectParseException(messages.errorScriptRequired("$context.extension"))
        if (extension.isBlank()) throw ProjectParseException(
            messages.errorScriptNonblank("$context.extension")
        )
        val pattern = ProjectFields.string(table["pattern"], "$context.pattern")
            ?: ConfigurationUtils.defaultFileTypePatterns[extension]
            ?: throw ProjectParseException(messages.errorScriptRequired("$context.pattern ($extension)"))
        return org.islandoftex.arara.core.files.FileType(extension, pattern)
    }

    private fun file(path: MPPPath, table: LuaTable, context: String): LuaProjectFile {
        ProjectFields.keys(table, setOf("file_type", "priority", "directives"), context)
        val type = ProjectFields.table(table["file_type"], "$context.file_type")
            ?.let { fileType(it, "$context.file_type") } ?: FileType.UNKNOWN_TYPE
        val value = table["priority"]
        val priority = when {
            value.isnil() -> 0
            value is LuaNumber && value.isint() -> value.toint()
            else -> throw ProjectParseException(messages.errorScriptInteger("$context.priority"))
        }
        return LuaProjectFile(path, type, priority).apply {
            directives = ProjectFields.table(table["directives"], "$context.directives")
                ?.let { ProjectFields.strings(it, "$context.directives") }
        }
    }

    @Suppress("ThrowsCount")
    private fun project(table: LuaTable, context: String, defaultName: String): Project {
        ProjectFields.keys(table, setOf("name", "working_directory", "files", "dependencies"), context)
        val name = ProjectFields.string(table["name"], "$context.name") ?: defaultName
        if (name.isBlank()) throw ProjectParseException(messages.errorScriptNonblank("$context.name"))
        val directory = ProjectFields.string(table["working_directory"], "$context.working_directory")
            ?.let { appWorkingDir.resolve(it).normalize() } ?: appWorkingDir.normalize()
        val files = ProjectFields.table(table["files"], "$context.files")
            ?: throw ProjectParseException(messages.errorScriptRequired("$context.files"))
        val paths = mutableSetOf<MPPPath>()
        val entries = files.keys().map { key ->
            val reference = ProjectFields.string(key, "$context.files key")
                ?: throw ProjectParseException(messages.errorScriptStringKeys("$context.files"))
            if (reference.isBlank()) throw ProjectParseException(
                messages.errorScriptNonblank("$context.files key")
            )
            val path = directory.resolve(reference).normalize()
            if (!paths.add(path)) throw ProjectParseException(
                messages.errorScriptDuplicate("$context.files", path.toString())
            )
            if (path.isDirectory) throw ProjectParseException(
                messages.errorScriptDirectory("$context.files[$reference]")
            )
            val spec = ProjectFields.table(files[key], "$context.files[$reference]")
                ?: throw ProjectParseException(messages.errorScriptTable("$context.files[$reference]"))
            file(path, spec, "$context.files[$reference]")
        }.toSet()
        if (entries.isEmpty()) throw ProjectParseException(messages.errorScriptEmpty("$context.files"))
        val dependencies = ProjectFields.table(table["dependencies"], "$context.dependencies")
            ?.let { ProjectFields.strings(it, "$context.dependencies").toSet() } ?: emptySet()
        return org.islandoftex.arara.core.files.Project(name, directory, entries, dependencies)
    }

    /** A table with named fields denotes one project; a positional table denotes a project list. */
    @Suppress("ThrowsCount")
    fun parseProjectsFromLua(
        luaScript: String,
        defaultName: String = "Untitled project",
        chunkName: String = "specification"
    ): List<Project> {
        val root = kotlin.runCatching {
            LuaScriptEnvironment.newGlobals(appWorkingDir).load(luaScript, chunkName).call()
        }.getOrElse {
            val detail = if (it is LuaError) LuaConversion.diagnostic(it) else it.message.orEmpty()
            throw ProjectParseException(
                messages.errorProjectInvalidLua(chunkName, detail),
                it as? Exception ?: RuntimeException(it)
            )
        }
        val table = root as? LuaTable ?: throw ProjectParseException(
            messages.errorScriptTable("specification")
        )
        val projects = if (table.keys().any { it is LuaString }) {
            listOf(project(table, "project", defaultName))
        } else {
            ProjectFields.list(table, "projects").mapIndexed { index, value ->
                val entry = value as? LuaTable
                    ?: throw ProjectParseException(messages.errorScriptTable("projects[${index + 1}]"))
                if (entry["name"].isnil()) throw ProjectParseException(
                    messages.errorScriptRequired("projects[${index + 1}].name")
                )
                project(entry, "projects[${index + 1}]", defaultName)
            }
        }
        if (projects.isEmpty()) throw ProjectParseException(messages.errorScriptEmpty("projects"))
        val names = projects.map { it.name }
        if (names.toSet().size != names.size) {
            val duplicates = names.groupBy { it }.filterValues { it.size > 1 }.keys.joinToString(", ")
            throw ProjectValidationException(messages.errorProjectDuplicateNames(duplicates))
        }
        return projects
    }

    companion object {
        /** Resolve implicit types against the configuration belonging to this project. */
        fun resolvePendingFileTypes(
            project: Project,
            options: org.islandoftex.arara.api.configuration.ExecutionOptions
        ): Project {
            val files = project.files.map { file ->
                if (file !is LuaProjectFile || file.fileType != FileType.UNKNOWN_TYPE) {
                    file
                } else {
                    val type = options.fileTypes.firstOrNull {
                        file.path.fileName.endsWith(".${it.extension}")
                    } ?: throw ProjectParseException(messages.errorProjectFiletype(file.path.toString()))
                    LuaProjectFile(file.path, type, file.priority).apply { directives = file.directives }
                }
            }.toSet()
            return org.islandoftex.arara.core.files.Project(
                project.name, project.workingDirectory, files, project.dependencies
            )
        }
    }
}

/** Strict conversions for the project format's data boundary. */
internal object ProjectFields {
    fun keys(table: LuaTable, allowed: Set<String>, context: String) {
        table.keys().firstOrNull { it !is LuaString || it.toString() !in allowed }?.let {
            throw ProjectParseException(messages.errorScriptUnknownKey("$context.$it"))
        }
    }

    fun string(value: LuaValue, context: String): String? = when {
        value.isnil() -> null
        value is LuaString -> value.toString()
        else -> throw ProjectParseException(messages.errorScriptString(context))
    }

    fun table(value: LuaValue, context: String): LuaTable? = when {
        value.isnil() -> null
        value is LuaTable -> value
        else -> throw ProjectParseException(messages.errorScriptTable(context))
    }

    fun list(table: LuaTable, context: String): List<LuaValue> {
        val size = table.keys().size
        if (table.keys().any { it !is LuaNumber || !it.isint() || it.toint() !in 1..size }) {
            throw ProjectParseException(messages.errorScriptList(context))
        }
        return (1..size).map { table[it] }
    }

    fun strings(table: LuaTable, context: String): List<String> = list(table, context).mapIndexed { index, value ->
        string(value, "$context[${index + 1}]")
            ?: throw ProjectParseException(messages.errorScriptString("$context[${index + 1}]"))
    }
}

private val messages get() = LanguageController.messages
