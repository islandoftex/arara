// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.DirectiveConditional
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LuaConditionalEvaluatorTest {
    private fun evaluator(maxLoops: Int = 10) =
        LuaConditionalEvaluator(ExecutionOptions(maxLoops = maxLoops))

    private fun conditional(
        type: DirectiveConditionalType,
        condition: String
    ): DirectiveConditional = DirectiveConditional(type, condition)

    @Test
    fun shouldEvaluateTrueLuaConditions() {
        // like the interpreter, every conditional gets its own evaluator
        // instance as the loop counter and the halt flag are per-directive
        assertTrue(
            evaluator().evaluate(
                conditional(DirectiveConditionalType.IF, "1 == 1")
            ),
            "a true condition holds"
        )
        assertTrue(
            evaluator().evaluate(
                conditional(
                    DirectiveConditionalType.IF,
                    "toFile('probe.txt') ~= nil"
                )
            ),
            "a helper call in the condition resolves to true"
        )
    }

    @Test
    fun shouldInvertUnlessAndUntilConditions() {
        assertFalse(
            evaluator().evaluate(
                conditional(DirectiveConditionalType.UNLESS, "1 == 1")
            ),
            "unless inverts a true condition"
        )
        assertTrue(
            evaluator().evaluate(
                conditional(DirectiveConditionalType.UNLESS, "1 == 2")
            ),
            "unless keeps a false condition"
        )
        assertTrue(
            evaluator().evaluate(
                conditional(DirectiveConditionalType.UNTIL, "1 == 2")
            ),
            "until keeps a false condition"
        )
    }

    @Test
    fun shouldRunIfConditionsAtMostOnce() {
        val evaluator = evaluator()
        val conditional =
            conditional(DirectiveConditionalType.IF, "1 == 1")
        // the first evaluation decides and sets the halt flag, the
        // second short-circuits so the directive cannot loop
        assertTrue(evaluator.evaluate(conditional), "the first evaluation decides")
        assertFalse(evaluator.evaluate(conditional), "the second short-circuits")
    }

    @Test
    fun shouldCapWhileAndUntilLoopsAtMaxLoops() {
        // while and until need separate evaluators, as the loop counter
        // is per-directive in the pipeline as well
        val whileEvaluator = evaluator(maxLoops = 2)
        val whileConditional =
            conditional(DirectiveConditionalType.WHILE, "true")
        assertTrue(whileEvaluator.evaluate(whileConditional), "first while iteration holds")
        assertTrue(whileEvaluator.evaluate(whileConditional), "second while iteration holds")
        assertFalse(whileEvaluator.evaluate(whileConditional), "the while cap stops the loop")

        val untilEvaluator = evaluator(maxLoops = 2)
        val untilConditional =
            conditional(DirectiveConditionalType.UNTIL, "false")
        // the until cap uses >= so the loop stops after exactly maxLoops
        // runs: the second evaluation already short-circuits
        assertTrue(untilEvaluator.evaluate(untilConditional), "first until iteration holds")
        assertFalse(untilEvaluator.evaluate(untilConditional), "the until cap stops the loop on the second evaluation")
        assertFalse(untilEvaluator.evaluate(untilConditional), "the until cap keeps stopping the loop")
    }

    @Test
    fun shouldRejectNonBooleanResults() {
        val evaluator = evaluator()
        // Lua truthiness deliberately does not apply: the condition has to
        // yield a proper boolean, mirroring the MVEL dialect
        val exception = kotlin.test.assertFailsWith<AraraException>(
            "a non-boolean condition must be rejected"
        ) {
            evaluator.evaluate(
                conditional(DirectiveConditionalType.IF, "1")
            )
        }
        assertTrue(exception.message != null, "the rejection carries a message")
    }

    @Test
    fun shouldWrapLuaSyntaxErrors() {
        val evaluator = evaluator()
        // && is MVEL syntax; in the Lua dialect the condition does not
        // even parse, which has to surface as an arara error
        val exception = kotlin.test.assertFailsWith<AraraException>(
            "MVEL syntax must not parse as Lua"
        ) {
            evaluator.evaluate(
                conditional(
                    DirectiveConditionalType.IF,
                    "1 == 1 && 2 == 2"
                )
            )
        }
        assertTrue(exception.message != null, "the rejection carries a message")
    }

    @Test
    fun shouldExposeTheSessionToConditions() {
        val evaluator = evaluator()
        assertFalse(
            evaluator.evaluate(
                conditional(
                    DirectiveConditionalType.IF,
                    "getSession().contains('lua-conditional-probe')"
                )
            ),
            "an unseeded session key resolves to false"
        )
    }
}
