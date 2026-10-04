// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.DefaultConfiguration
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Verifies the file searching helpers: extension and pattern listings
 * over a temporary file system, and the reference lookup including the
 * safe mode restriction on implicit extension completion.
 */
class FileSearchingTest {
    /**
     * The recursive and flat listing helpers.
     */
    @Nested
    inner class `file listings` {
        /**
         * Builds the fixture file system inside the provided directory:
         * a nested quack chain with tex and txt files at every level.
         */
        private fun prepareFileSystem(tempDir: Path): MPPPath {
            (tempDir / "quack/quack").createDirectories()
            listOf("quack", "quack/quack", "quack/quack/quack").forEach {
                (tempDir / "$it.tex").writeText(" ")
                (tempDir / "$it.txt").writeText(" ")
            }
            return tempDir.toMPPPath()
        }

        @Test
        fun `finds files in listings by extension`(@TempDir tempDir: Path) {
            val listingDir = prepareFileSystem(tempDir)
            assertEquals(
                setOf(listingDir / "quack.tex"),
                FileSearching.listFilesByExtensions(
                    listingDir, listOf("tex"), false
                ).toSet(),
                "non-recursive extension listing"
            )
            assertEquals(
                listOf("quack", "quack/quack", "quack/quack/quack")
                    .map { listingDir / "$it.tex" }.toSet(),
                FileSearching.listFilesByExtensions(
                    listingDir, listOf("tex"), true
                ).toSet(),
                "recursive extension listing"
            )
        }

        @Test
        fun `finds files in listings by pattern`(@TempDir tempDir: Path) {
            val listingDir = prepareFileSystem(tempDir)
            assertEquals(
                setOf(listingDir / "quack.txt"),
                FileSearching.listFilesByPatterns(
                    listingDir, listOf("*q*.txt"), false
                ).toSet(),
                "non-recursive pattern listing"
            )
            assertEquals(
                listOf("quack", "quack/quack", "quack/quack/quack")
                    .map { listingDir / "$it.txt" }.toSet(),
                FileSearching.listFilesByPatterns(
                    listingDir, listOf("q*.txt"), true
                ).toSet(),
                "recursive pattern listing"
            )
        }

        @Test
        fun `returns the target itself when it is a regular file`(@TempDir tempDir: Path) {
            (tempDir / "solo.tex").writeText("quack")
            assertEquals(
                listOf((tempDir / "solo.tex").toMPPPath().toString()),
                FileSearching.listFilesByExtensions(
                    (tempDir / "solo.tex").toMPPPath(), listOf("tex"), false
                ).map { it.toString() },
                "a regular file target lists as its own match"
            )
        }

        @Test
        fun `lists the results in a sorted order`(@TempDir tempDir: Path) {
            listOf("zeta.tex", "alpha.tex", "mid.tex").forEach {
                (tempDir / it).writeText("quack")
            }
            assertEquals(
                listOf("alpha.tex", "mid.tex", "zeta.tex"),
                FileSearching.listFilesByExtensions(
                    tempDir.toMPPPath(), listOf("tex"), false
                ).map { it.fileName },
                "the search results come sorted by name"
            )
        }

        @Test
        fun `includes a link to a file in the results`(@TempDir tempDir: Path) {
            val target = tempDir / "target.tex"
            target.writeText("quack")
            val link = tempDir / "link.tex"
            assumeTrue(
                try {
                    Files.createSymbolicLink(link, target)
                    true
                } catch (_: IOException) {
                    false
                } catch (_: UnsupportedOperationException) {
                    false
                },
                "the host cannot create symlinks"
            )
            assertTrue(
                FileSearching.listFilesByExtensions(
                    tempDir.toMPPPath(), listOf("tex"), false
                ).map { it.fileName }.contains("link.tex"),
                "a link to a file matches like the file it points to"
            )
        }
    }

