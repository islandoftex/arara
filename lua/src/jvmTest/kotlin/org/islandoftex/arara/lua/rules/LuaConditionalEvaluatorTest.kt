// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.DirectiveConditional

class LuaConditionalEvaluatorTest {
    @Test
    fun conditionsAreStrictBooleansAndInvertUnlessAndUntil() {
        val evaluator = LuaConditionalEvaluator(ExecutionOptions())
        assertTrue(evaluator.evaluateCondition(DirectiveConditional(DirectiveConditionalType.IF, "true and not false")))
        assertFalse(evaluator.evaluateCondition(DirectiveConditional(DirectiveConditionalType.UNLESS, "true")))
        assertTrue(evaluator.evaluateCondition(DirectiveConditional(DirectiveConditionalType.UNTIL, "false")))
        listOf("1", "nil", "'true'", "{}", "true && false").forEach {
            assertFailsWith<AraraException> {
                evaluator.evaluateCondition(DirectiveConditional(DirectiveConditionalType.IF, it))
            }
        }
    }

    @Test
    fun sharedSchedulingCapsLoopsAndSkipsDryRunExpressions() {
        val condition = DirectiveConditional(DirectiveConditionalType.WHILE, "true")
        val evaluator = LuaConditionalEvaluator(ExecutionOptions(maxLoops = 2))
        assertTrue(evaluator.evaluate(condition))
        assertTrue(evaluator.evaluate(condition))
        assertFalse(evaluator.evaluate(condition))
        val once = LuaConditionalEvaluator(ExecutionOptions())
        val onlyIf = DirectiveConditional(DirectiveConditionalType.IF, "true")
        assertTrue(once.evaluate(onlyIf))
        assertFalse(once.evaluate(onlyIf))
        assertFalse(LuaConditionalEvaluator(ExecutionOptions(executionMode = ExecutionMode.DRY_RUN))
            .evaluate(DirectiveConditional(DirectiveConditionalType.IF, "error('must not execute')")))
    }
}
