// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

/**
 * Verifies the directive extraction of project file entries: the
 * project-carried directives go through the extractor with their own
 * error wrap, so an unparseable line reports the file it came from.
 */
class LuaProjectFileTest {
    @org.junit.jupiter.api.BeforeEach
    fun installHooks() {
        // the extraction requires directive-building hooks the command
        // line installs; a minimal builder stands in, as the test cares
        // about the extraction plumbing, not the built directive
        org.islandoftex.arara.core.rules.Directives.hooks =
            org.islandoftex.arara.core.rules.DirectiveFetchingHooks(
                buildDirectiveRaw = { identifier, parameters, conditional, lines ->
                    object : org.islandoftex.arara.api.rules.Directive {
                        override val identifier = identifier
                        override val parameters = emptyMap<String, Any>()
                        override val conditional = conditional
                        override val lineNumbers = lines
                        override fun execute(): Int = 0
                    }
                }
            )
    }

    @Test
    fun `extracts the directives carried by the project file`(
        @org.junit.jupiter.api.io.TempDir tempDir: java.nio.file.Path
    ) {
        val document = tempDir.resolve("main.tex")
        Files.writeString(
            document,
            "% arara: pdflatex\n\\documentclass{article}\n"
        )
        val file = LuaProjectFile(
            MPPPath(document.toString()),
            org.islandoftex.arara.core.files.FileType("tex", "^\\s*%\\s*")
        )
        file.directives = listOf("% arara: pdflatex")
        val directives = file.fetchDirectives(parseOnlyHeader = true)
        assertTrue(
            directives.isNotEmpty(),
            "the carried directives extract through the project file"
        )
    }

    @Test
    fun `wraps unextractable directives with the project file context`(
        @org.junit.jupiter.api.io.TempDir tempDir: java.nio.file.Path
    ) {
        // a directive line the extractor cannot digest reports the
        // project file it was declared in
        val document = tempDir.resolve("broken.tex")
        Files.writeString(document, "% arara: quack\n")
        val file = LuaProjectFile(
            MPPPath(document.toString()),
            org.islandoftex.arara.core.files.FileType("tex", "^\\s*%\\s*")
        )
        file.directives = listOf("% arara: :")
        val exception = assertFailsWith<AraraException>(
            "an unextractable directive wraps with the file context"
        ) {
            file.fetchDirectives(parseOnlyHeader = true)
        }
        assertTrue(
            exception.message?.contains("broken.tex") == true,
            "the wrap names the project file: ${exception.message}"
        )
    }
}
