// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.rules.RuleCommand
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.CommandEvaluator
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue

/** A rule parsed from a Lua table by [LuaRuleParser]. */
class LuaRule(
    override val identifier: String,
    override val displayName: String?,
    override val authors: List<String>,
    override val commands: List<LuaRuleCommand>,
    override val arguments: List<LuaRuleArgument>
) : Rule

/**
 * Evaluates a Lua command closure through [CommandEvaluator].
 *
 * The closure receives processed argument lists and a `reference` table with
 * string fields `file_name`, `path` and `parent`. It returns command handles,
 * booleans or nested lists of either. An optional exit closure accepts a process
 * exit code and must return a boolean; without one, only zero indicates success.
 */
class LuaRuleCommand(
    override val name: String?,
    private val commandFunction: LuaFunction,
    private val exitFunction: LuaFunction?
) : RuleCommand, CommandEvaluator {
    override fun isSuccessful(exitCode: Int): Boolean {
        val code = exitCode
        return exitFunction?.let { fn ->
            LuaConversion.withLuaErrorHandling(
                LanguageController.messages
                    .errorInterpreterExitRuntimeError
            ) {
                LuaConversion.booleanResult(
                    fn.call(LuaValue.valueOf(code)),
                    "exit"
                )
            }
        } ?: (code == 0)
    }

    @Throws(AraraException::class)
    override fun evaluate(parameters: Map<String, Any>): List<Any> =
        LuaConversion.withLuaErrorHandling(
            LanguageController.messages
                .errorInterpreterCommandRuntimeError
        ) {
            val context = LuaValue.tableOf()
            parameters.forEach { (key, value) ->
                if (key == "reference" && value is MPPPath) {
                    context.set(key, LuaConversion.referenceToLua(value))
                } else {
                    context.set(key, LuaConversion.toLua(value))
                }
            }
            LuaConversion.commandResultToList(commandFunction.call(context))
        }
}

/**
 * A rule argument with a literal default and an optional flag closure.
 *
 * The flag receives `(ctx, value)`, where `ctx.parameters` contains the raw
 * directive parameters (including this argument's value) and `ctx.reference`
 * contains string fields `file_name`, `path` and `parent`. Its result becomes
 * a list of flag strings. Without a flag closure, the input is converted to
 * a single string. Required arguments must be supplied explicitly.
 */
class LuaRuleArgument(
    override val identifier: String,
    override val isRequired: Boolean,
    override val defaultValue: Any?,
    private val flagFunction: LuaFunction?
) : RuleArgument<Any?> {
    override val processor: (Any?, Map<String, Any>) -> List<String> =
        { input, context ->
            LuaConversion.withLuaErrorHandling(
                LanguageController.messages
                    .errorInterpreterFlagRuntimeException
            ) {
                flagFunction?.let { fn ->
                    val parameters = (context["parameters"] as? Map<*, *>)
                        .orEmpty()
                        .map { (key, value) -> key.toString() to value }
                        .toMap()
                        .plus(identifier to input)
                    val luaContext = LuaValue.tableOf()
                    luaContext.set(
                        "parameters",
                        LuaConversion.toLua(parameters)
                    )
                    (context["reference"] as? MPPPath)?.let { reference ->
                        luaContext.set(
                            "reference",
                            LuaConversion.referenceToLua(reference)
                        )
                    }
                    LuaConversion.flagResultToList(
                        fn.call(luaContext, LuaConversion.toLua(input))
                    )
                } ?: listOf(input.toString())
            }
        }

    override fun toString(): String =
        "LuaRuleArgument(identifier='$identifier', isRequired=$isRequired, " +
            "defaultValue=$defaultValue)"
}
