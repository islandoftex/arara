// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType

/**
 * Verifies the conditional evaluator of the deprecated engine: the
 * inversion of an `until` condition, the rejection of results that
 * are not proper booleans, and the wrapping of compilation failures -
 * the same proof the Lua evaluator already carries.
 */
class DirectiveConditionalEvaluatorTest {
    private fun conditional(type: DirectiveConditionalType, condition: String): DirectiveConditional =
        object : DirectiveConditional {
            override val type = type
            override val condition = condition
        }

    @Test
    fun `inverts an until condition`() {
        val evaluator = DirectiveConditionalEvaluator(
            org.islandoftex.arara.core.configuration.ExecutionOptions()
        )
        val conditional = conditional(
            DirectiveConditionalType.UNTIL, "true"
        )
        assertFalse(
            evaluator.evaluate(conditional),
            "an until condition inverts its result"
        )
        val falseConditional = conditional(
            DirectiveConditionalType.UNTIL, "false"
        )
        assertTrue(
            evaluator.evaluate(falseConditional),
            "the inversion holds for a false condition too"
        )
    }

    @Test
    fun `rejects results that are not proper booleans`() {
        val evaluator = DirectiveConditionalEvaluator(
            org.islandoftex.arara.core.configuration.ExecutionOptions()
        )
        val conditional = conditional(
            DirectiveConditionalType.IF, "1 + 1"
        )
        val exception = assertFailsWith<AraraException>(
            "a non boolean result is rejected"
        ) {
            evaluator.evaluate(conditional)
        }
        assertTrue(
            exception.message?.contains("boolean value") == true,
            "the localized rejection carries: ${exception.message}"
        )
    }

    @Test
    fun `wraps compilation failures`() {
        val evaluator = DirectiveConditionalEvaluator(
            org.islandoftex.arara.core.configuration.ExecutionOptions()
        )
        val conditional = conditional(
            DirectiveConditionalType.IF, "1 +"
        )
        val exception = assertFailsWith<AraraException>(
            "a malformed condition wraps into the compilation error"
        ) {
            evaluator.evaluate(conditional)
        }
        assertTrue(
            exception.message?.contains("compile the expression") == true,
            "the localized wrap carries: ${exception.message}"
        )
    }
}
