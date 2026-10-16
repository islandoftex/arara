// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.interpreter

import com.charleskorn.kaml.Yaml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.mvel.rules.RuleArgument
import org.islandoftex.arara.mvel.rules.SerialRuleCommand

class MvelCommandEvaluatorTest {
    @Test
    fun commandEvaluationAddsMvelHelpersToResolvedParameters() {
        val command = SerialRuleCommand().apply { commandString = "@{ buildString('tool', mode) }" }
        assertEquals("tool draft", MvelCommandEvaluator(command).evaluate(mapOf("mode" to "draft")))
    }

    @Test
    fun argumentFlagsStillReceiveHelpersWithAnEngineNeutralContext() {
        val argument = Yaml.default.decodeFromString(RuleArgument.serializer(), """
            identifier: mode
            flag: "@{ buildString(trimSpaces(parameters.mode), 'suffix') }"
        """.trimIndent())
        assertEquals(listOf("draft suffix"), argument.processor(
            " draft ", mapOf("parameters" to mapOf("mode" to " draft "))
        ))
    }

    @Test
    fun defaultAndCustomExitExpressionsKeepTheirBooleanMeaning() {
        val command = SerialRuleCommand()
        val evaluator = MvelCommandEvaluator(command)
        assertEquals(true, evaluator.isSuccessful(0))
        assertEquals(false, evaluator.isSuccessful(7))
        command.exit = "value == 7"
        assertEquals(true, evaluator.isSuccessful(7))
        assertEquals(false, evaluator.isSuccessful(0))
    }

    @Test
    fun malformedExpressionsAndNonBooleanExitResultsRemainContextualErrors() {
        val command = SerialRuleCommand().apply { commandString = "@{ throw new RuntimeException('sentinel'); }" }
        assertFailsWith<AraraExceptionWithHeader> { MvelCommandEvaluator(command).evaluate(emptyMap()) }
        command.exit = "value =="
        assertFailsWith<AraraExceptionWithHeader> { MvelCommandEvaluator(command).isSuccessful(0) }
        command.exit = "42"
        assertFailsWith<AraraExceptionWithHeader> { MvelCommandEvaluator(command).isSuccessful(0) }
    }
}
