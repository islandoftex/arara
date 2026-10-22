// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.localization.LanguageController
import java.io.IOException
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FileIoRegressionTest {
    @Test
    fun missingDirectiveSourcesUseTheLocalizedErrorAndRetainTheReadFailure() {
        val root = Files.createTempDirectory("arara missing source ").toMPPPath()
        try {
            val file = ProjectFile(root.resolve("missing.tex"), FileType("tex", "^%"))
            val failure = assertFailsWith<AraraException> { file.fetchDirectives(true) }
            assertEquals(LanguageController.messages.errorExtractorIoError, failure.message)
            val pathFailure = failure.exception
            assertTrue(pathFailure is AraraIOException)
            assertTrue(pathFailure.exception is IOException)
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun streamingCrc32KeepsTheKnownUnsignedResultAndWrapsReadFailures() {
        val root = Files.createTempDirectory("arara crc32 ").toMPPPath()
        try {
            val file = root.resolve("checksum.bin").apply { writeText("123456789") }
            assertEquals(3421780262L, FileHandling.calculateHash(file))
            file.writeText("123456789".repeat(10000))
            assertEquals(989182688L, FileHandling.calculateHash(file))
            file.writeText("")
            assertEquals(0L, FileHandling.calculateHash(file))
            val failure = assertFailsWith<AraraException> {
                FileHandling.calculateHash(root.resolve("missing.bin"))
            }
            assertEquals(LanguageController.messages.errorCalculateHashIoException, failure.message)
            assertTrue(failure.exception is IOException)
        } finally {
            root.removeDirectory()
        }
    }
}
