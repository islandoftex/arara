// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.DirectiveConditional
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RuleValidationTest {
    private fun parse(arguments: String, body: (org.islandoftex.arara.api.rules.Rule) -> Unit = {}) {
        val directory = Files.createTempDirectory("arara rule validation ").toMPPPath()
        try {
            val file = directory.resolve("sample.yaml")
            file.writeText("""
                !config
                identifier: sample
                name: Sample
                commands:
                  - command: '@{ true }'
                arguments:
            """.trimIndent() + "\n" + arguments.prependIndent("  ") + "\n")
            body(Rule.parse(file, "sample"))
        } finally {
            directory.removeDirectory()
        }
    }

    @Test
    fun reservedArgumentIdentifiersAreRejectedBeforeExecution() {
        for (identifier in listOf("files", "reference")) {
            val failure = assertFailsWith<AraraException> {
                parse("- identifier: $identifier\n  flag: '@{ parameters.$identifier }'")
            }
            assertTrue(failure.message!!.contains(identifier))
        }
    }

    @Test
    fun duplicateIdentifiersAreRejectedEvenWhenTheirDefinitionsDiffer() {
        assertFailsWith<AraraException> {
            parse("- identifier: option\n  flag: 'first'\n- identifier: option\n  default: 'second'")
        }
    }

    @Test
    fun distinctIdentifiersRemainValid() {
        parse("- identifier: one\n  flag: 'first'\n- identifier: two\n  default: 'second'") {
            assertEquals(listOf("one", "two"), it.arguments.map { argument -> argument.identifier })
        }
    }

    @Test
    fun malformedArgumentsReportTheRuleAndPath() {
        val failure = assertFailsWith<AraraException> { parse("- identifier: missing") }
        assertTrue(failure.message!!.contains("sample"))
        assertTrue(failure.message!!.contains("sample.yaml"))
    }

    @Test
    fun invalidConditionalsFailRatherThanBeingTreatedAsBooleans() {
        for (condition in listOf("42", "return (")) {
            assertFailsWith<AraraException> {
                DirectiveConditionalEvaluator(ExecutionOptions()).evaluateCondition(
                    DirectiveConditional(DirectiveConditionalType.IF, condition)
                )
            }
        }
    }
}
