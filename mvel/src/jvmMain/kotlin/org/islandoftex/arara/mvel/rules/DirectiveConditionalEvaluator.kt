// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditionalEvaluator
import org.islandoftex.arara.mvel.interpreter.withMvelErrorHandling
import org.islandoftex.arara.mvel.utils.MvelState
import org.mvel2.templates.TemplateRuntime

/**
 * Implements an evaluator for MVEL rule conditionals.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class DirectiveConditionalEvaluator(executionOptions: ExecutionOptions) :
    DirectiveConditionalEvaluator(executionOptions) {

    /**
     * Only run the evaluation of the conditional including a check whether
     * the result needs to be inverted.
     * @param conditional The conditional.
     * @return The result of the evaluation.
     */
    @Throws(AraraException::class)
    override fun evaluateCondition(conditional: DirectiveConditional): Boolean = withMvelErrorHandling(
        message = { LanguageController.messages.errorEvaluateCompilationFailed }
    ) {
        val result = TemplateRuntime.eval(
            "@{ ${conditional.condition} }", MvelState.conditionalMethods
        ) as? Boolean ?: throw AraraException(LanguageController.messages.errorEvaluateNotBooleanValue)
        when (conditional.type) {
            DirectiveConditionalType.UNLESS, DirectiveConditionalType.UNTIL -> !result
            else -> result
        }
    }
}
