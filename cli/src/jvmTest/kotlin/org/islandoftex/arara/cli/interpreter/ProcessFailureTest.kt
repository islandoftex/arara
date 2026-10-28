// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.io.IOException
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Command

class ProcessFailureTest {
    @Test
    fun `classify a failed launch while retaining its original exception`() {
        val directory = Files.createTempDirectory("arara missing command ").toMPPPath()
        try {
            val failure = assertFailsWith<AraraException> {
                CommandRunner.run(Command(listOf(directory.resolve("missing").toString())))
            }
            assertEquals(LanguageController.messages.errorRunIoException, failure.message)
            assertTrue(failure.exception is IOException)
        } finally {
            directory.removeDirectory()
        }
    }
}
