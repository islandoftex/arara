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
    /** Identifier used to locate this rule. */
    override val identifier: String,
    /** Optional human-readable rule name. */
    override val displayName: String?,
    /** Authors credited by the rule. */
    override val authors: List<String>,
    /** Commands evaluated in declaration order. */
    override val commands: List<LuaRuleCommand>,
    /** Accepted directive parameters and their processors. */
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
    /** Optional human-readable task name. */
    override val name: String?,
    private val commandFunction: LuaFunction,
    private val exitFunction: LuaFunction?
) : RuleCommand, CommandEvaluator {
    /** Apply the optional Lua exit predicate; without one, only exit code zero succeeds. */
    override fun isSuccessful(exitCode: Int): Boolean {
        val code = exitCode
        return exitFunction?.let { fn ->
            LuaFailures.withLuaErrorHandling(
                LanguageController.messages
                    .errorInterpreterExitRuntimeError
            ) {
                LuaResults.booleanResult(
                    fn.call(LuaValue.valueOf(code)),
                    "exit"
                )
            }
        } ?: (code == 0)
    }

    /** Evaluate the Lua command closure with processed parameters and reference metadata. */
    @Throws(AraraException::class)
    override fun evaluate(parameters: Map<String, Any>): List<Any> =
        LuaFailures.withLuaErrorHandling(
            LanguageController.messages
                .errorInterpreterCommandRuntimeError
        ) {
            val context = LuaValue.tableOf()
            parameters.forEach { (key, value) ->
                if (key == "reference" && value is MPPPath) {
                    context.set(key, LuaValues.referenceToLua(value))
                } else {
                    context.set(key, LuaConversion.toLua(value))
                }
            }
            LuaResults.commandResultToList(commandFunction.call(context))
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
    /** Directive parameter name. */
    override val identifier: String,
    /** Whether the directive must supply this argument explicitly. */
    override val isRequired: Boolean,
    /** Literal default used when an optional argument is omitted. */
    override val defaultValue: Any?,
    private val flagFunction: LuaFunction?
) : RuleArgument<Any?> {
    /** Convert a value to flag strings using the optional Lua flag closure and raw parameter context. */
    override val processor: (Any?, Map<String, Any>) -> List<String> =
        { input, context ->
            LuaFailures.withLuaErrorHandling(
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
                            LuaValues.referenceToLua(reference)
                        )
                    }
                    LuaResults.flagResultToList(
                        fn.call(luaContext, LuaConversion.toLua(input))
                    )
                } ?: listOf(input.toString())
            }
        }

    /** Describe argument metadata for diagnostics, without executing the flag closure. */
    override fun toString(): String =
        "LuaRuleArgument(identifier='$identifier', isRequired=$isRequired, " +
            "defaultValue=$defaultValue)"
}
