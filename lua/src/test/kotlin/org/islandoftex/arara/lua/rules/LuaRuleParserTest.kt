// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

/**
 * Verifies the Lua rule parser: the shape of a valid rule, exit
 * closures, flag processing, surfaced rule errors and the rejection of
 * invalid rules (mismatched identifiers, missing commands, reserved
 * argument identifiers, arguments without flag or default, and
 * wrongly typed entries such as numbers in string slots or non
 * boolean required flags). Runtime failures on the command path
 * surface behind the localized runtime error with the detail as the
 * cause, while author errors keep flowing raw.
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

    @Test
    fun `rejects non string rule names`() {
        val rule = """
            return {
              identifier = "demo",
              name = 42,
              commands = {
                { command = function(ctx) return true end }
              },
              arguments = {}
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a number rule name must not silently become a string"
        ) {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
        assertTrue(
            exception.message!!.contains("requires a string value"),
            "the error names the expected type: ${exception.message}"
        )
    }

    @Test
    fun `rejects non boolean required flags`() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              commands = {
                { command = function(ctx) return true end }
              },
              arguments = {
                { identifier = "shell", required = "yes", default = "x" }
              }
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a string required flag must not be truth-tested"
        ) {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
        assertTrue(
            exception.message!!.contains("requires a boolean value"),
            "the error names the expected type: ${exception.message}"
        )
    }

    @Test
    fun `rejects non string list entries`() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              authors = { "Island of TeX", 42 },
              commands = {
                { command = function(ctx) return true end }
              },
              arguments = {}
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a number authors entry must not silently become a string"
        ) {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
        assertTrue(
            exception.message!!.contains("has to be a string"),
            "the error names the expected entry type: ${exception.message}"
        )
    }

    @Test
    fun `treats absent required flags as false`() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              commands = {
                { command = function(ctx) return true end }
              },
              arguments = {
                { identifier = "shell", default = "x" }
              }
            }
        """.trimIndent()
        val parsed = LuaRuleParser.parse(ruleFile(rule), "demo")
        assertFalse(
            parsed.arguments[0].isRequired,
            "an argument without a required flag is optional"
        )
    }

    @Test
    fun `wraps helper type errors behind the localized command error`() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              commands = {
                {
                  command = function(ctx)
                    return getCommand(trimSpaces(123))
                  end
                }
              },
              arguments = {}
            }
        """.trimIndent()
        val parsed = LuaRuleParser.parse(ruleFile(rule), "demo")
        val exception = assertFailsWith<AraraException>(
            "a helper type error must surface behind the localized wrapper"
        ) {
            parsed.commands[0].evaluate(emptyMap())
        }
        assertTrue(
            exception.message?.contains("could not evaluate") == true,
            "the primary message is the localized command error: " +
                "${exception.message}"
        )
        assertTrue(
            exception.cause?.message
                ?.contains("bad argument #1 to 'trimSpaces'") == true,
            "the bad argument detail is available as the cause: " +
                "${exception.cause?.message}"
        )
    }

    @Test
    fun `keeps author errors raw on the command path`() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              commands = {
                {
                  command = function(ctx)
                    throwError("I was expecting a list of options.")
                    return true
                  end
                }
              },
              arguments = {}
            }
        """.trimIndent()
        val parsed = LuaRuleParser.parse(ruleFile(rule), "demo")
        val exception = assertFailsWith<AraraException>(
            "an author error must keep its message primary"
        ) {
            parsed.commands[0].evaluate(emptyMap())
        }
        assertTrue(
            exception.message
                ?.contains("I was expecting a list of options.") == true,
            "the author text stays the primary message: ${exception.message}"
        )
    }

    @Test
    fun `rejects broken Lua scripts`() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(ruleFile("return {"), "demo")
        }
    }

    @Test
    fun `rejects non table results`() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(ruleFile("return 42"), "demo")
        }
    }

    @Test
    fun `rejects rules without an identifier`() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(ruleFile("""return { name = "Demo" }"""), "demo")
        }
    }

    @Test
    fun `rejects rules without a name`() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(ruleFile("""return { identifier = "demo" }"""), "demo")
        }
    }

    @Test
    fun `rejects commands without their function`() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(
                ruleFile(
                    """
                    return {
                      identifier = "demo",
                      name = "Demo",
                      commands = { { name = "No function" } }
                    }
                    """.trimIndent()
                ),
                "demo"
            )
        }
    }

    @Test
    fun `rejects blank argument identifiers`() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(
                ruleFile(
                    """
                    return {
                      identifier = "demo",
                      name = "Demo",
                      commands = { { command = function(ctx) return true end } },
                      arguments = { { identifier = "" } }
                    }
                    """.trimIndent()
                ),
                "demo"
            )
        }
    }

    @Test
    fun `rejects wrongly typed rule collections`() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(
                ruleFile(
                    """
                    return {
                      identifier = "demo",
                      name = "Demo",
                      commands = { { command = function(ctx) return true end } },
                      authors = { 42 }
                    }
                    """.trimIndent()
                ),
                "demo"
            )
        }
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(
                ruleFile(
                    """
                    return {
                      identifier = "demo",
                      name = "Demo",
                      commands = { 42 }
                    }
                    """.trimIndent()
                ),
                "demo"
            )
        }
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(
                ruleFile(
                    """
                    return {
                      identifier = "demo",
                      name = "Demo",
                      commands = { { command = function(ctx) return true end } },
                      arguments = { 42 }
                    }
                    """.trimIndent()
                ),
                "demo"
            )
        }
    }

    @Test
    fun `rejects duplicate argument identifiers`() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(
                ruleFile(
                    """
                    return {
                      identifier = "demo",
                      name = "Demo",
                      commands = { { command = function(ctx) return true end } },
                      arguments = {
                        { identifier = "one", flag = function(ctx, v) return { } end },
                        { identifier = "one", flag = function(ctx, v) return { } end }
                      }
                    }
                    """.trimIndent()
                ),
                "demo"
            )
        }
    }
}
