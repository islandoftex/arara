// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.localization

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.AraraMessages
import org.islandoftex.arara.core.localization.BrazilianPortugueseMessages
import org.islandoftex.arara.core.localization.DutchMessages
import org.islandoftex.arara.core.localization.EnglishMessages
import org.islandoftex.arara.core.localization.FrenchMessages
import org.islandoftex.arara.core.localization.GermanMessages
import org.islandoftex.arara.core.localization.ItalianMessages
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.localization.NorfolkMessages
import org.islandoftex.arara.lua.rules.LuaArguments
import org.islandoftex.arara.lua.rules.LuaConversion
import org.islandoftex.arara.lua.rules.LuaHelperException
import org.islandoftex.arara.lua.rules.LuaInterpolation
import org.islandoftex.arara.lua.rules.LuaResults
import org.islandoftex.arara.lua.rules.LuaRuleParser
import org.luaj.vm2.LuaValue

class RuleDiagnosticsTest {
    private fun eachLocale(check: (AraraMessages) -> Unit) {
        val previous = LanguageController.messages
        try {
            for (messages in listOf(EnglishMessages, GermanMessages, ItalianMessages, DutchMessages,
                BrazilianPortugueseMessages, FrenchMessages, NorfolkMessages)) {
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
                    "return true" to messages.errorScriptTable("rule"),
                    "return { identifier = false }" to messages.errorScriptString("identifier"),
                    "return { identifier = 'other' }" to
                        messages.errorValidateHeaderWrongIdentifier("probe", "other"),
                    "return { identifier = 'probe', name = 'Probe', commands = {} }" to
                        messages.errorScriptEmpty("commands"),
                    "return { identifier = 'probe', name = 'Probe', commands = { {} } }" to
                        messages.errorScriptRequired("commands[1].command"),
                    "return { identifier = 'probe', name = 'Probe', commands = { { command = true } } }" to
                        messages.errorScriptFunction("commands[1].command"),
                    "return { identifier = 'probe', name = 'Probe', $command, arguments = { false } }" to
                        messages.errorScriptTable("arguments[1]"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = '', default = 'x' } } }" to
                        messages.errorScriptNonblank("arguments[1].identifier"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'x', default = function() end } } }" to
                        messages.errorScriptLiteral("arguments[1].default"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'x', default = 'x', required = 'true' } } }" to
                        messages.errorScriptBoolean("arguments[1].required"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'x' } } }" to
                        messages.errorScriptAlternatives("arguments[1]", "flag, default"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'files', default = 'x' } } }" to
                        messages.errorValidateBodyArgumentIdIsReserved("files"),
                    "return { identifier = 'probe', name = 'Probe', $command, " +
                        "arguments = { { identifier = 'x', default = 1 }, { identifier = 'x', default = 2 } } }" to
                        messages.errorValidateBodyDuplicateArgumentIdentifiers
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
            "@{1}" to messages.errorYamlTagsInLua,
            "{{ 1" to messages.errorLuaUnterminatedTag,
            "{{ 'text" to messages.errorLuaUnterminatedQuote,
            "{{ [=[text" to messages.errorLuaUnterminatedLongString
        )
        for ((template, expected) in cases) {
            val error = assertFailsWith<AraraException> { LuaInterpolation.interpolate(template) }
            assertEquals(messages.errorLuaInterpolationFailure, error.message)
            assertEquals(expected, error.exception?.message)
        }
    }

    @Test
    fun helperDiagnosticsPreserveNamesTypesAndValuesInEveryLocale() = eachLocale { messages ->
        val name = "helper-${'$'}1-%s-\\"
        val argument = LuaArguments.badArgument(name, 2, "string", LuaValue.NIL)
        assertEquals(messages.errorLuaBadArgument(2, name, "string", "nil"), argument.message)
        val result = assertFailsWith<AraraException> {
            LuaResults.booleanResult(LuaValue.NIL, name)
        }
        assertEquals(messages.errorLuaBooleanResult(name, "nil"), result.exception?.message)
        val number = assertFailsWith<LuaHelperException> { LuaConversion.toLua(Long.MAX_VALUE) }
        assertEquals(messages.errorLuaExactInteger(Long.MAX_VALUE.toString()), number.message)
        val cyclic = mutableListOf<Any>()
        cyclic.add(cyclic)
        val error = assertFailsWith<LuaHelperException> { LuaConversion.toLua(cyclic) }
        assertEquals(messages.errorLuaCyclicValues, error.message)
    }
}
