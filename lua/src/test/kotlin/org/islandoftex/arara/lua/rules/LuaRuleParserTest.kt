// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import java.nio.file.Files
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies the Lua rule parser: the shape of a valid rule, exit
 * closures, flag processing, surfaced rule errors and the rejection of
 * invalid rules (mismatched identifiers, missing commands, reserved
 * argument identifiers and arguments without flag or default).
 */
class LuaRuleParserTest {
    private fun ruleFile(content: String): MPPPath {
        val file = Files.createTempFile("arara-rule-", ".lua")
        Files.writeString(file, content)
        file.toFile().deleteOnExit()
        return MPPPath(file.toString())
    }

    private val validRule = """
        return {
          identifier = "demo",
          name = "Demo",
          authors = { "Island of TeX" },
          commands = {
            {
              name = "The demo tool",
              command = function(ctx)
                return true
              end,
              exit = function(code) return code <= 1 end
            }
          },
          arguments = {
            {
              identifier = "shell",
              flag = function(ctx, value)
                return isTrue(value, "--shell-escape", "--no-shell-escape")
              end
            },
            {
              identifier = "mode",
              required = true,
              default = "normal",
              flag = function(ctx, value)
                if contains({ "normal", "draft" }, value) then
                  return { "--mode", value }
                else
                  throwError("Invalid mode.")
                end
              end
            }
          }
        }
    """.trimIndent()

    @Test
    fun `parses a valid rule`() {
        val rule = LuaRuleParser.parse(ruleFile(validRule), "demo")
        assertEquals("demo", rule.identifier, "the rule identifier matches the directive")
        assertEquals("Demo", rule.displayName, "the rule display name")
        assertEquals(listOf("Island of TeX"), rule.authors, "the rule authors")
        assertEquals(1, rule.commands.size, "the rule holds a single command")
        assertEquals("The demo tool", rule.commands[0].name, "the command name")
        assertEquals(2, rule.arguments.size, "the rule holds two arguments")
        assertEquals("shell", rule.arguments[0].identifier, "the first argument identifier")
        assertTrue(rule.arguments[1].isRequired, "the second argument is required")
        assertEquals("normal", rule.arguments[1].defaultValue, "the second argument default value")
    }

    @Test
    fun `evaluates exit closures`() {
        val rule = LuaRuleParser.parse(ruleFile(validRule), "demo")
        val command = rule.commands[0]
        assertTrue(command.exitCheck(1), "exit code one satisfies the exit closure")
        assertFalse(command.exitCheck(2), "exit code two violates the exit closure")
    }

    @Test
    fun `processes flags`() {
        val rule = LuaRuleParser.parse(ruleFile(validRule), "demo")
        val context = mapOf<String, Any>(
            "parameters" to mapOf("shell" to "yes"),
            "reference" to MPPPath("file.tex")
        )
        assertEquals(
            listOf("--shell-escape"),
            rule.arguments[0].processor("yes", context),
            "the truthy flag value produces the enable option"
        )
        assertEquals(
            listOf("--no-shell-escape"),
            rule.arguments[0].processor("no", context),
            "the falsy flag value produces the disable option"
        )
        assertEquals(
            listOf("--mode", "draft"),
            rule.arguments[1].processor("draft", context),
            "the choice flag echoes the selected mode"
        )
    }

    @Test
    fun `surfaces rule errors`() {
        val rule = LuaRuleParser.parse(ruleFile(validRule), "demo")
        val context = mapOf<String, Any>(
            "parameters" to mapOf<String, Any>(),
            "reference" to MPPPath("file.tex")
        )
        val exception = assertFailsWith<AraraException>(
            "an invalid choice must surface the rule error"
        ) {
            rule.arguments[1].processor("invalid", context)
        }
        assertTrue(exception.message!!.contains("Invalid mode."), "the rule error names the invalid mode")
    }

    @Test
    fun `rejects mismatched identifiers`() {
        assertFailsWith<AraraException>(
            "a mismatched directive identifier must be rejected"
        ) {
            LuaRuleParser.parse(ruleFile(validRule), "other")
        }
    }

    @Test
    fun `rejects rules without commands`() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              commands = {},
              arguments = {}
            }
        """.trimIndent()
        assertFailsWith<AraraException>(
            "a rule without commands must be rejected"
        ) {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
    }

    @Test
    fun `rejects reserved argument identifiers`() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              commands = {
                { command = function(ctx) return true end }
              },
              arguments = {
                { identifier = "reference", default = "x" }
              }
            }
        """.trimIndent()
        assertFailsWith<AraraException>(
            "a reserved argument identifier must be rejected"
        ) {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
    }

    @Test
    fun `rejects arguments without flag and default`() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              commands = {
                { command = function(ctx) return true end }
              },
              arguments = {
                { identifier = "broken" }
              }
            }
        """.trimIndent()
        assertFailsWith<AraraException>(
            "an argument without flag and default must be rejected"
        ) {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
    }
}
