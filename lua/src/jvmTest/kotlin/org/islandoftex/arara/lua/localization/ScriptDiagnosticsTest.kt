// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.localization

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.Messages
import org.islandoftex.arara.core.localization.BrazilianLanguage
import org.islandoftex.arara.core.localization.DutchLanguage
import org.islandoftex.arara.core.localization.GermanLanguage
import org.islandoftex.arara.core.localization.ItalianLanguage
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.localization.NorfolkLanguage
import org.islandoftex.arara.core.utils.formatString
import org.islandoftex.arara.lua.LuaInterpreter
import org.islandoftex.arara.lua.ProjectParseException
import org.islandoftex.arara.lua.configuration.LuaLocalConfiguration
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ScriptDiagnosticsTest {
    private fun eachLocale(check: (Messages) -> Unit) {
        val previous = LanguageController.messages
        try {
            for (catalog in listOf(Messages(), GermanLanguage(), ItalianLanguage(), DutchLanguage(),
                BrazilianLanguage(), NorfolkLanguage())) {
                LanguageController.messages = catalog
                check(catalog)
            }
        } finally {
            LanguageController.messages = previous
        }
    }

    @Test
    fun configurationDetailsUseEachLocaleAndKeepQualifiedFieldNames() {
        val root = Files.createTempDirectory("arara localized config ").toMPPPath()
        try {
            val file = root.resolve("arararc.lua")
            eachLocale { messages ->
                val cases = listOf(
                    "verbose = 'true'" to messages.ERROR_SCRIPT_BOOLEAN.formatString("verbose"),
                    "loops = 1.5" to messages.ERROR_SCRIPT_INTEGER.formatString("loops"),
                    "loops = 0" to messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE,
                    "paths = { [2] = 'gap' }" to messages.ERROR_SCRIPT_LIST.formatString("paths"),
                    "log = { file = false }" to messages.ERROR_SCRIPT_STRING.formatString("log.file"),
                    "log = false" to messages.ERROR_SCRIPT_TABLE.formatString("log"),
                    "log = { typo = true }" to messages.ERROR_SCRIPT_UNKNOWN_KEY.formatString("log.typo"),
                    "preamble = { prepend = 'sometimes' }" to
                        messages.ERROR_CONFIGURATION_PREPEND.formatString("sometimes"),
                    "preamble = { definitions = { [1] = {} } }" to
                        messages.ERROR_SCRIPT_STRING_KEYS.formatString("preamble.definitions")
                )
                for ((fields, expected) in cases) {
                    file.writeText("return { $fields }")
                    val error = assertFailsWith<AraraException> { LuaLocalConfiguration.load(file, root) }
                    assertTrue(error.message.orEmpty().contains(file.toString()))
                    assertEquals(expected, error.exception?.message)
                }
            }
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun projectErrorsUseEachLocaleAndKeepTheOffendingField() = eachLocale { messages ->
        val parser = LuaInterpreter(MPPPath("."))
        val cases = listOf(
            "return true" to messages.ERROR_SCRIPT_TABLE.formatString("specification"),
            "return { files = false }" to messages.ERROR_SCRIPT_TABLE.formatString("project.files"),
            "return { name = ' ', files = {} }" to messages.ERROR_SCRIPT_NONBLANK.formatString("project.name"),
            "return { files = { ['a.tex'] = { priority = false } } }" to
                messages.ERROR_SCRIPT_INTEGER.formatString("project.files[a.tex].priority"),
            "return { files = { ['a.tex'] = {} }, typo = true }" to
                messages.ERROR_SCRIPT_UNKNOWN_KEY.formatString("project.typo")
        )
        for ((script, expected) in cases) {
            val error = assertFailsWith<ProjectParseException> { parser.parseProjectsFromLua(script) }
            assertEquals(expected, error.message)
        }
        val failure = assertFailsWith<ProjectParseException> {
            parser.parseProjectsFromLua("error('authored sentinel')", chunkName = "source.proj.lua")
        }
        assertTrue(failure.message.orEmpty().contains("source.proj.lua"))
        assertTrue(failure.message.orEmpty().contains("authored sentinel"))
    }
}
