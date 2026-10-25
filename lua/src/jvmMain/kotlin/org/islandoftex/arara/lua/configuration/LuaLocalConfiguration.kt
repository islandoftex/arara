// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.configuration

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.configuration.SerialFileType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.lua.LuaScriptEnvironment
import org.islandoftex.arara.lua.rules.LuaConversion
import org.luaj.vm2.LuaBoolean
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaNumber
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

/**
 * Reads Lua scripts returning configuration tables. Scalar settings and lists live
 * at the root; logging and preambles use `log` and `preamble` tables. Unknown keys,
 * coercions and noncontiguous lists are rejected. Paths are computed in Lua;
 * YAML placeholder syntax is not expanded.
 */
object LuaLocalConfiguration {
    /** Evaluates [file] once with `user.home`, `user.name` and `cwd` in scope. */
    fun load(file: MPPPath, workingDirectory: MPPPath): LocalConfiguration = runCatching {
        val value = LuaScriptEnvironment.newGlobals(workingDirectory).load(file.readText(), file.toString()).call()
        extract(Fields.table(value, "configuration"))
    }.getOrElse {
        val cause = when (it) {
            is LuaError -> Exception(LuaConversion.diagnostic(it), it)
            is Exception -> it
            else -> RuntimeException(it)
        }
        throw AraraException("${messages.errorConfigurationGenericError} ($file)", cause)
    }

    private fun extract(table: LuaTable): LocalConfiguration {
        Fields.keys(table, setOf(
            "paths", "file_types", "language", "console", "animations", "loops", "verbose", "header",
            "database", "log", "preamble", "warn_deprecated_yaml"
        ), "configuration")
        val log = Fields.group(table, "log", setOf("file", "format"))
        val preamble = Fields.group(table, "preamble", setOf("default", "prepend", "definitions"))
        val loops = Fields.integer(table["loops"], "loops")
        require(loops == null || loops > 0) { messages.errorConfigurationLoopsInvalidRange }
        val prepend = when (val mode = Fields.string(preamble["prepend"], "preamble.prepend")) {
            null, "always" -> true
            "without-directives" -> false
            else -> error(messages.errorConfigurationPrepend(mode))
        }
        return LocalConfiguration(
            paths = Fields.strings(table["paths"], "paths"),
            filetypes = fileTypes(table["file_types"]),
            language = Fields.string(table["language"], "language"),
            console = Fields.string(table["console"], "console"),
            animations = Fields.boolean(table["animations"], "animations"),
            loops = loops,
            verbose = Fields.boolean(table["verbose"], "verbose"),
            header = Fields.boolean(table["header"], "header"),
            warnDeprecatedYaml = Fields.boolean(table["warn_deprecated_yaml"], "warn_deprecated_yaml"),
            dbname = Fields.string(table["database"], "database"),
            logname = Fields.string(log["file"], "log.file"),
            logformat = Fields.string(log["format"], "log.format"),
            preambles = definitions(preamble["definitions"]),
            defaultPreamble = Fields.string(preamble["default"], "preamble.default"),
            prependPreambleIfDirectivesGiven = prepend,
            expandPathTokens = false
        )
    }

    private fun fileTypes(value: LuaValue): List<SerialFileType> = Fields.list(value, "file_types")
        .mapIndexed { index, entry ->
            val context = "file_types[${index + 1}]"
            val type = Fields.table(entry, context)
            Fields.keys(type, setOf("extension", "pattern"), context)
            val extension = Fields.string(type["extension"], "$context.extension")
            require(!extension.isNullOrBlank()) { messages.errorScriptNonblank("$context.extension") }
            val pattern = Fields.string(type["pattern"], "$context.pattern")
            val result = if (pattern == null) SerialFileType(extension) else SerialFileType(extension, pattern)
            result.toFileType()
            result
        }

    private fun definitions(value: LuaValue): Map<String, String> {
        if (value.isnil()) return emptyMap()
        val table = Fields.table(value, "preamble.definitions")
        return table.keys().associate { key ->
            require(key is LuaString && key.tojstring().isNotBlank()) {
                messages.errorScriptStringKeys("preamble.definitions")
            }
            key.tojstring() to Fields.strings(table[key], "preamble.definitions.${key.tojstring()}")
                .joinToString("\n")
        }
    }
}

private object Fields {
    fun table(value: LuaValue, context: String): LuaTable =
        value as? LuaTable ?: error(messages.errorScriptTable(context))

    fun keys(table: LuaTable, allowed: Set<String>, context: String) {
        table.keys().firstOrNull { it !is LuaString || it.tojstring() !in allowed }?.let {
            error(messages.errorScriptUnknownKey("$context.${it.tojstring()}"))
        }
    }

    fun group(parent: LuaTable, name: String, allowed: Set<String>): LuaTable {
        val value = parent[name]
        val table = if (value.isnil()) LuaTable() else table(value, name)
        keys(table, allowed, name)
        return table
    }

    fun string(value: LuaValue, context: String): String? = when (value) {
        LuaValue.NIL -> null
        is LuaString -> value.tojstring()
        else -> error(messages.errorScriptString(context))
    }

    fun boolean(value: LuaValue, context: String): Boolean? = when (value) {
        LuaValue.NIL -> null
        is LuaBoolean -> value.toboolean()
        else -> error(messages.errorScriptBoolean(context))
    }

    fun integer(value: LuaValue, context: String): Int? = when {
        value.isnil() -> null
        value is LuaNumber && value.isint() -> value.toint()
        else -> error(messages.errorScriptInteger(context))
    }

    fun list(value: LuaValue, context: String): List<LuaValue> {
        if (value.isnil()) return emptyList()
        val table = table(value, context)
        val size = table.keys().size
        require(table.keys().all { it is LuaNumber && it.isint() && it.toint() in 1..size }) {
            messages.errorScriptList(context)
        }
        return (1..size).map { table[it] }
    }

    fun strings(value: LuaValue, context: String): List<String> = list(value, context).mapIndexed { index, entry ->
        string(entry, "$context[${index + 1}]") ?: error(
            messages.errorScriptString("$context[${index + 1}]")
        )
    }
}

private val messages get() = LanguageController.messages
