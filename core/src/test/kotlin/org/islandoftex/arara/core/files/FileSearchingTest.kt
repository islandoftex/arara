// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileSearchingTest {
    @Nested
    inner class `file listings` {
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
    }

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
        fun `fails on an existing directory`() {
            assertNull(
                FileSearching.lookupFile(
                    "../buildSrc", MPPPath("."), ExecutionOptions()
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
                ConfigurationUtils.defaultFileTypePatterns["tex"],
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
                ConfigurationUtils.defaultFileTypePatterns["tex"],
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
                ConfigurationUtils.defaultFileTypePatterns["tex"],
                projectFile.fileType.pattern,
                "file type pattern of the default tex type"
            )
        }

        @Test
        fun `does not find a tex file without its extension in safe mode`() {
            assertNull(
                FileSearching.lookupFile(
                    MPPPath("src/test/resources/executiontests/changes/changes")
                        .toString(),
                    MPPPath("."),
                    ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
                ),
                "safe mode does not complete extensions when looking up files"
            )
        }
    }
}
