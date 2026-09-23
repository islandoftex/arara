// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType

/**
 * Implements the evaluator model, on which a conditional can be analyzed and
 * processed.
 *
 * One evaluator instance is expected per directive, as the loop counter
 * and the halt flag are tied to the lifetime of a single conditional.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
abstract class DirectiveConditionalEvaluator(
    private val executionOptions: ExecutionOptions
) {
    /**
     * This attribute holds the maximum number of
     * loops arara will accept; it's like reaching infinity
     */
    private val maxLoops: Int = executionOptions.maxLoops

    /**
     * The counter for the current execution, it helps us keep track of the
     * number of times this evaluation has happened, and also to prevent
     * potential infinite loops.
     */
    private var counter: Int = 0

    /**
     * A flag that indicates an IF or UNLESS conditional has already been
     * evaluated once, so the enclosing loop in the interpreter does not
     * run such directives more than once (the flag is unrelated to the
     * halt mechanism of the rule helpers).
     */
    private var evaluatedOnce: Boolean = false

    /**
     * Check if a condition is of type if or unless and whether it has
     * been evaluated once already.
     * @param type The type to check.
     * @param check The value [evaluatedOnce] should have.
     * @return `(type == if || type == unless) && evaluatedOnce == check`
     */
    private fun isIfUnlessAndEvaluatedOnce(
        type: DirectiveConditionalType,
        check: Boolean = true
    ): Boolean =
        (
            type == DirectiveConditionalType.IF ||
                type == DirectiveConditionalType.UNLESS
            ) &&
            evaluatedOnce == check

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
            isIfUnlessAndEvaluatedOnce(conditional.type, true)
        )
            return false
        else if (isIfUnlessAndEvaluatedOnce(conditional.type, false)) {
            evaluatedOnce = true
        }

        // check counters and see if the execution has reached our
        // concept of infinity, thus breaking the cycles; the boundary
        // differs on purpose: a WHILE conditional checks its condition
        // before the first run and an UNTIL conditional after each
        // run, so both cap the body at exactly maxLoops executions
        counter++
        return when {
            conditional.type == DirectiveConditionalType.WHILE &&
                counter > maxLoops -> false
            conditional.type == DirectiveConditionalType.UNTIL &&
                counter >= maxLoops -> false
            else -> evaluateCondition(conditional)
        }
    }
}
