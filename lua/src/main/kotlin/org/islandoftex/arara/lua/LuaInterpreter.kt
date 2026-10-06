// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.UNKNOWN_TYPE
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Interprets Lua project specification files, i.e. Lua scripts
 * returning a project table or a list of project tables.
 *
 * @param appWorkingDir The working directory to resolve relative file
 * references against.
 */
// the parser walks a key surface of its own depth - projects,
// file entries and file types - and every extraction is its own
// function; the count is the shape of the format, not
// accumulated responsibilities
@Suppress("TooManyFunctions")
class LuaInterpreter(private val appWorkingDir: MPPPath) {
    /**
     * The logger of the Lua project interpreter.
     */
    private val logger = KotlinLogging.logger { }

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

    /**
     * The keys a project table accepts; a key outside the set
     * names itself instead of passing silently.
     */
    private val projectKeys = setOf(
        "name", "working_directory", "files", "dependencies"
    )

    /**
     * The keys a project file entry accepts.
     */
    private val projectFileKeys = setOf("file_type", "priority", "directives")

    /**
     * The keys a project file type accepts.
     */
    private val fileTypeKeys = setOf("extension", "pattern")

    /**
     * Collects the keys a table carries outside the accepted set
     * and reports them together, qualified the way the
     * configuration format names a stray key of its own groups:
     * bare at the root, prefixed by its surface deeper in.
     */
    private fun rejectUnknownKeys(
        table: LuaTable,
        keys: Set<String>,
        qualify: (String) -> String = { it }
    ) {
        table.keys().map { it.tojstring() }
            .filter { it !in keys }
            .takeIf { it.isNotEmpty() }?.let { unknown ->
                throw ProjectParseException(
                    LanguageController.messages.errorProjectUnknownKey(
                        unknown.joinToString(", ", "(", ")") { key ->
                            qualify(key)
                        }
                    )
                )
            }
    }

    /**
     * Extracts a file type from its Lua table: the extension is
     * required, and the pattern may come from the table or from
     * the default patterns of a known extension, and the table
     * accepts no key outside its own pair.
     */
    private fun extractFileType(fileType: LuaTable): FileType {
        rejectUnknownKeys(fileType, fileTypeKeys) { "file_type.$it" }
        val extension = fileType["extension"].takeIf { it is LuaString }?.toString()
            ?: throw ProjectParseException(
                LanguageController.messages
                    .errorProjectFileTypeExtensionRequired
            )
        val pattern = optionalString(
            fileType["pattern"],
            LanguageController.messages
                .errorProjectFileTypePatternNotAString
        ) ?: DefaultConfiguration.defaultFileTypePatterns[extension]
            ?: throw ProjectParseException(
                LanguageController.messages
                    .errorProjectFileTypePatternMissing
            )

        return org.islandoftex.arara.core.files.FileType(extension, pattern)
    }

    /**
     * Detects the file type of an entry by its extension, against
     * the file types in effect when the specification is read; an
     * extension nothing carries stays pending for the project's
     * own configuration to resolve.
     */
    private fun detectFileType(path: MPPPath): FileType =
        FileSearching.lookupFileType(
            path.fileName.substringAfterLast('.', ""),
            LinearExecutor.executionOptions
        ) ?: FileType.UNKNOWN_TYPE

    /**
     * Extracts the file name key of a project file entry, which has to
     * be a Lua string.
     */
    private fun projectFileKey(key: LuaValue): LuaString =
            key as? LuaString
                ?: throw ProjectParseException(
                    LanguageController.messages
                        .errorProjectFileEntryKeyNotAString
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
                    LanguageController.messages
                        .errorProjectFileSpecNotATable
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
        val name = projectFileKey(key).toString()
        val path = workingDirectory.resolve(name)
        if (path.isDirectory) {
            throw ProjectParseException(
                LanguageController.messages
                    .errorProjectFileIsADirectory
            )
        }
        // a missing entry keeps its place in the specification: a
        // run may generate the file before its turn comes, so the
        // entry is only named in the log for the author to check,
        // and a file still missing when its turn arrives fails
        // with the ordinary extraction error
        if (!path.exists) {
            logger.warn {
                "The project file entry $path does not exist yet."
            }
        }
        return extractProjectFile(name, path, projectFileSpec(fileTable, key))
    }