    /**
     * The reference lookup and its safe mode restriction.
     */
    @Nested
    inner class `file lookup` {
        @Test
        fun `fails looking up an inexistent file`() {
            assertNull(
                FileSearching.lookupFile("QUACK", MPPPath("."), ExecutionOptions()),
                "an inexistent reference does not resolve"
            )
        }

        @Test
        fun `fails on an existing directory`(@TempDir tempDir: Path) {
            // the directory exists and is reached by name, so the
            // miss belongs to the directory rejection alone
            (tempDir / "subdir").createDirectories()
            assertNull(
                FileSearching.lookupFile(
                    (tempDir / "subdir").toString(),
                    tempDir.toMPPPath(),
                    ExecutionOptions()
                ),
                "a directory reference does not resolve to a file"
            )
        }

        @Test
        fun `succeeds finding a tex file with its extension`(@TempDir tempDir: Path) {
            val parent = (tempDir / "test/quack").createDirectories()
            val pathToTest = parent / "changes.tex"
            pathToTest.writeText("quack")
            val projectFile = FileSearching.lookupFile(
                pathToTest.toString(),
                tempDir.toMPPPath(),
                ExecutionOptions()
            ) as ProjectFile
            assertEquals(
                pathToTest.normalize().toString(),
                projectFile.path.normalize().toString(),
                "the resolved path points at the requested file"
            )
            assertEquals("tex", projectFile.fileType.extension, "file type extension")
            assertEquals(
                DefaultConfiguration.defaultFileTypePatterns["tex"],
                projectFile.fileType.pattern,
                "file type pattern of the default tex type"
            )
        }

        @Test
        fun `succeeds finding a tex file without its extension`(@TempDir tempDir: Path) {
            val parent = (tempDir / "test/quack").createDirectories()
            (parent / "changes.tex").writeText("quack")
            val projectFile = FileSearching.lookupFile(
                (parent / "changes").toString(),
                tempDir.toMPPPath(),
                ExecutionOptions()
            ) as ProjectFile
            assertEquals(
                (parent.normalize() / "changes.tex").toString(),
                projectFile.path.normalize().toString(),
                "the reference resolves to the file with its extension"
            )
            assertEquals("tex", projectFile.fileType.extension, "file type extension")
            assertEquals(
                DefaultConfiguration.defaultFileTypePatterns["tex"],
                projectFile.fileType.pattern,
                "file type pattern of the default tex type"
            )
        }

        @Test
        fun `succeeds finding a tex file with its extension in safe mode`(
            @TempDir tempDir: Path
        ) {
            val parent = (tempDir / "test/quack").createDirectories()
            (parent / "changes.tex").writeText("quack")
            val pathToTest = parent / "changes.tex"
            val projectFile = FileSearching.lookupFile(
                pathToTest.toString(),
                tempDir.toMPPPath(),
                ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
            ) as ProjectFile
            assertEquals(
                pathToTest.normalize().toString(),
                projectFile.path.normalize().toString(),
                "the resolved path points at the requested file"
            )
            assertEquals("tex", projectFile.fileType.extension, "file type extension")
            assertEquals(
                DefaultConfiguration.defaultFileTypePatterns["tex"],
                projectFile.fileType.pattern,
                "file type pattern of the default tex type"
            )
        }

        @Test
        fun `does not find a tex file without its extension in safe mode`(
            @TempDir tempDir: Path
        ) {
            // the file exists: only the extension completion is
            // refused, so the miss belongs to the safe mode gate
            (tempDir / "changes.tex").writeText("quack")
            assertNull(
                FileSearching.lookupFile(
                    (tempDir / "changes").toString(),
                    tempDir.toMPPPath(),
                    ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
                ),
                "safe mode does not complete extensions when looking up files"
            )
        }
    }

    @Test
    fun `falls back to an empty list on a missing directory`(@TempDir tempDir: Path) {
        val missing = tempDir.resolve("does-not-exist")
        assertTrue(
            FileSearching.listFilesByExtensions(
                missing.toMPPPath(), listOf("tex"), false
            ).isEmpty(),
            "a missing directory yields no files by extension"
        )
        assertTrue(
            FileSearching.listFilesByPatterns(
                missing.toMPPPath(), listOf("*.tex"), true
            ).isEmpty(),
            "a missing directory yields no pattern matches"
        )
    }
}
