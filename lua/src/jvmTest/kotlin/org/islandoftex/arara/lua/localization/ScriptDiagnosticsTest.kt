// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.localization

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.localization.AraraMessages
import org.islandoftex.arara.core.localization.BrazilianPortugueseMessages
import org.islandoftex.arara.core.localization.DutchMessages
import org.islandoftex.arara.core.localization.EnglishMessages
import org.islandoftex.arara.core.localization.GermanMessages
import org.islandoftex.arara.core.localization.ItalianMessages
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.localization.NorfolkMessages
import org.islandoftex.arara.lua.LuaInterpreter
import org.islandoftex.arara.lua.ProjectParseException
import org.islandoftex.arara.lua.configuration.LuaLocalConfiguration

class ScriptDiagnosticsTest {
    private fun eachLocale(check: (AraraMessages) -> Unit) {
        val previous = LanguageController.messages
        try {
            for (catalog in listOf(EnglishMessages, GermanMessages, ItalianMessages, DutchMessages,
                BrazilianPortugueseMessages, NorfolkMessages)) {
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
                    "verbose = 'true'" to messages.errorScriptBoolean("verbose"),
                    "loops = 1.5" to messages.errorScriptInteger("loops"),
                    "loops = 0" to messages.errorConfigurationLoopsInvalidRange,
                    "paths = { [2] = 'gap' }" to messages.errorScriptList("paths"),
                    "log = { file = false }" to messages.errorScriptString("log.file"),
                    "log = false" to messages.errorScriptTable("log"),
                    "log = { typo = true }" to messages.errorScriptUnknownKey("log.typo"),
                    "preamble = { prepend = 'sometimes' }" to
                        messages.errorConfigurationPrepend("sometimes"),
                    "preamble = { definitions = { [1] = {} } }" to
                        messages.errorScriptStringKeys("preamble.definitions")
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
            "return true" to messages.errorScriptTable("specification"),
            "return { files = false }" to messages.errorScriptTable("project.files"),
            "return { name = ' ', files = {} }" to messages.errorScriptNonblank("project.name"),
            "return { files = { ['a.tex'] = { priority = false } } }" to
                messages.errorScriptInteger("project.files[a.tex].priority"),
            "return { files = { ['a.tex'] = {} }, typo = true }" to
                messages.errorScriptUnknownKey("project.typo")
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
