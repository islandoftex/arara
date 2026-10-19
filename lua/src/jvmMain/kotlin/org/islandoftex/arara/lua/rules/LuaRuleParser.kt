// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.utils.formatString
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
     * @throws AraraException If the script or rule table is invalid.
     */
    fun parse(file: MPPPath, identifier: String): LuaRule {
        val header = LanguageController.messages.ERROR_RULE_IDENTIFIER_AND_PATH
            .formatString(identifier, file.normalize().parent.toString())
        return LuaConversion.withLuaErrorHandling(header) {
            val value = LuaRuleEnvironment.newGlobals().load(file.readText(), file.toString()).call()
            val table = value as? LuaTable ?: invalid(messages.ERROR_SCRIPT_TABLE.formatString("rule"))
            extract(table, identifier)
        }
    }

    private fun extract(table: LuaTable, identifier: String): LuaRule {
        val actual = string(table.get("identifier"), "identifier")
        if (actual != identifier) invalid(
            messages.ERROR_VALIDATEHEADER_WRONG_IDENTIFIER.formatString(identifier, actual)
        )
        val name = string(table.get("name"), "name")
        val authors = sequence(table.get("authors"), "authors").map { string(it, "author") }
        val commands = sequence(table.get("commands"), "commands").mapIndexed { index, command ->
            val context = "commands[${index + 1}]"
            val entry = command as? LuaTable ?: invalid(messages.ERROR_SCRIPT_TABLE.formatString(context))
            LuaRuleCommand(
                optionalString(entry.get("name"), "$context.name"),
                function(entry.get("command"), "$context.command")
                    ?: invalid(messages.ERROR_SCRIPT_REQUIRED.formatString("$context.command")),
                function(entry.get("exit"), "$context.exit")
            )
        }
        if (commands.isEmpty()) invalid(messages.ERROR_SCRIPT_EMPTY.formatString("commands"))
        val arguments = sequence(table.get("arguments"), "arguments").mapIndexed { index, value ->
            argument(value, "arguments[${index + 1}]")
        }
        val identifiers = arguments.map { it.identifier }
        identifiers.firstOrNull { it in setOf("files", "reference") }?.let {
            invalid(messages.ERROR_VALIDATEBODY_ARGUMENT_ID_IS_RESERVED.formatString(it))
        }
        if (identifiers.distinct().size != identifiers.size) invalid(
            messages.ERROR_VALIDATEBODY_DUPLICATE_ARGUMENT_IDENTIFIERS
        )
        return LuaRule(identifier, name, authors, commands, arguments)
    }

    private fun argument(value: LuaValue, context: String): LuaRuleArgument {
        val entry = value as? LuaTable ?: invalid(messages.ERROR_SCRIPT_TABLE.formatString(context))
        val identifier = string(entry.get("identifier"), "$context.identifier")
        if (identifier.isBlank()) invalid(messages.ERROR_SCRIPT_NONBLANK.formatString("$context.identifier"))
        val flag = function(entry.get("flag"), "$context.flag")
        val default = entry.get("default")
        if (default.isfunction()) invalid(messages.ERROR_SCRIPT_LITERAL.formatString("$context.default"))
        if (flag == null && default.isnil()) invalid(
            messages.ERROR_SCRIPT_ALTERNATIVES.formatString(context, "flag, default")
        )
        val required = entry.get("required").let {
            when (it) {
                LuaValue.NIL -> false
                is LuaBoolean -> it.toboolean()
                else -> invalid(messages.ERROR_SCRIPT_BOOLEAN.formatString("$context.required"))
            }
        }
        return LuaRuleArgument(identifier, required, LuaConversion.fromLua(default), flag)
    }

    private fun function(value: LuaValue, name: String): LuaFunction? = when {
        value.isnil() -> null
        value is LuaFunction -> value
        else -> invalid(messages.ERROR_SCRIPT_FUNCTION.formatString(name))
    }

    private fun optionalString(value: LuaValue, name: String): String? =
        if (value.isnil()) null else string(value, name)

    private fun string(value: LuaValue, name: String): String =
        (value as? LuaString)?.tojstring() ?: invalid(messages.ERROR_SCRIPT_STRING.formatString(name))

    private fun sequence(value: LuaValue, name: String): List<LuaValue> {
        if (value.isnil()) return emptyList()
        val table = value as? LuaTable ?: invalid(messages.ERROR_SCRIPT_LIST.formatString(name))
        val keys = table.keys()
        if (keys.any { !it.isint() || it.toint() !in 1..keys.size }) {
            invalid(messages.ERROR_SCRIPT_LIST.formatString(name))
        }
        return (1..keys.size).map { table.get(it) }
    }

    private fun invalid(detail: String): Nothing = throw LuaHelperException(detail)
}

private val messages get() = LanguageController.messages
