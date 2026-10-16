// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.configuration

import io.github.taoguan.luaj.LuaTable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.configuration.SerialFileType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.lua.LuaPlatform
import org.islandoftex.arara.lua.rules.LuaConversion

/**
 * Parse configuration files written in arara's Lua configuration
 * format.
 *
 * A configuration file is a Lua script returning a table. The keys
 * that name single values or plain lists sit at the root, and the
 * keys that form true families nest in their own tables:
 *
 * ```lua
 * return {
 *   paths = {
 *     user.home .. "/opt/paulo/rules",
 *     cwd .. "/local-rules"
 *   },
 *   file_types = {
 *     { extension = "tex" },
 *     { extension = "texw", pattern = "^\\s*%\\s+" }
 *   },
 *   language = "en",
 *   loops = 10,
 *   verbose = true,
 *   header = true,
 *   database = "arara.db",
 *   log = {
 *     enabled = true,
 *     file = "arara.log"
 *   },
 *   console = {
 *     mode = "plain",
 *     animations = true
 *   },
 *   preamble = {
 *     default = "twopdftex",
 *     prepend = "always",
 *     definitions = {
 *       twopdftex = {
 *         "% arara: pdflatex",
 *         "% arara: pdflatex"
 *       }
 *     }
 *   }
 * }
 * ```
 *
 * The `preamble.prepend` key names where the default preamble
 * applies: `"always"` prepends it to every file, and
 * `"without-directives"` keeps it for the files that carry no
 * directives of their own - any other word is an error. A
 * definition is a list of directive lines, one per entry, in the
 * same shape the `paths` list keeps.
 *
 * The `database` and `log.file` values name their files exactly
 * as written, extension included - a relative path resolves
 * against the working directory when the file is opened.
 *
 * The script runs with arara's own values in scope, so paths and
 * other strings are computed in plain Lua instead of relying on a
 * placeholder syntax: the table `user` carries the `home` and
 * `name` of the user running arara, and `cwd` is the working
 * directory the configuration is loaded for. As a script
 * evaluates once, at load, so do the values: `cwd` is the
 * working directory of that very moment.
 *
 * The parsed result is converted into the very same
 * [LocalConfiguration] representation the YAML parser produces,
 * so both formats behave identically once loaded.
 */
// the parser walks a key surface of two depths - roots and
// groups - and every extraction is its own function; the count
// is the shape of the format, not accumulated responsibilities
@Suppress("TooManyFunctions")
object LuaLocalConfiguration {
    /**
     * The configuration keys the Lua format accepts at the root of
     * the table; a key outside the set is an error.
     */
    private val knownKeys = setOf(
        "paths", "file_types", "language", "loops", "verbose", "header",
        "database", "log", "console", "preamble"
    )

    /**
     * Validates the configuration file.
     *
     * @param file The configuration file.
     * @param workingDirectory The working directory the configuration is
     *   loaded for; it is in scope to the script as `cwd`.
     * @return The configuration file as a resource.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun load(
        file: MPPPath,
        workingDirectory: MPPPath
    ): LocalConfiguration =
        runCatching {
            // arara's values are in scope to the script, mirroring
            // what the placeholder expansion of the YAML format
            // resolves
            val result = LuaPlatform.globalsFor(workingDirectory)
                .load(file.readText(), file.fileName).call()
            val table = result as? LuaTable ?: throw AraraException(
                LanguageController.messages
                    .errorConfigurationLuaReturnNotATable
            )
            fromMap(tableToMap(table))
        }.getOrElse {
            throw AraraException(
                LanguageController.messages.errorConfigurationGenericError,
                it as? Exception ?: AraraException(it.toString())
            )
        }

    /**
     * Converts a Lua table into a plain map, recursively
     * flattening nested tables into maps of their own.
     *
     * @param table The Lua table to convert.
     * @return The map representation of the table.
     */
    private fun tableToMap(table: LuaTable): Map<String, Any?> =
        table.keys().associate { key ->
            key.tojstring() to LuaConversion.fromLua(table.get(key))
        }

    /**
     * Converts the plain map of the Lua configuration into the
     * model, rejecting every key the format does not accept, at
     * the root and inside every group.
     *
     * @param map The map the Lua table converted to.
     * @return The configuration model.
     */
    @Throws(AraraException::class)
    private fun fromMap(map: Map<String, Any?>): LocalConfiguration {
        map.keys.minus(knownKeys).takeIf { it.isNotEmpty() }?.let {
            throw AraraException(
                LanguageController.messages
                    .errorConfigurationUnknownKeys(
                        it.joinToString(", ", "(", ")")
                    )
            )
        }
        val log = group(map, "log", setOf("enabled", "file"))
        val console = group(map, "console", setOf("mode", "animations"))
        val preamble = group(
            map, "preamble",
            setOf("default", "prepend", "definitions")
        )
        return LocalConfiguration(
            paths = stringList(map, "paths"),
            filetypes = fileTypes(map),
            language = string(map, "language"),
            loops = int(map, "loops"),
            verbose = boolean(map, "verbose"),
            logging = boolean(log, "enabled"),
            header = boolean(map, "header"),
            dbname = string(map, "database"),
            logname = string(log, "file"),
            console = string(console, "mode"),
            animations = boolean(console, "animations"),
            preambles = preambleDefinitions(preamble),
            defaultPreamble = string(preamble, "default"),
            prependPreambleIfDirectivesGiven = prependMode(preamble),
            // the script computed its paths with the values in scope,
            // so no engine tokens remain to expand downstream
            expandPathTokens = false
        )
    }

