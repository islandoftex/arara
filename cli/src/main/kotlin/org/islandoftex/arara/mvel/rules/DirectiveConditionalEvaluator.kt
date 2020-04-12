// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.core.rules.DirectiveConditionalEvaluator
import org.islandoftex.arara.mvel.utils.Methods
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
    @Suppress("TooGenericExceptionCaught")
    override fun evaluateCondition(conditional: DirectiveConditional): Boolean {
        try {
            val result = TemplateRuntime.eval("@{ " + conditional.condition + " }",
                    Methods.getConditionalMethods())
            return if (result is Boolean) {
                if (conditional.type == DirectiveConditionalType.UNLESS ||
                        conditional.type == DirectiveConditionalType.UNTIL)
                    !result
                else
                    result
            } else {
                throw AraraException(
                        LanguageController.getMessage(
                                Messages.ERROR_EVALUATE_NOT_BOOLEAN_VALUE
                        )
                )
            }
        } catch (exception: RuntimeException) {
            throw AraraException(
                    LanguageController.getMessage(
                            Messages.ERROR_EVALUATE_COMPILATION_FAILED
                    ),
                    exception
            )
        }
    }
}