    /**
     * Extracts a project file from the Lua table the project
     * specification provides, resolving the file type and the
     * priority from the table's fields.
     *
     * @param name The file name as the specification spells it.
     * @param path The path the specification names.
     * @param table The Lua table describing the project file.
     * @return The extracted project file.
     */
    private fun extractProjectFile(
        name: String,
        path: MPPPath,
        table: LuaTable
    ): ProjectFile {
        rejectUnknownKeys(table, projectFileKeys) { "$name.$it" }
        val fileType = optionalTable(
            table["file_type"],
            LanguageController.messages
                .errorProjectFileTypeKeyRequiresTable
        )?.let { extractFileType(it) } ?: detectFileType(path)

        val priority = table["priority"].let { value ->
            when {
                value.isnil() -> null
                value is LuaNumber -> value.toint()
                else -> throw ProjectParseException(
                    LanguageController.messages
                        .errorProjectPriorityKeyRequiresNumber
                )
            }
        } ?: org.islandoftex.arara.core.files.ProjectFile.DEFAULT_PRIORITY

        val projectFile = LuaProjectFile(path, fileType, priority)
        optionalTable(
            table["directives"],
            LanguageController.messages
                .errorProjectDirectivesKeyRequiresTable
        )?.let { luaDirectives ->
            projectFile.directives = luaDirectives.keys()
                .map { key -> luaDirectives[key] }
                .map { value ->
                    (value as? LuaString)?.toString()
                        ?: throw ProjectParseException(
                            LanguageController.messages
                                .errorProjectDirectiveEntryNotAString
                        )
                }
                .toList()
        }
        return projectFile
    }

    /**
     * Extracts the files of a project from their table: every
     * entry resolves against the working directory, and a file
     * is listed once - two spellings of the same location are
     * still the same file, and the path's equality resolves
     * the spelling.
     */
    private fun extractFiles(
        fileTable: LuaTable,
        workingDirectory: MPPPath
    ): Set<ProjectFile> {
        val seen = mutableSetOf<MPPPath>()
        return fileTable.keys().map { key ->
            val name = projectFileKey(key).toString()
            val file = projectFileEntry(fileTable, key, workingDirectory)
            if (!seen.add(file.path)) {
                throw ProjectParseException(
                    LanguageController.messages
                        .errorProjectDuplicatePath(name)
                )
            }
            file
        }.toSet()
    }

    /**
     * Extracts a project from its Lua table: the name falls back to
     * the caller's default when the key is absent, the working
     * directory resolves against the application's, and the file
     * entries and dependencies convert to their own shapes.
     */
    private fun extractProject(
        table: LuaTable,
        defaultName: String
    ): Project {
        rejectUnknownKeys(table, projectKeys)
        val name = optionalString(
            table["name"],
            LanguageController.messages
                .errorProjectNameKeyRequiresString
        ) ?: defaultName
        val workingDirectory = optionalString(
            table["working_directory"],
            LanguageController.messages
                .errorProjectWorkingDirectoryKeyRequiresString
        )?.let { MPPPath(it) } ?: appWorkingDir

        val files = optionalTable(
            table["files"],
            LanguageController.messages
                .errorProjectFilesKeyRequiresTable
        )?.let { extractFiles(it, workingDirectory) } ?: setOf()

        if (files.isEmpty()) {
            throw ProjectParseException(
                LanguageController.messages.errorProjectNeedsAFile
            )
        }

        val dependencies = optionalTable(
            table["dependencies"],
            LanguageController.messages
                .errorProjectDependenciesKeyRequiresStringList
        )?.let { dependencyTable ->
            dependencyTable.keys().map { key ->
                (dependencyTable[key] as? LuaString)?.toString()
                    ?: throw ProjectParseException(
                        LanguageController.messages
                            .errorProjectDependencyEntryNotAString
                    )
            }.toSet()
        } ?: setOf()

        return org.islandoftex.arara.core.files.Project(name, workingDirectory, files, dependencies)
    }

    /**
     * Extracts the projects of a positional list: every entry is a
     * project table carrying an explicit name, the names are
     * unique within the list, and the list is not empty.
     */
    @Suppress("ThrowsCount")
    private fun extractProjectList(
        luaTable: LuaTable,
        defaultName: String
    ): List<Project> {
        // a list of projects is positional; a root table with named
        // keys is not a list, and reading it as one would misreport
        // a malformed single project, so its named keys name
        // themselves instead
        luaTable.keys().filterIsInstance<LuaString>()
            .takeIf { it.isNotEmpty() }?.let { named ->
                throw ProjectParseException(
                    LanguageController.messages.errorProjectUnknownKey(
                        named.joinToString(", ", "(", ")") { key ->
                            key.tojstring()
                        }
                    )
                )
            }

        val projects = luaTable.keys()
            .map { key ->
                luaTable[key] as? LuaTable
                    ?: throw ProjectParseException(
                        LanguageController.messages
                            .errorProjectListEntryNotATable
                    )
            }
            .map { table ->
                // dependencies identify projects by name, so every
                // project of a list has to carry one
                if (table["name"].isnil()) {
                    throw ProjectParseException(
                        LanguageController.messages
                            .errorProjectListEntryNeedsName
                    )
                }
                extractProject(table, defaultName)
            }
            .toList()

        if (projects.isEmpty()) {
            throw ProjectParseException(
                LanguageController.messages.errorProjectReturnNotATable
            )
        }

        // a name identifies a project within one run, so the
        // list may not repeat it
        projects.map { it.name }
            .groupBy { it }
            .filterValues { it.size > 1 }.keys
            .takeIf { it.isNotEmpty() }?.let { duplicated ->
                throw ProjectValidationException(
                    LanguageController.messages.errorProjectDuplicateName(
                        duplicated.joinToString(", ", "(", ")")
                    )
                )
            }
        return projects
    }

