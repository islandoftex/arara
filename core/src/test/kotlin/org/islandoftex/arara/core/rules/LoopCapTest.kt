// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the loop cap of the conditional evaluator: a `while` and an
 * `until` body run exactly the configured number of times, and the
 * evaluation reports false once the cap is reached - the boundary the
 * loop guards protect the tool with.
 */
class LoopCapTest {
    private fun conditional(type: DirectiveConditionalType): DirectiveConditional =
        object : DirectiveConditional {
            override val type = type
            override val condition = "found('log', 'x')"
        }

    // the cap logic lives in the abstract base; the minimal subclass
    // holds the condition true so only the counter can end the loop
    private fun alwaysHolding(): DirectiveConditionalEvaluator =
        object : DirectiveConditionalEvaluator(
            LinearExecutor.executionOptions
        ) {
            override fun evaluateCondition(
                conditional: DirectiveConditional
            ): Boolean = true
        }

    private fun withCap(cap: Int, block: () -> Unit) {
        val original = LinearExecutor.executionOptions
        LinearExecutor.executionOptions =
            ExecutionOptions.from(original).copy(maxLoops = cap)
        try {
            block()
        } finally {
            LinearExecutor.executionOptions = original
        }
    }

    @Test
    fun `a while conditional runs its body exactly the cap`() {
        withCap(3) {
            val evaluator = alwaysHolding()
            val conditional = conditional(DirectiveConditionalType.WHILE)
            // the body runs per evaluation while the conditional holds:
            // three true reports, and the fourth evaluation stops the
            // loop at exactly the cap
            repeat(3) {
                assertTrue(evaluator.evaluate(conditional), "the body keeps running within the cap")
            }
            assertFalse(
                evaluator.evaluate(conditional),
                "the cap ends the loop on the next evaluation"
            )
            assertFalse(
                evaluator.evaluate(conditional),
                "the loop stays ended past the cap"
            )
        }
    }

    @Test
    fun `an until conditional stops at the cap with its earlier boundary`() {
        withCap(3) {
            val evaluator = alwaysHolding()
            val conditional = conditional(DirectiveConditionalType.UNTIL)
            // the until boundary is one earlier: it checks at the cap
            // itself, so the body runs one fewer time than a while
            repeat(2) {
                assertTrue(evaluator.evaluate(conditional), "the body keeps running before the cap")
            }
            assertFalse(
                evaluator.evaluate(conditional),
                "the until boundary stops at the cap"
            )
        }
    }
}
