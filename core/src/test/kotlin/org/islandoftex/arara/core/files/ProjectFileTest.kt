// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.core.rules.DirectiveFetchingHooks
import org.islandoftex.arara.core.rules.Directives

/**
 * Verifies the project file model: the directive extraction honors
 * the header-only parameter it receives instead of silently
 * consulting the global execution options, an unreadable path
 * reaches the extraction error, and files compare by their
 * ingredients - path, type and priority alike.
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

    @Test
    fun `directive extraction rejects unreadable paths with the extraction error`() {
        val directory = Files.createTempDirectory("arara-unreadable")
        val projectFile = ProjectFile(
            org.islandoftex.arara.api.files.MPPPath(directory.toString()),
            FileType("tex", "^%.*")
        )
        // an unreadable path reaches the extraction error instead of
        // crashing with a raw system exception
        assertFailsWith<AraraException> { projectFile.fetchDirectives(true) }
    }

    @Test
    fun `project files compare by their ingredients`() {
        val first = ProjectFile(
            org.islandoftex.arara.api.files.MPPPath("a.tex"),
            FileType("tex", "^%.*")
        )
        val same = ProjectFile(
            org.islandoftex.arara.api.files.MPPPath("a.tex"),
            FileType("tex", "^%.*")
        )
        val otherPath = ProjectFile(
            org.islandoftex.arara.api.files.MPPPath("b.tex"),
            FileType("tex", "^%.*")
        )
        val otherType = ProjectFile(
            org.islandoftex.arara.api.files.MPPPath("a.tex"),
            FileType("log", "^%.*")
        )
        val otherPriority = ProjectFile(
            org.islandoftex.arara.api.files.MPPPath("a.tex"),
            FileType("tex", "^%.*"),
            1
        )
        assertEquals(first, first, "a project file equals itself")
        assertEquals(first, same, "identical ingredients compare equal")
        assertEquals(first.hashCode(), same.hashCode())
        assertNotEquals(first, otherPath)
        assertNotEquals(first, otherType)
        assertNotEquals(first, otherPriority)
        assertFalse(first.equals("not a project file"))
        assertTrue(
            first.toString().contains("a.tex"),
            "the string form names the file"
        )
    }
}
