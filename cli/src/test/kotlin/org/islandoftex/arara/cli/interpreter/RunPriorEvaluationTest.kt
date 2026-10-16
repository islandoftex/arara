// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the dispatch policy of the interpreter: which conditional
 * types deserve a prior evaluation per execution mode.
 */
class RunPriorEvaluationTest {
    private val originalOptions = LinearExecutor.executionOptions

    private fun conditional(type: DirectiveConditionalType): DirectiveConditional =
        object : DirectiveConditional {
            override val type = type
            override val condition = "found('log', 'x')"
        }

    @Test
    fun `evaluates the priors per conditional type`() {
        LinearExecutor.executionOptions = ExecutionOptions.from(originalOptions)
            .copy(executionMode = ExecutionMode.DRY_RUN)
        try {
            assertFalse(
                runPriorEvaluation(conditional(DirectiveConditionalType.IF)),
                "dry runs skip the prior evaluation"
            )
        } finally {
            LinearExecutor.executionOptions = originalOptions
        }
        assertTrue(runPriorEvaluation(conditional(DirectiveConditionalType.IF)))
        assertTrue(runPriorEvaluation(conditional(DirectiveConditionalType.WHILE)))
        assertTrue(runPriorEvaluation(conditional(DirectiveConditionalType.UNLESS)))
        assertFalse(runPriorEvaluation(conditional(DirectiveConditionalType.UNTIL)))
        assertFalse(runPriorEvaluation(conditional(DirectiveConditionalType.NONE)))
    }
}
