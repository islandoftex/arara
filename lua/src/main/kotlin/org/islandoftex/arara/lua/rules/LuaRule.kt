// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaFunction
import io.github.taoguan.luaj.LuaValue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.rules.RuleCommand

/**
 * A rule backed by a Lua specification (see [LuaRuleParser] for the format).
 *
 * @author Island of TeX
 * @version 8.0
 * @since 8.0
 */
class LuaRule(
    override val identifier: String,
    override val displayName: String?,
    override val authors: List<String>,
    override val commands: List<LuaRuleCommand>,
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
    override val name: String?,
    private val commandFunction: LuaFunction,
    private val exitFunction: LuaFunction?
) : RuleCommand {
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
            LuaConversion.withLuaErrorHandling {
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
        LuaConversion.withLuaErrorHandling {
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
    override val identifier: String,
    override val isRequired: Boolean,
    override val defaultValue: Any?,
    private val flagFunction: LuaFunction?
) : RuleArgument<Any?> {
    override val processor: (Any?, Map<String, Any>) -> List<String> =
        { input, context ->
            LuaConversion.withLuaErrorHandling {
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
