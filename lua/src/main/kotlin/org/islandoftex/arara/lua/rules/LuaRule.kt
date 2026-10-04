// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaFunction
import io.github.taoguan.luaj.LuaValue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.rules.RuleCommand
import org.islandoftex.arara.core.localization.LanguageController

/**
 * A rule backed by a Lua specification (see [LuaRuleParser] for the format).
 *
 * @author Island of TeX
 */
class LuaRule(
    /**
     * The identifier directives use to ask for this rule.
     */
    override val identifier: String,
    /**
     * The name the rule displays itself with.
     */
    override val displayName: String?,
    /**
     * The authors credited for the rule.
     */
    override val authors: List<String>,
    /**
     * The commands of the rule, in order.
     */
    override val commands: List<LuaRuleCommand>,
    /**
     * The arguments the rule declares.
     */
    override val arguments: List<LuaRuleArgument>
) : Rule

/**
 * A rule command backed by a Lua closure.
 *
 * The closure receives a context table with the resolved rule arguments
 * and the `reference` and returns a command (from `getCommand`), a boolean
 * or a list thereof.
 */
class LuaRuleCommand(
    /**
     * The name the command entry displays itself with, if any.
     */
    override val name: String?,
    /**
     * The closure the command evaluates against the resolved
     * arguments.
     */
    private val commandFunction: LuaFunction,
    /**
     * The optional closure checking the exit code of the
     * command's tasks.
     */
    private val exitFunction: LuaFunction?
) : RuleCommand {
    /**
     * Always unsupported: the command entry evaluates through the Lua
     * function instead of a closure.
     */
    override val command: () -> Int
        get() = throw AraraException(
            "Operation not supported for LuaRuleCommand"
        )

    /**
     * Check the exit code of a system command against the rule's `exit`
     * closure (all exit codes but zero indicate failure by default).
     */
    val exitCheck: (Int) -> Boolean = { code ->
        exitFunction?.let { fn ->
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

    /**
     * Evaluate the command closure against the resolved [parameters]
     * (the argument identifiers mapped to their processed values plus the
     * `reference` path).
     *
     * @return The flat list of objects the closure evaluated to.
     * @throws AraraException The evaluation failed.
     */
    @Throws(AraraException::class)
    fun evaluate(parameters: Map<String, Any>): List<Any> =
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
 * A rule argument backed by an optional Lua flag closure.
 *
 * The closure receives a context table (with the raw directive parameters
 * and the `reference`) and the value the user provided for this argument;
 * it returns a string or a list of strings.
 */
class LuaRuleArgument(
    /**
     * The identifier directives use to pass this argument.
     */
    override val identifier: String,
    /**
     * Whether the argument must be present in every directive.
     */
    override val isRequired: Boolean,
    /**
     * The value the argument takes when the directive omits it.
     */
    override val defaultValue: Any?,
    /**
     * The optional closure rendering the argument's flags from
     * the user-provided value.
     */
    private val flagFunction: LuaFunction?
) : RuleArgument<Any?> {
    /**
     * Evaluates the flag function of the argument against the input
     * value, returning the resulting list of flags.
     */
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

    /**
     * A human-readable representation of the argument, for logs and
     * error reports.
     */
    override fun toString(): String =
        "LuaRuleArgument(identifier='$identifier', isRequired=$isRequired, " +
            "defaultValue=$defaultValue)"
}
