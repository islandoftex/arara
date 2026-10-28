// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditionalEvaluator
import org.luaj.vm2.LuaBoolean

/**
 * Evaluates boolean Lua expressions using the shared scheduling and loop limits.
 * `unless` and `until` invert the result; dry runs skip evaluation. Compound
 * expressions use Lua's `and`, `or` and `not` operators.
 */
class LuaConditionalEvaluator(executionOptions: ExecutionOptions) : DirectiveConditionalEvaluator(executionOptions) {
    /** Evaluate the expression as a strict Lua boolean, inverting unless and until results. */
    override fun evaluateCondition(conditional: DirectiveConditional): Boolean {
        val result = LuaFailures.withLuaErrorHandling(LanguageController.messages.errorEvaluateCompilationFailed) {
            LuaConditionalEnvironment.newGlobals()
                .load("return (" + conditional.condition + ")", "directive-conditional").call()
        }
        val value = (result as? LuaBoolean)?.toboolean()
            ?: throw AraraException(LanguageController.messages.errorEvaluateNotBooleanValue)
        return if (conditional.type in setOf(DirectiveConditionalType.UNLESS, DirectiveConditionalType.UNTIL)) {
            !value
        } else value
    }
}
