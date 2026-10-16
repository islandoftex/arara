// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.localization.BrazilianPortugueseMessages
import org.islandoftex.arara.core.localization.DutchMessages
import org.islandoftex.arara.core.localization.EnglishMessages
import org.islandoftex.arara.core.localization.FrenchMessages
import org.islandoftex.arara.core.localization.GermanMessages
import org.islandoftex.arara.core.localization.ItalianMessages
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.localization.NorfolkMessages

class FileResolutionDiagnosticsTest {
    @Test
    fun errorsNameTheRealFailureInEveryLocaleWithoutChangingResolution() {
        val root = Files.createTempDirectory("arara resolution diagnostics ").toMPPPath()
        val previous = LanguageController.messages
        try {
            root.resolve("existing.xyz").writeText("content")
            root.resolve("ordinary.lua").writeText("error('must not be executed')")
            root.resolve("completed.tex").writeText("content")
            root.resolve("compound.source.tex").writeText("content")
            val first = FileType("tex", "^%")
            val compound = FileType("source.tex", "^%")
            val options = ExecutionOptions(fileTypes = listOf(first, compound, first))
            for (messages in listOf(EnglishMessages, GermanMessages, ItalianMessages, DutchMessages,
                BrazilianPortugueseMessages, NorfolkMessages, FrenchMessages)) {
                LanguageController.messages = messages
                fun error(name: String, configured: ExecutionOptions = options): String? =
                    assertFailsWith<AraraException> { FileSearching.resolveFile(name, root, configured) }.message
                assertEquals(messages.errorDiscoverFileUnknownExtension("existing.xyz", "tex, source.tex"),
                    error("existing.xyz"))
                assertEquals(messages.errorDiscoverFileUnknownLuaExtension("ordinary.lua", "tex, source.tex"),
                    error("ordinary.lua"))
                assertEquals(messages.errorDiscoverFileFileNotFound("missing", "tex, source.tex"), error("missing"))
                val safe = options.copy(executionMode = ExecutionMode.SAFE_RUN)
                assertEquals(messages.errorDiscoverFileSafeModeNoCompletion("completed"), error("completed", safe))
                assertEquals(root.resolve("completed.tex"), FileSearching.resolveFile("completed", root, options).path)
                assertEquals(first, FileSearching.resolveFile("completed.tex", root, safe).fileType)
                val compoundFirst = options.copy(fileTypes = listOf(compound, first))
                assertEquals(compound, FileSearching.resolveFile("compound.source.tex", root, compoundFirst).fileType)
                assertEquals(compound, FileSearching.resolveFile("compound", root, compoundFirst).fileType)
                val lua = FileType("lua", "^--")
                assertEquals(lua, FileSearching.resolveFile("ordinary.lua", root,
                    options.copy(fileTypes = listOf(lua))).fileType)
            }
        } finally {
            LanguageController.messages = previous
            root.removeDirectory()
        }
    }
}
