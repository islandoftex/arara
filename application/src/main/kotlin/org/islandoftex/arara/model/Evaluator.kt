// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.model

import org.islandoftex.arara.Arara
import org.islandoftex.arara.AraraException
import org.islandoftex.arara.configuration.AraraSpec
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.rules.DirectiveConditional
import org.islandoftex.arara.rules.DirectiveConditionalType
import org.islandoftex.arara.utils.Methods
import org.mvel2.templates.TemplateRuntime

/**
 * Implements the evaluator model, on which a conditional can be analyzed and
 * processed.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class Evaluator {
    // this attribute holds the maximum number of
    // loops arara will accept; it's like
    // reaching infinity
    private val loops: Int = Arara.config[AraraSpec.Execution.maxLoops]

    // the counter for the current execution, it
    // helps us keep track of the number of times
    // this evaluation has happened, and also to
    // prevent potential infinite loops
    private var counter: Int = 0

    // a flag that indicates the
    // evaluation to halt regardless
    // of the the result
    private var halt: Boolean = false

    /**
     * Check if a condition is of type if or unless and whether halt
     * is set.
     * @param type The type to check.
     * @param haltCheck The value [halt] should have.
     * @return `(type == if || type == unless) && haltCheck`
     */
    private fun isIfUnlessAndHalt(
        type: DirectiveConditionalType,
        haltCheck: Boolean = true
    ): Boolean =
            (type == DirectiveConditionalType.IF ||
                    type == DirectiveConditionalType.UNLESS) &&
                    halt == haltCheck

    /**
     * Only run the evaluation of the conditional including a check whether
     * the result needs to be inverted.
     * @param conditional The conditional.
     * @return The result of the evaluation.
     */
    @Throws(AraraException::class, RuntimeException::class)
    private fun evaluateCondition(conditional: DirectiveConditional): Boolean {
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
                    messages.getMessage(
                            Messages.ERROR_EVALUATE_NOT_BOOLEAN_VALUE
                    )
            )
        }
    }

    /**
     * Evaluate the provided conditional.
     *
     * @param conditional The conditional object.
     * @return A boolean value indicating if the conditional holds.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught")
    fun evaluate(conditional: DirectiveConditional): Boolean {
        // when in dry-run mode or not evaluating a
        // conditional, arara always ignores conditional
        // evaluations
        if (conditional.type == DirectiveConditionalType.NONE ||
                Arara.config[AraraSpec.Execution.dryrun] ||
                isIfUnlessAndHalt(conditional.type, true))
            return false
        else if (isIfUnlessAndHalt(conditional.type, false)) {
            halt = true
        }

        // check counters and see if the execution
        // has reached our concept of infinity,
        // thus breaking the cycles
        counter++
        return when {
            conditional.type == DirectiveConditionalType.WHILE
                    && counter > loops -> false
            conditional.type == DirectiveConditionalType.UNTIL
                    && counter >= loops -> false
            else -> {
                try {
                    evaluateCondition(conditional)
                } catch (exception: RuntimeException) {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_EVALUATE_COMPILATION_FAILED
                            ),
                            exception
                    )
                }
            }
        }
    }

    companion object {
        // the application messages obtained from the
        // language controller
        private val messages = LanguageController
    }
}
