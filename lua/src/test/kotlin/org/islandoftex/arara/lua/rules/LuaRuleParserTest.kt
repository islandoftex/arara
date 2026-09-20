// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import java.nio.file.Files
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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
    fun shouldParseAValidRule() {
        val rule = LuaRuleParser.parse(ruleFile(validRule), "demo")
        assertEquals("demo", rule.identifier)
        assertEquals("Demo", rule.displayName)
        assertEquals(listOf("Island of TeX"), rule.authors)
        assertEquals(1, rule.commands.size)
        assertEquals("The demo tool", rule.commands[0].name)
        assertEquals(2, rule.arguments.size)
        assertEquals("shell", rule.arguments[0].identifier)
        assertTrue(rule.arguments[1].isRequired)
        assertEquals("normal", rule.arguments[1].defaultValue)
    }

    @Test
    fun shouldEvaluateExitClosures() {
        val rule = LuaRuleParser.parse(ruleFile(validRule), "demo")
        val command = rule.commands[0]
        assertTrue(command.exitCheck(1))
        assertTrue(!command.exitCheck(2))
    }

    @Test
    fun shouldProcessFlags() {
        val rule = LuaRuleParser.parse(ruleFile(validRule), "demo")
        val context = mapOf<String, Any>(
            "parameters" to mapOf("shell" to "yes"),
            "reference" to MPPPath("file.tex")
        )
        assertEquals(
            listOf("--shell-escape"),
            rule.arguments[0].processor("yes", context)
        )
        assertEquals(
            listOf("--no-shell-escape"),
            rule.arguments[0].processor("no", context)
        )
        assertEquals(
            listOf("--mode", "draft"),
            rule.arguments[1].processor("draft", context)
        )
    }

    @Test
    fun shouldSurfaceRuleErrors() {
        val rule = LuaRuleParser.parse(ruleFile(validRule), "demo")
        val context = mapOf<String, Any>(
            "parameters" to mapOf<String, Any>(),
            "reference" to MPPPath("file.tex")
        )
        val exception = assertFailsWith<AraraException> {
            rule.arguments[1].processor("invalid", context)
        }
        assertTrue(exception.message!!.contains("Invalid mode."))
    }

    @Test
    fun shouldRejectMismatchedIdentifiers() {
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(ruleFile(validRule), "other")
        }
    }

    @Test
    fun shouldRejectRulesWithoutCommands() {
        val rule = """
            return {
              identifier = "demo",
              name = "Demo",
              commands = {},
              arguments = {}
            }
        """.trimIndent()
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
    }

    @Test
    fun shouldRejectReservedArgumentIdentifiers() {
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
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
    }

    @Test
    fun shouldRejectArgumentsWithoutFlagAndDefault() {
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
        assertFailsWith<AraraException> {
            LuaRuleParser.parse(ruleFile(rule), "demo")
        }
    }
}
