// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditionalEvaluator
import org.mvel2.templates.TemplateRuntime

/**
 * Implements the evaluator for the MVEL dialect of directive
 * conditionals.
 */
class DirectiveConditionalEvaluator(executionOptions: ExecutionOptions) :
    DirectiveConditionalEvaluator(executionOptions) {

    /**
     * Run the evaluation of the conditional, inverting the result
     * for the negated types.
     *
     * @param conditional The conditional.
     * @return The result of the evaluation.
     */
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught")
    override fun evaluateCondition(conditional: DirectiveConditional): Boolean {
        try {
            val result = TemplateRuntime.eval(
                "@{ ${conditional.condition} }",
                MvelState.conditionalMethods
            )
            return if (result is Boolean) {
                if (conditional.type == DirectiveConditionalType.UNLESS ||
                    conditional.type == DirectiveConditionalType.UNTIL
                )
                    !result
                else
                    result
            } else {
                throw AraraException(
                    LanguageController.messages.errorEvaluateNotBooleanValue
                )
            }
        } catch (exception: RuntimeException) {
            throw AraraException(
                LanguageController.messages.errorEvaluateCompilationFailed,
                exception
            )
        }
    }
}
