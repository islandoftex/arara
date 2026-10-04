// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.configuration

import io.github.taoguan.luaj.LuaTable
import io.github.taoguan.luaj.LuaValue
import io.github.taoguan.luaj.lib.jse.JsePlatform
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.configuration.SerialFileType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.lua.rules.LuaConversion

/**
 * Parse configuration files written in arara's Lua configuration format.
 *
 * A configuration file is a Lua script returning a table with the same
 * keys as the YAML configuration format, spelled in Lua's snake_case
 * where the YAML names join words in camelCase or without a separator:
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
 *   logging = false,
 *   header = true,
 *   db_name = "arara",
 *   log_name = "arara",
 *   preambles = {
 *     ["twopdftex"] = "% arara: pdftex\n% arara: pdftex"
 *   },
 *   default_preamble = "twopdftex",
 *   prepend_preamble_if_directives_given = true
 * }
 * ```
 *
 * The script runs with arara's own values in scope, so paths and other
 * strings are computed in plain Lua instead of relying on a placeholder
 * syntax: the table `user` carries the `home` and `name` of the user
 * running arara, and `cwd` is the working directory the configuration
 * is loaded for. As a script evaluates once, at load, so do the values:
 * `cwd` is the working directory of that very moment.
 *
 * The parsed result is converted into the very same [LocalConfiguration]
 * representation the YAML parser produces, so both formats behave
 * identically once loaded.
 *
 * @author Island of TeX
 */
object LuaLocalConfiguration {
    /**
     * The configuration keys the Lua format accepts, in their
     * snake_case spellings; a key outside the set is an error.
     */
    private val knownKeys = setOf(
        "paths", "file_types", "language", "loops", "verbose", "logging",
        "header", "db_name", "log_name", "preambles", "console",
        "default_preamble", "prepend_preamble_if_directives_given",
        "animations"
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
            val globals = JsePlatform.standardGlobals()
            // arara's values are in scope to the script, mirroring what
            // the placeholder expansion of the YAML format resolves;
            // missing system properties fall back to empty strings so
            // concatenation never sees nil
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
                LuaValue.valueOf(workingDirectory.normalize().toString())
            )
            val result = globals.load(file.readText(), file.fileName).call()
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
     * model, rejecting every key the format does not accept.
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
        return LocalConfiguration(
            paths = stringList(map, "paths"),
            filetypes = fileTypes(map),
            language = string(map, "language"),
            loops = int(map, "loops"),
            verbose = boolean(map, "verbose"),
            logging = boolean(map, "logging"),
            header = boolean(map, "header"),
            dbname = string(map, "db_name"),
            logname = string(map, "log_name"),
            console = string(map, "console"),
            animations = boolean(map, "animations"),
            preambles = stringMap(map, "preambles"),
            defaultPreamble = string(map, "default_preamble"),
            prependPreambleIfDirectivesGiven =
                boolean(map, "prepend_preamble_if_directives_given") ?: true,
            // the script computed its paths with the values in scope,
            // so no engine tokens remain to expand downstream
            expandPathTokens = false
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
     * Extracts a map of string pairs, accepting the empty table -
     * which the Lua conversion yields as an empty list - and
     * rejecting every other shape.
     */
    private fun stringMap(
        map: Map<String, Any?>,
        key: String
    ): Map<String, String> =
        when (val value = map[key]) {
            null -> emptyMap()
            // an empty Lua table converts to an empty list
            is List<*> -> value.takeIf { it.isEmpty() }?.let { emptyMap() }
                ?: throw AraraException(
                    LanguageController.messages
                        .errorConfigurationKeyRequiresTable(key)
                )
            is Map<*, *> -> value.entries.associate { (k, v) ->
                (k as? String ?: throw AraraException(
                    LanguageController.messages
                        .errorConfigurationKeyRequiresStringKeys(key)
                )) to (v as? String ?: throw AraraException(
                    LanguageController.messages
                        .errorConfigurationKeyRequiresStringValues(key)
                ))
            }
            else -> throw AraraException(
                LanguageController.messages
                    .errorConfigurationKeyRequiresTable(key)
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
                    // dropped: the entry degrades to no pattern at
                    // all, and the type expectation is spoken
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
