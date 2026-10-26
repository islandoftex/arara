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

/**
 * Parses scripts returning a rule table with command and flag closures.
 *
 * Tables require an `identifier`, a `name` and at least one command; `authors`
 * and `arguments` are optional lists. Argument identifiers must be unique and
 * cannot be `files` or `reference`. Each argument needs a literal `default`,
 * a `flag` function or both; `required` defaults to false.
 */
object LuaRuleParser {
    /**
     * Reads and evaluates [file], requiring its identifier to match [identifier].
     *
     * @param scriptOutputToStderr Route script output to diagnostics, preserving structured caller output.
     * @throws AraraException If the script or rule table is invalid.
     */
    fun parse(file: MPPPath, identifier: String, scriptOutputToStderr: Boolean = false): LuaRule {
        val header = LanguageController.messages.errorRuleIdentifierAndPath(identifier, file.normalize().parent.toString())
        return LuaConversion.withLuaErrorHandling(header) {
            val globals = LuaRuleEnvironment.newGlobals()
            if (scriptOutputToStderr) globals.STDOUT = globals.STDERR
            val value = globals.load(file.readText(), file.toString()).call()
            val table = value as? LuaTable ?: invalid(messages.errorScriptTable("rule"))
            extract(table, identifier)
        }
    }

    private fun extract(table: LuaTable, identifier: String): LuaRule {
        val actual = string(table.get("identifier"), "identifier")
        if (actual != identifier) invalid(
            messages.errorValidateHeaderWrongIdentifier(identifier, actual)
        )
        val name = string(table.get("name"), "name")
        val authors = sequence(table.get("authors"), "authors").map { string(it, "author") }
        val commands = sequence(table.get("commands"), "commands").mapIndexed { index, command ->
            val context = "commands[${index + 1}]"
            val entry = command as? LuaTable ?: invalid(messages.errorScriptTable(context))
            LuaRuleCommand(
                optionalString(entry.get("name"), "$context.name"),
                function(entry.get("command"), "$context.command")
                    ?: invalid(messages.errorScriptRequired("$context.command")),
                function(entry.get("exit"), "$context.exit")
            )
        }
        if (commands.isEmpty()) invalid(messages.errorScriptEmpty("commands"))
        val arguments = sequence(table.get("arguments"), "arguments").mapIndexed { index, value ->
            argument(value, "arguments[${index + 1}]")
        }
        val identifiers = arguments.map { it.identifier }
        identifiers.firstOrNull { it in setOf("files", "reference") }?.let {
            invalid(messages.errorValidateBodyArgumentIdIsReserved(it))
        }
        if (identifiers.distinct().size != identifiers.size) invalid(
            messages.errorValidateBodyDuplicateArgumentIdentifiers
        )
        return LuaRule(identifier, name, authors, commands, arguments)
    }

    private fun argument(value: LuaValue, context: String): LuaRuleArgument {
        val entry = value as? LuaTable ?: invalid(messages.errorScriptTable(context))
        val identifier = string(entry.get("identifier"), "$context.identifier")
        if (identifier.isBlank()) invalid(messages.errorScriptNonblank("$context.identifier"))
        val flag = function(entry.get("flag"), "$context.flag")
        val default = entry.get("default")
        if (default.isfunction()) invalid(messages.errorScriptLiteral("$context.default"))
        if (flag == null && default.isnil()) invalid(
            messages.errorScriptAlternatives(context, "flag, default")
        )
        val required = entry.get("required").let {
            when (it) {
                LuaValue.NIL -> false
                is LuaBoolean -> it.toboolean()
                else -> invalid(messages.errorScriptBoolean("$context.required"))
            }
        }
        return LuaRuleArgument(identifier, required, LuaConversion.fromLua(default), flag)
    }

    private fun function(value: LuaValue, name: String): LuaFunction? = when {
        value.isnil() -> null
        value is LuaFunction -> value
        else -> invalid(messages.errorScriptFunction(name))
    }

    private fun optionalString(value: LuaValue, name: String): String? =
        if (value.isnil()) null else string(value, name)

    private fun string(value: LuaValue, name: String): String =
        (value as? LuaString)?.tojstring() ?: invalid(messages.errorScriptString(name))

    private fun sequence(value: LuaValue, name: String): List<LuaValue> {
        if (value.isnil()) return emptyList()
        val table = value as? LuaTable ?: invalid(messages.errorScriptList(name))
        val keys = table.keys()
        if (keys.any { !it.isint() || it.toint() !in 1..keys.size }) {
            invalid(messages.errorScriptList(name))
        }
        return (1..keys.size).map { table.get(it) }
    }

    private fun invalid(detail: String): Nothing = throw LuaHelperException(detail)
}

private val messages get() = LanguageController.messages
