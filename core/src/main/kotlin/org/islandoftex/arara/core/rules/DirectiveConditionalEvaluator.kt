// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import kotlin.time.ExperimentalTime
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType

/**
 * Implements the evaluator model, on which a conditional can be analyzed and
 * processed.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@ExperimentalTime
abstract class DirectiveConditionalEvaluator(
    private val executionOptions: ExecutionOptions
) {
    /**
     * This attribute holds the maximum number of
     * loops arara will accept; it's like reaching infinity
     */
    private val maxLoops: Int = executionOptions.maxLoops

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
    abstract fun evaluateCondition(conditional: DirectiveConditional): Boolean

    /**
     * Evaluate the provided conditional.
     *
     * @param conditional The conditional object.
     * @return A boolean value indicating if the conditional holds.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun evaluate(conditional: DirectiveConditional): Boolean {
        // when in dry-run mode or not evaluating a
        // conditional, arara always ignores conditional
        // evaluations
        if (conditional.type == DirectiveConditionalType.NONE ||
                executionOptions.executionMode == ExecutionMode.DRY_RUN ||
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
                    && counter > maxLoops -> false
            conditional.type == DirectiveConditionalType.UNTIL
                    && counter >= maxLoops -> false
            else -> evaluateCondition(conditional)
        }
    }
}