    /**
     * Extracts a group table from the map, validating the shape
     * and rejecting every subkey the group does not accept - a
     * typo inside a group names itself the same way a root key
     * does, qualified by its group.
     */
    @Throws(AraraException::class)
    private fun group(
        map: Map<String, Any?>,
        name: String,
        keys: Set<String>
    ): Map<String, Any?> =
        when (val value = map[name]) {
            null -> emptyMap()
            // an empty Lua table converts to an empty list
            is List<*> -> value.takeIf { it.isEmpty() }?.let { emptyMap() }
                ?: throw AraraException(
                    LanguageController.messages
                        .errorConfigurationKeyRequiresTable(name)
                )
            is Map<*, *> -> {
                value.keys.map { it.toString() }
                    .minus(keys).takeIf { it.isNotEmpty() }?.let {
                        throw AraraException(
                            LanguageController.messages
                                .errorConfigurationUnknownKeys(
                                    it.joinToString(", ", "(", ")") { key ->
                                        "$name.$key"
                                    }
                                )
                        )
                    }
                @Suppress("UNCHECKED_CAST")
                value as Map<String, Any?>
            }
            else -> throw AraraException(
                LanguageController.messages
                    .errorConfigurationKeyRequiresTable(name)
            )
        }

    /**
     * Extracts the preamble definitions: a table mapping each
     * preamble name to its list of directive lines, joined into
     * the single string the model keeps.
     */
    @Throws(AraraException::class)
    private fun preambleDefinitions(
        preamble: Map<String, Any?>
    ): Map<String, String> =
        when (val value = preamble["definitions"]) {
            null -> emptyMap()
            is List<*> -> value.takeIf { it.isEmpty() }?.let { emptyMap() }
                ?: throw AraraException(
                    LanguageController.messages
                        .errorConfigurationKeyRequiresTable("definitions")
                )
            is Map<*, *> -> value.entries.associate { (name, lines) ->
                val preambleName = name as? String
                    ?: throw AraraException(
                        LanguageController.messages
                            .errorConfigurationKeyRequiresStringKeys(
                                "definitions"
                            )
                    )
                val directives = lines as? List<*>
                    ?: throw AraraException(
                        LanguageController.messages
                            .errorConfigurationKeyRequiresStringList(
                                "definitions"
                            )
                    )
                preambleName to directives.joinToString("\n") { line ->
                    line as? String ?: throw AraraException(
                        LanguageController.messages
                            .errorConfigurationEntryNotAString(
                                "definitions"
                            )
                    )
                }
            }
            else -> throw AraraException(
                LanguageController.messages
                    .errorConfigurationKeyRequiresTable("definitions")
            )
        }

    /**
     * Extracts the preamble application policy: `"always"` names
     * the historical behavior and `"without-directives"` keeps the
     * default preamble for files with no directives of their own;
     * any other word is an error, and an absent key applies
     * everywhere.
     */
    @Throws(AraraException::class)
    private fun prependMode(preamble: Map<String, Any?>): Boolean =
        when (val value = string(preamble, "prepend")) {
            null -> true
            "always" -> true
            "without-directives" -> false
            else -> throw AraraException(
                LanguageController.messages
                    .errorConfigurationPreamblePrependInvalid(value)
            )
        }

    /**
     * Extracts an optional string value, rejecting any other type.
     */
    private fun string(map: Map<String, Any?>, key: String): String? =
        map[key]?.let {
            it as? String ?: throw AraraException(
                LanguageController.messages
                    .errorConfigurationKeyRequiresString(key)
            )
        }

    /**
     * Extracts an optional integer value, rejecting any other type.
     */
    private fun int(map: Map<String, Any?>, key: String): Int? =
        map[key]?.let {
            it as? Int ?: throw AraraException(
                LanguageController.messages
                    .errorConfigurationKeyRequiresInteger(key)
            )
        }

    /**
     * Extracts an optional boolean value, rejecting any other type.
     */
    private fun boolean(map: Map<String, Any?>, key: String): Boolean? =
        map[key]?.let {
            it as? Boolean ?: throw AraraException(
                LanguageController.messages
                    .errorConfigurationKeyRequiresBoolean(key)
            )
        }

    /**
     * Extracts a list of strings, rejecting any non-string entry
     * and any other shape.
     */
    private fun stringList(map: Map<String, Any?>, key: String): List<String> =
        (map[key] ?: emptyList<Any?>()).let { value ->
            (value as? List<*>)?.map { element ->
                element as? String ?: throw AraraException(
                    LanguageController.messages
                        .errorConfigurationEntryNotAString(key)
                )
            } ?: throw AraraException(
                LanguageController.messages
                    .errorConfigurationKeyRequiresStringList(key)
            )
        }

    /**
     * Extracts the file type declarations, each a table with an
     * extension and an optional pattern.
     */
    @Throws(AraraException::class)
    private fun fileTypes(map: Map<String, Any?>): List<SerialFileType> =
        (map["file_types"] ?: emptyList<Any?>()).let { value ->
            (value as? List<*>)?.map { entry ->
                val fileType = entry as? Map<*, *> ?: throw AraraException(
                    LanguageController.messages
                        .errorConfigurationFileTypeEntryNotATable
                )
                val extension = fileType["extension"] as? String
                    ?: throw AraraException(
                        LanguageController.messages
                            .errorConfigurationFileTypeExtensionMissing
                    )
                val pattern = fileType["pattern"]
                when {
                    pattern == null -> SerialFileType(extension)
                    pattern is String -> SerialFileType(extension, pattern)
                    // a wrongly typed pattern is never silently
                    // dropped: the rejection names the expectation,
                    // matching the project specification's own
                    // strictness
                    else -> throw AraraException(
                        LanguageController.messages
                            .errorConfigurationFileTypePatternNotAString
                    )
                }
            } ?: throw AraraException(
                LanguageController.messages
                    .errorConfigurationFileTypeKeyRequiresTableList
            )
        }
}