    /**
     * Execute a lua script to get a list of projects.
     *
     * A root table holding a 'files' table denotes a single project;
     * any other root is read as a list of project tables. The parse
     * enforces the documented invariants of one specification and
     * reports every violation where it is detected; the
     * dependencies resolve against the whole invocation, one
     * level up.
     *
     * @param luaScript The Lua string to be interpreted.
     * @param defaultName The name a project that declared no name
     *   takes; the caller names it from its specification file.
     * @throws ProjectParseException The Lua script is invalid or does not
     * return a project table.
     * @throws ProjectValidationException The returned project specification
     * violates the documented invariants.
     * @return List of projects extracted from the Lua run.
     */
    @Suppress("ThrowsCount")
    fun parseProjectsFromLua(
        luaScript: String,
        defaultName: String = "Untitled"
    ): List<Project> {
        val luaTable = runCatching {
            // arara's values are in scope to the script, the same
            // platform the configuration format provides: the
            // table `user` carries the `home` and `name` of the
            // user running arara, and `cwd` is the working
            // directory the specification is loaded for
            val globals = JsePlatform.standardGlobals()
            globals.set(
                "user",
                LuaValue.tableOf().also { user ->
                    user.set(
                        "home",
                        LuaValue.valueOf(
                            Environment.getSystemPropertyOrNull("user.home")
                                ?: ""
                        )
                    )
                    user.set(
                        "name",
                        LuaValue.valueOf(
                            Environment.getSystemPropertyOrNull("user.name")
                                ?: ""
                        )
                    )
                }
            )
            globals.set(
                "cwd",
                LuaValue.valueOf(appWorkingDir.normalize().toString())
            )
            globals.load(luaScript).call() as? LuaTable
        }.getOrElse {
            throw ProjectParseException(
                LanguageController.messages
                    .errorProjectInvalidLua(it.message ?: ""),
                it as? Exception ?: RuntimeException(it.toString())
            )
        }
            ?: throw ProjectParseException(
                LanguageController.messages
                    .errorProjectReturnNotATable
            )

        if (luaTable["files"] is LuaTable) {
            val project = extractProject(luaTable, defaultName)
            if (project.dependencies.isNotEmpty()) {
                throw ProjectValidationException(
                    LanguageController.messages
                        .errorProjectSingleProjectHasDependencies
                )
            }
            return listOf(project)
        }

        // dependencies resolve against the whole invocation, so the
        // parse validates the shapes of one specification alone -
        // the merged verdicts live one level up, where every
        // project of the run is known
        return extractProjectList(luaTable, defaultName)
    }

    /**
     * Holds the post-configuration resolution of the file types
     * the parse left pending.
     */
    companion object {
        /**
         * Resolves the file types the parse left pending: an entry
         * that declared no file type is detected by its extension
         * against the file types in effect for the project - its
         * own configuration is in by now - and an extension no
         * file type carries is an error naming the file and the
         * remedy, before anything runs.
         *
         * @param project The project whose pending entries resolve.
         * @return The project with every pending file type
         *   resolved, itself when nothing was pending.
         */
        fun resolvePendingFileTypes(project: Project): Project {
            val resolved = project.files.map { file ->
                if (file is LuaProjectFile &&
                    file.fileType == FileType.UNKNOWN_TYPE
                ) {
                    val fileType = FileSearching.lookupFileType(
                        file.path.fileName.substringAfterLast('.', ""),
                        LinearExecutor.executionOptions
                    ) ?: throw ProjectParseException(
                        LanguageController.messages
                            .errorProjectFileTypeUnknownExtension(
                                file.path.toString()
                            )
                    )
                    LuaProjectFile(file.path, fileType, file.priority)
                        .also { it.directives = file.directives }
                } else {
                    file
                }
            }.toSet()
            return if (resolved == project.files) {
                project
            } else {
                org.islandoftex.arara.core.files.Project(
                    project.name,
                    project.workingDirectory,
                    resolved,
                    project.dependencies
                )
            }
        }
    }
}
