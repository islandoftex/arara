// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.luaj.vm2.LuaBoolean
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

/** Parses scripts returning a rule table with command and flag closures. */
object LuaRuleParser {
    fun parse(file: MPPPath, identifier: String): LuaRule {
        val header = LanguageController.messages.ERROR_RULE_IDENTIFIER_AND_PATH
            .format(identifier, file.normalize().parent.toString())
        return LuaConversion.withLuaErrorHandling(header) {
            val value = LuaRuleEnvironment.newGlobals().load(file.readText(), file.toString()).call()
            val table = value as? LuaTable ?: invalid("A rule must return a table.")
            extract(table, identifier)
        }
    }

    private fun extract(table: LuaTable, identifier: String): LuaRule {
        val actual = string(table.get("identifier"), "identifier")
        if (actual != identifier) invalid("Expected identifier '$identifier', got '$actual'.")
        val name = string(table.get("name"), "name")
        val authors = sequence(table.get("authors"), "authors").map { string(it, "author") }
        val commands = sequence(table.get("commands"), "commands").map { command ->
            val entry = command as? LuaTable ?: invalid("Each command must be a table.")
            LuaRuleCommand(
                optionalString(entry.get("name"), "command name"),
                function(entry.get("command"), "command") ?: invalid("Missing command function."),
                function(entry.get("exit"), "exit")
            )
        }
        if (commands.isEmpty()) invalid("At least one command is required.")
        val arguments = sequence(table.get("arguments"), "arguments").map { argument(it) }
        val identifiers = arguments.map { it.identifier }
        if (identifiers.any { it in setOf("files", "reference") }) invalid("Reserved argument identifier.")
        if (identifiers.distinct().size != identifiers.size) invalid("Duplicate argument identifiers.")
        return LuaRule(identifier, name, authors, commands, arguments)
    }

    private fun argument(value: LuaValue): LuaRuleArgument {
        val entry = value as? LuaTable ?: invalid("Each argument must be a table.")
        val identifier = string(entry.get("identifier"), "argument identifier")
        if (identifier.isBlank()) invalid("Argument identifier must not be blank.")
        val flag = function(entry.get("flag"), "flag")
        val default = entry.get("default")
        if (default.isfunction()) invalid("Argument defaults must be values, not functions.")
        if (flag == null && default.isnil()) invalid("Argument '$identifier' needs a flag or default.")
        val required = entry.get("required").let {
            when (it) {
                LuaValue.NIL -> false
                is LuaBoolean -> it.toboolean()
                else -> invalid("Argument '$identifier': required must be boolean.")
            }
        }
        return LuaRuleArgument(identifier, required, LuaConversion.fromLua(default), flag)
    }

    private fun function(value: LuaValue, name: String): LuaFunction? = when {
        value.isnil() -> null
        value is LuaFunction -> value
        else -> invalid("'$name' must be a function.")
    }

    private fun optionalString(value: LuaValue, name: String): String? =
        if (value.isnil()) null else string(value, name)

    private fun string(value: LuaValue, name: String): String =
        (value as? LuaString)?.tojstring() ?: invalid("'$name' must be a string.")

    private fun sequence(value: LuaValue, name: String): List<LuaValue> {
        if (value.isnil()) return emptyList()
        val table = value as? LuaTable ?: invalid("'$name' must be a list.")
        val keys = table.keys()
        if (keys.any { !it.isint() || it.toint() !in 1..keys.size }) {
            invalid("'$name' must have consecutive integer keys starting at 1.")
        }
        return (1..keys.size).map { table.get(it) }
    }

    private fun invalid(detail: String): Nothing = throw LuaHelperException(detail)
}
