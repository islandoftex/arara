// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.files.workspace.InputIgnores
import org.islandoftex.arara.core.files.workspace.InputPattern

class WorkspacePatternsTest {

    @Test
    fun slashGlobsRespectSegmentsAndHandleDoubleStarsClassesAndEscapes() {
        for ((pattern, yes, no) in listOf(
            Triple("*.tex", "book.tex", "sub/book.tex"),
            Triple("**/book.tex", "sub/book.tex", "sub/notbook.tex"),
            Triple("a/**/b", "a/b", "a/notb"),
            Triple("a/**", "a/b/c", "not-a/b"),
            Triple("file?.[a-z]", "file1.a", "file12.a"),
            Triple("[!a]*", "book", "abc"),
            Triple("\\!literal", "!literal", "literal"))) {
            assertTrue(InputPattern(pattern).matches(yes), pattern)
            assertFalse(InputPattern(pattern).matches(no), pattern)
        }
        assertFailsWith<AraraException> { InputPattern("bad[") }
        assertFailsWith<AraraException> { InputPattern("escape\\") }
    }

    @Test
    fun gitAndDockerKeepTheirDistinctScopeAndNegationRules() {
        val root = Files.createTempDirectory("arara-ignore").toMPPPath()
        try {
            root.resolve(".gitignore").writeText("""
                # comment
                !
                \#literal
                \!literal
                directories/
                **/cache/**
                /anchored
            """.trimIndent() + "\ntrailing\\ \n")
            root.resolve(".dockerignore").writeText("# comment\n.\n /./root-only/ \n*.tmp\n!keep.tmp\n")
            val ignores = InputIgnores(root)
            for (name in listOf("#literal", "!literal", "trailing ", "directories/child", "a/cache/file",
                "anchored", "root-only/child", "remove.tmp")) {
                assertTrue(ignores.excluded(root.resolve(name), false), name)
            }
            for (name in listOf("nested/anchored", "nested/root-only/child", "keep.tmp", "nested/remove.tmp")) {
                assertFalse(ignores.excluded(root.resolve(name), false), name)
            }
            assertFalse(ignores.excluded(root.resolve("directories"), false))
            assertTrue(ignores.excluded(root.resolve("directories"), true))
            assertEquals(false, ignores.excluded(root.resolve("other"), false))
        } finally {
            root.removeDirectory()
        }
    }
}
