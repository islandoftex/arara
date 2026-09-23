// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.DirectiveConditional
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Verifies the evaluator's user-facing diagnostics in addition to its
 * semantics: helper binding errors surface behind the localized
 * evaluation wrapper with the binding message as the cause, matching
 * what the MVEL dialect produces for helper failures.
 */
class LuaConditionalEvaluatorErrorTest {
    private fun evaluator() =
        LuaConditionalEvaluator(ExecutionOptions(maxLoops = 10))

    @Test
    fun `wraps helper binding errors behind the localized evaluation error`() {
        val exception = kotlin.test.assertFailsWith<AraraException>(
            "a binding error must not propagate raw"
        ) {
            evaluator().evaluate(
                DirectiveConditional(
                    DirectiveConditionalType.IF, "exists(true)"
                )
            )
        }
        assertTrue(
            exception.message != null,
            "the primary message is the localized evaluation error"
        )
        assertTrue(
            exception.exception?.message
                ?.contains("bad argument #1 to 'exists'") == true,
            "the binding message is available as the cause"
        )
    }

    @Test
    fun `keeps the strict boolean error as the primary message`() {
        val exception = kotlin.test.assertFailsWith<AraraException>(
            "a non-boolean condition must report the boolean error, " +
                "not a double-wrapped compilation error"
        ) {
            evaluator().evaluate(
                DirectiveConditional(DirectiveConditionalType.IF, "1")
            )
        }
        assertTrue(
            exception.message != exception.exception?.message,
            "the strict boolean error is not nested behind itself"
        )
        assertTrue(
            exception.exception == null,
            "the strict boolean error carries no spurious cause"
        )
    }
}
