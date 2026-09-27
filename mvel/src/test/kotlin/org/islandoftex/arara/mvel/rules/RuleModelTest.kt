// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

/**
 * Verifies the YAML rule model, the format the deprecated engine reads:
 * the parse rejections users of hand-written rules encounter - the
 * configuration header, the identifier match, the mandatory fields and
 * the argument validations - together with the serialized command
 * contract. The format lives for the current release as the default
 * and for the next one as a deprecated guest, so its error surface
 * deserves the same pinning the Lua format has.
 */
class RuleModelTest {
    private fun ruleFile(content: String): MPPPath {
        val file = Files.createTempFile("arara-rule-", ".yaml")
        Files.writeString(file, content)
        file.toFile().deleteOnExit()
        return MPPPath(file.toString())
    }

    private fun parse(content: String) =
        Rule.parse(ruleFile(content), "demo")

    private val validRule = """
        !config
        identifier: demo
        name: Demo
        authors:
        - Island of TeX
        commands:
        - name: The demo tool
          command: >
            @{ return true; }
        arguments: []
    """.trimIndent()

    @Test
    fun `parses valid rules`() {
        val rule = parse(validRule)
        assertTrue(rule.displayName == "Demo", "the rule name parses")
        val command = rule.commands.single() as SerialRuleCommand
        assertTrue(
            "return true" in (command.commandString ?: ""),
            "the command expression is available, trimmed"
        )
    }

    @Test
    fun `rejects rules without the configuration header`() {
        assertFailsWith<AraraException>(
            "a rule without its header is not a rule"
        ) {
            parse(validRule.removePrefix("!config\n"))
        }
    }

    @Test
    fun `wraps unparseable rule files`() {
        assertFailsWith<AraraException>(
            "a broken rule file reports the generic parse error"
        ) {
            parse("!config\n{ broken")
        }
    }

    @Test
    fun `rejects mismatched identifiers`() {
        assertFailsWith<AraraException>(
            "a rule not matching its directive is rejected"
        ) {
            parse(validRule.replace("identifier: demo", "identifier: other"))
        }
    }

    @Test
    fun `rejects rules without a name`() {
        assertFailsWith<AraraException>("a rule without a name is rejected") {
            parse(validRule.replace("name: Demo\n", ""))
        }
    }

    @Test
    fun `rejects commands without their expression`() {
        assertFailsWith<AraraException>(
            "a command without its expression is rejected"
        ) {
            parse(
                validRule.replace(
                    "  command: >\n    @{ return true; }",
                    ""
                )
            )
        }
    }

    @Test
    fun `rejects reserved argument identifiers`() {
        assertFailsWith<AraraException>(
            "the reference identifier is reserved for the file itself"
        ) {
            parse(
                validRule.replace(
                    "arguments: []",
                    """
                    arguments:
                    - identifier: reference
                      flag: '@{ return "x"; }'
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `rejects duplicate argument identifiers`() {
        assertFailsWith<AraraException>(
            "an argument list with duplicates is rejected"
        ) {
            parse(
                validRule.replace(
                    "arguments: []",
                    """
                    arguments:
                    - identifier: one
                      flag: '@{ return "x"; }'
                    - identifier: one
                      flag: '@{ return "x"; }'
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `rejects arguments without flag or default`() {
        assertFailsWith<AraraException>(
            "an argument needs a flag or a default value"
        ) {
            parse(
                validRule.replace(
                    "arguments: []",
                    """
                    arguments:
                    - identifier: mode
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `rejects blank argument identifiers`() {
        assertFailsWith<AraraException>(
            "an argument needs its identifier"
        ) {
            parse(
                validRule.replace(
                    "arguments: []",
                    """
                    arguments:
                    - identifier: ''
                      flag: '@{ return "x"; }'
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun `the serialized command rejects direct evaluation`() {
        val command = parse(validRule).commands.single() as SerialRuleCommand
        assertFailsWith<AraraException>(
            "the serialized command is a template, not a runnable value"
        ) {
            command.command
        }
    }
}
