// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaFunction
import io.github.taoguan.luaj.LuaTable
import io.github.taoguan.luaj.LuaValue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

/**
 * Parse rules written in arara's Lua rule format.
 *
 * A rule file is a Lua script returning a table of the following shape:
 *
 * ```lua
 * return {
 *   identifier = "pdflatex",
 *   name = "PDFLaTeX",
 *   authors = { "Island of TeX" },
 *   commands = {
 *     {
 *       name = "PDFLaTeX engine",
 *       command = function(ctx)
 *         return getCommand("pdflatex", ctx.interaction, ctx.options,
 *                           ctx.reference.fileName)
 *       end,
 *       -- optional, defaults to a zero exit code check
 *       exit = function(code) return code == 0 end
 *     }
 *   },
 *   arguments = {
 *     {
 *       identifier = "interaction",
 *       required = false,          -- optional, defaults to false
 *       default = "nonstopmode",   -- optional
 *       flag = function(ctx, value)
 *         return "--interaction=" .. value
 *       end
 *     }
 *   }
 * }
 * ```
 *
 * Command closures receive a context table holding the processed argument
 * values (lists of strings) and the `reference` (a table with `fileName`,
 * `path` and `parent` fields). Flag closures receive a context table with
 * the raw directive `parameters` and the `reference`, plus the user-provided
 * value of the argument as second parameter. The helper functions known
 * from the YAML format (`getCommand`, `isTrue`, `throwError`, ...) are
 * available as globals.
 *
 * @author Island of TeX
 * @version 8.0
 * @since 8.0
 */
object LuaRuleParser {
    /**
     * Parses the provided file, checks the identifier and returns a rule
     * representation.
     *
     * @param file The rule file.
     * @param identifier The directive identifier.
     * @return The rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun parse(file: MPPPath, identifier: String): LuaRule {
        val errorHeader = "I found an error in rule '$identifier' located " +
            "at '${file.normalize().parent}'. "
        val table = LuaConversion.withLuaErrorHandling {
            val globals = LuaRuleEnvironment.newGlobals()
            runCatching {
                globals.load(file.readText(), file.fileName).call()
            }.getOrElse {
                throw AraraException(
                    errorHeader + "The Lua rule could not be evaluated.",
                    it as? Exception ?: AraraException(it.toString())
                )
            } as? LuaTable ?: throw AraraException(
                errorHeader + "A Lua rule has to return a table."
            )
        }
        return extractRule(table, identifier, errorHeader)
    }

    @Suppress("ThrowsCount")
    private fun extractRule(
        table: LuaTable,
        identifier: String,
        errorHeader: String
    ): LuaRule {
        val ruleIdentifier = table.get("identifier")
            .takeIf { it.isstring() }?.tojstring()
            ?: throw AraraException(
                errorHeader + "The rule does not provide an identifier."
            )
        if (ruleIdentifier != identifier) {
            throw AraraException(
                errorHeader + "The rule identifier '$ruleIdentifier' does " +
                    "not match the directive identifier '$identifier'."
            )
        }
        val name = table.get("name")
            .takeIf { it.isstring() }?.tojstring()
            ?: throw AraraException(
                errorHeader + "The rule does not provide a name."
            )
        val authors = stringList(table.get("authors"))
        val commands = tableList(table.get("commands")).map { command ->
            extractCommand(command, errorHeader)
        }.ifEmpty {
            throw AraraException(
                errorHeader + "The rule does not provide any command."
            )
        }
        val arguments = tableList(table.get("arguments")).map { argument ->
            extractArgument(argument, errorHeader)
        }
        validateArguments(arguments, errorHeader)
        return LuaRule(ruleIdentifier, name, authors, commands, arguments)
    }

    private fun extractCommand(
        command: LuaTable,
        errorHeader: String
    ): LuaRuleCommand = LuaRuleCommand(
        name = command.get("name").takeIf { it.isstring() }?.tojstring(),
        commandFunction = command.get("command") as? LuaFunction
            ?: throw AraraException(
                errorHeader + "Every command requires a 'command' function."
            ),
        exitFunction = command.get("exit") as? LuaFunction
    )

    @Suppress("ThrowsCount")
    private fun extractArgument(
        argument: LuaTable,
        errorHeader: String
    ): LuaRuleArgument {
        val identifier = argument.get("identifier")
            .takeIf { it.isstring() }?.tojstring()?.takeIf { it.isNotBlank() }
            ?: throw AraraException(
                errorHeader + "Every argument requires a non-blank " +
                    "'identifier'."
            )
        val flag = argument.get("flag") as? LuaFunction
        val default = argument.get("default")
            .takeUnless { it.isnil() }
            ?.let { LuaConversion.fromLua(it) }
        if (flag == null && default == null) {
            throw AraraException(
                errorHeader + "The argument '$identifier' requires at " +
                    "least one of 'flag' or 'default'."
            )
        }
        return LuaRuleArgument(
            identifier = identifier,
            isRequired = argument.get("required").toboolean(),
            defaultValue = default,
            flagFunction = flag
        )
    }

    @Suppress("ThrowsCount")
    private fun validateArguments(
        arguments: List<LuaRuleArgument>,
        errorHeader: String
    ) {
        val identifiers = arguments.map { it.identifier }
        identifiers.intersect(setOf("files", "reference")).forEach {
            throw AraraException(
                errorHeader + "The argument identifier '$it' is reserved."
            )
        }
        if (identifiers.size != identifiers.toSet().size) {
            throw AraraException(
                errorHeader + "Duplicate argument identifiers are not allowed."
            )
        }
    }

    private fun stringList(value: LuaValue): List<String> =
        (value as? LuaTable)?.keys()
            ?.mapNotNull { key ->
                value.get(key).takeIf { it.isstring() }?.tojstring()
            } ?: emptyList()

    private fun tableList(value: LuaValue): List<LuaTable> =
        (value as? LuaTable)?.keys()
            ?.mapNotNull { key -> value.get(key) as? LuaTable }
            ?: emptyList()
}
