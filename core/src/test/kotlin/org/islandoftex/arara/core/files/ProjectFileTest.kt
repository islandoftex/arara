// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.core.rules.DirectiveFetchingHooks
import org.islandoftex.arara.core.rules.Directives

/**
 * Verifies that the project file extraction honors the header-only
 * parameter it receives, instead of silently consulting the global
 * execution options.
 */
class ProjectFileTest {
    private val installedHooks = Directives.hooks

    @AfterTest
    fun restoreHooks() {
        Directives.hooks = installedHooks
    }

    /**
     * A minimal directive stub collecting what the extraction produced.
     */
    private class StubDirective(
        override val identifier: String,
        override val parameters: Map<String, Any>,
        override val conditional: DirectiveConditional,
        override val lineNumbers: List<Int>
    ) : Directive {
        override fun execute(): Int = 0
    }

    @Test
    fun `respects the header only parameter`() {
        val file = Files.createTempFile("arara-project-file-", ".tex")
        try {
            Files.writeString(
                file,
                """
                % arara: header
                \documentclass{article}
                % arara: body
                """.trimIndent()
            )
            Directives.hooks = DirectiveFetchingHooks(
                processPotentialDirective = { _, text -> text },
                buildDirectiveRaw = { id, _, conditional, lines ->
                    StubDirective(id, mapOf(), conditional, lines)
                }
            )
            val projectFile = ProjectFile(
                org.islandoftex.arara.api.files.MPPPath(file.toString()),
                FileType("tex", "^%.*")
            )

            assertEquals(
                1, projectFile.fetchDirectives(true).size,
                "the header only extraction stops at the document body"
            )
            assertEquals(
                2, projectFile.fetchDirectives(false).size,
                "the whole file extraction reaches beyond the header"
            )
        } finally {
            Files.deleteIfExists(file)
        }
    }
}
