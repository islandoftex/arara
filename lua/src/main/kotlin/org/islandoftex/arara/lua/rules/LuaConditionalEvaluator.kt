// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaError
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditionalEvaluator

/**
 * Implements an evaluator for Lua directive conditionals, used when the
 * Lua rule engine is selected on the command line.
 *
 * The condition is evaluated as a Lua expression and, like the MVEL
 * dialect, has to yield a proper boolean value; anything else is
 * rejected. Single-call conditions such as `exists('log')` work in both
 * dialects unchanged, but compound conditions use Lua's `and`, `or` and
 * `not` instead of `&&`, `||` and `!`.
 *
 * @author Island of TeX
 * @version 8.0
 * @since 8.0
 */
class LuaConditionalEvaluator(
    executionOptions: org.islandoftex.arara.api.configuration.ExecutionOptions
) : DirectiveConditionalEvaluator(executionOptions) {

    /**
     * Only run the evaluation of the conditional including a check whether
     * the result needs to be inverted.
     *
     * @param conditional The conditional.
     * @return The result of the evaluation.
     */
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught")
    override fun evaluateCondition(conditional: DirectiveConditional): Boolean {
        val result = try {
            LuaConditionalEnvironment.newGlobals()
                .load("return (" + conditional.condition + ")")
                .call()
        } catch (exception: RuntimeException) {
            // Lua errors and helper binding errors cross the Lua VM as
            // LuaError (a runtime exception) with the original exception
            // preserved in the cause chain, so a single runtime catch
            // wraps both behind the localized evaluation error, with
            // the binding message available as the cause
            throw AraraException(
                LanguageController.messages.ERROR_EVALUATE_COMPILATION_FAILED,
                exception
            )
        }
        return if (result.isboolean()) {
            val value = result.toboolean()
            if (conditional.type == DirectiveConditionalType.UNLESS ||
                conditional.type == DirectiveConditionalType.UNTIL
            )
                !value
            else
                value
        } else {
            throw AraraException(
                LanguageController.messages.ERROR_EVALUATE_NOT_BOOLEAN_VALUE
            )
        }
    }
}
