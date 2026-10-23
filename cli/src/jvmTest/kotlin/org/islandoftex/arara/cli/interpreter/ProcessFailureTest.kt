// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.io.IOException
import java.nio.file.Files
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Command

class ProcessFailureTest : ShouldSpec({
    should("classify a failed launch while retaining its original exception") {
        val directory = Files.createTempDirectory("arara missing command ").toMPPPath()
        try {
            val failure = shouldThrow<AraraException> {
                CommandRunner.run(Command(listOf(directory.resolve("missing").toString())))
            }
            failure.message shouldBe LanguageController.messages.errorRunIoException
            (failure.exception is IOException) shouldBe true
        } finally {
            directory.removeDirectory()
        }
    }
})
