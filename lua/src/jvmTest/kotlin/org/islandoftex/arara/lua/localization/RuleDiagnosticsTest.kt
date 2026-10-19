// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.localization

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.Messages
import org.islandoftex.arara.core.localization.BrazilianLanguage
import org.islandoftex.arara.core.localization.DutchLanguage
import org.islandoftex.arara.core.localization.GermanLanguage
import org.islandoftex.arara.core.localization.ItalianLanguage
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.localization.NorfolkLanguage
import org.islandoftex.arara.core.utils.formatString
import org.islandoftex.arara.lua.rules.LuaConversion
import org.islandoftex.arara.lua.rules.LuaHelperException
import org.islandoftex.arara.lua.rules.LuaInterpolation
import org.islandoftex.arara.lua.rules.LuaRuleParser
import org.luaj.vm2.LuaValue
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RuleDiagnosticsTest {
    private fun eachLocale(check: (Messages) -> Unit) {
        val previous = LanguageController.messages
        try {
            for (messages in listOf(Messages(), GermanLanguage(), ItalianLanguage(), DutchLanguage(),
                BrazilianLanguage(), NorfolkLanguage())) {
                LanguageController.messages = messages
                check(messages)
            }
        } finally {
            LanguageController.messages = previous
        }
    }

    @Test
    fun ruleSchemaErrorsUseEveryLocaleAndKeepRuleAndFieldContext() {
        val root = Files.createTempDirectory("arara localized rule ").toMPPPath()
        try {
            val file = root.resolve("probe.lua")
            val command = "commands = {{ command = function(ctx) return true end }}"
            eachLocale { messages ->
                val cases = listOf(
                    "return true" to messages.ERROR_SCRIPT_TABLE.formatString("rule"),
                    "return { identifier = false }" to messages.ERROR_SCRIPT_STRING.formatString("identifier"),
                    "return { identifier = 'other' }" to
                        messages.ERROR_VALIDATEHEADER_WRONG_IDENTIFIER.formatString("probe", "other"),
                    "return { identifier = 'probe', name = 'Probe', commands = {} }" to
                        messages.ERROR_SCRIPT_EMPTY.formatString("commands"),
                    "return { identifier = 'probe', name = 'Probe', commands = { {} } }" to
                        messages.ERROR_SCRIPT_REQUIRED.formatString("commands[1].command"),
                    "return { identifier = 'probe', name = 'Probe', commands = { { command = true } } }" to
                        messages.ERROR_SCRIPT_FUNCTION.formatString("commands[1].command"),
                    "return { identifier = 'probe', name = 'Probe', $command, arguments = { false } }" to
                        messages.ERROR_SCRIPT_TABLE.formatString("arguments[1]"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = '', default = 'x' } } }" to
                        messages.ERROR_SCRIPT_NONBLANK.formatString("arguments[1].identifier"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'x', default = function() end } } }" to
                        messages.ERROR_SCRIPT_LITERAL.formatString("arguments[1].default"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'x', default = 'x', required = 'true' } } }" to
                        messages.ERROR_SCRIPT_BOOLEAN.formatString("arguments[1].required"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'x' } } }" to
                        messages.ERROR_SCRIPT_ALTERNATIVES.formatString("arguments[1]", "flag, default"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'files', default = 'x' } } }" to
                        messages.ERROR_VALIDATEBODY_ARGUMENT_ID_IS_RESERVED.formatString("files"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'x', default = 1 }, { identifier = 'x', default = 2 } } }" to
                        messages.ERROR_VALIDATEBODY_DUPLICATE_ARGUMENT_IDENTIFIERS
                )
                for ((script, expected) in cases) {
                    file.writeText(script)
                    val error = assertFailsWith<AraraException> { LuaRuleParser.parse(file, "probe") }
                    assertTrue(error.message.orEmpty().contains("probe"))
                    assertTrue(error.message.orEmpty().contains(root.toString()))
                    assertEquals(expected, error.exception?.message)
                }
            }
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun interpolationUsesLuaSpecificMessagesInEveryLocale() = eachLocale { messages ->
        val cases = listOf(
            "@{1}" to messages.ERROR_YAML_TAGS_IN_LUA,
            "{{ 1" to messages.ERROR_LUA_UNTERMINATED_TAG,
            "{{ 'text" to messages.ERROR_LUA_UNTERMINATED_QUOTE,
            "{{ [=[text" to messages.ERROR_LUA_UNTERMINATED_LONG_STRING
        )
        for ((template, expected) in cases) {
            val error = assertFailsWith<AraraException> { LuaInterpolation.interpolate(template) }
            assertEquals(messages.ERROR_LUA_INTERPOLATION_FAILURE, error.message)
            assertEquals(expected, error.exception?.message)
        }
    }

    @Test
    fun helperDiagnosticsPreserveNamesTypesAndValuesInEveryLocale() = eachLocale { messages ->
        val name = "helper-${'$'}1-%s-\\"
        val argument = LuaConversion.badArgument(name, 2, "string", LuaValue.NIL)
        assertEquals(messages.ERROR_LUA_BAD_ARGUMENT.formatString("2", name, "string", "nil"), argument.message)
        val result = assertFailsWith<AraraException> {
            LuaConversion.booleanResult(LuaValue.NIL, name)
        }
        assertEquals(messages.ERROR_LUA_BOOLEAN_RESULT.formatString(name, "nil"), result.exception?.message)
        val number = assertFailsWith<LuaHelperException> { LuaConversion.toLua(Long.MAX_VALUE) }
        assertEquals(messages.ERROR_LUA_EXACT_INTEGER.formatString(Long.MAX_VALUE.toString()), number.message)
        val cyclic = mutableListOf<Any>()
        cyclic.add(cyclic)
        val error = assertFailsWith<LuaHelperException> { LuaConversion.toLua(cyclic) }
        assertEquals(messages.ERROR_LUA_CYCLIC_VALUES, error.message)
    }
}
