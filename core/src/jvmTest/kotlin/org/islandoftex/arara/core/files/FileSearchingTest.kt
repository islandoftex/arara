// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.junit.jupiter.api.io.TempDir

class FileSearchingTest {
    @TempDir
    lateinit var temporaryDirectory: java.nio.file.Path

    private fun prepareFileSystem(): Path {
        val tempDir = temporaryDirectory.resolve("listing").apply { createDirectories() }
        (tempDir / "quack/quack").createDirectories()
        listOf("quack", "quack/quack", "quack/quack/quack").forEach {
            (tempDir / "$it.tex").writeText(" ")
            (tempDir / "$it.txt").writeText(" ")
        }
        return tempDir
    }

    @Test
    fun `find file in listing by extension`() {
        val tempDir = prepareFileSystem().toMPPPath()
        assertEquals(setOf(tempDir / "quack.tex"), FileSearching.listFilesByExtensions(
            tempDir,
            listOf("tex"), false
        ).toSet())
        assertEquals(listOf("quack", "quack/quack", "quack/quack/quack")
                .map { tempDir / "$it.tex" }.toSet(), FileSearching.listFilesByExtensions(
            tempDir,
            listOf("tex"), true
        ).toSet())
    }

    @Test
    fun `find file in listing by pattern`() {
        val tempDir = prepareFileSystem().toMPPPath()
        assertEquals(setOf(tempDir / "quack.txt"), FileSearching.listFilesByPatterns(
            tempDir,
            listOf("*q*.txt"), false
        ).toSet())
        assertEquals(listOf("quack", "quack/quack", "quack/quack/quack")
                .map { tempDir / "$it.txt" }.toSet(), FileSearching.listFilesByPatterns(
            tempDir,
            listOf("q*.txt"), true
        ).toSet())
    }

    private val directory get() = temporaryDirectory.toMPPPath()

    val compound = FileType("source.tex", "custom compound pattern")
    val ordinary = FileType("tex", "custom ordinary pattern")
    val options = ExecutionOptions(fileTypes = listOf(compound, ordinary))

    @Test
    fun `keep the matched compound type for an explicit relative path`() {
        (directory / "nested folder").createDirectories()
        (directory / "nested folder/document.source.tex").writeText("content")
        val found = FileSearching.lookupFile(
            "nested folder/../nested folder/document.source.tex", directory, options
        )!!
        assertEquals(compound, found.fileType)
        assertEquals(compound.pattern, found.fileType.pattern)
        assertEquals((directory / "nested folder/document.source.tex").normalize(), found.path.normalize())
    }

    @Test
    fun `keep the matched compound type when completing an extension`() {
        (directory / "completed.source.tex").writeText("content")
        val found = FileSearching.lookupFile("completed", directory, options)!!
        assertEquals(compound, found.fileType)
        assertEquals(compound.pattern, found.fileType.pattern)
    }

    @Test
    fun `use the first configured type when extensions are repeated`() {
        (directory / "priority.tex").writeText("content")
        val first = FileType("tex", "first pattern")
        val configured = options.copy(fileTypes = listOf(first, ordinary))
        for (reference in listOf("priority.tex", "priority")) {
            assertEquals(first.pattern, FileSearching.lookupFile(reference, directory, configured)!!
                .fileType.pattern)
        }
    }

    @Test
    fun `allow explicit compound files but prohibit extension completion in safe mode`() {
        (directory / "safe.source.tex").writeText("content")
        val safe = options.copy(executionMode = ExecutionMode.SAFE_RUN)
        assertNull(FileSearching.lookupFile("safe", directory, safe))
        assertEquals(compound.pattern, FileSearching.lookupFile("safe.source.tex", directory, safe)!!
            .fileType.pattern)
    }

    @Test
    fun `reject directories and existing files with unconfigured extensions`() {
        (directory / "directory.tex").createDirectories()
        (directory / "unknown.xyz").writeText("content")
        assertNull(FileSearching.lookupFile("directory.tex", directory, options))
        assertNull(FileSearching.lookupFile("unknown.xyz", directory, options))
    }

    @Test
    fun `fail looking up inexistent file`() {
        assertNull(FileSearching.lookupFile(
            "QUACK", MPPPath("."),
            ExecutionOptions()
        ))
    }

    @Test
    fun `fail on existing directory`() {
        assertNull(FileSearching.lookupFile(
            "../buildSrc", MPPPath("."),
            ExecutionOptions()
        ))
    }

    @Test
    fun `succeed finding tex file with extension`() {
        val testDir = temporaryDirectory.resolve("listing").apply { createDirectories() }
        val parent = (testDir / "test/quack/").createDirectories()
        val pathToTest = parent / "changes.tex"
        pathToTest.writeText("quack")
        val projectFile = FileSearching.lookupFile(
            pathToTest.toString(),
            testDir.toMPPPath(),
            ExecutionOptions()
        ) as ProjectFile
        assertEquals(pathToTest.normalize().toString(), projectFile.path.normalize().toString())
        assertEquals("tex", projectFile.fileType.extension)
        assertEquals(ConfigurationUtils.defaultFileTypePatterns["tex"], projectFile.fileType.pattern)
    }

    @Test
    fun `succeed finding tex file without extension`() {
        val testDir = temporaryDirectory.resolve("listing").apply { createDirectories() }
        val parent = (testDir / "test/quack/").createDirectories()
        (parent / "changes.tex").writeText("quack")
        val projectFile = FileSearching.lookupFile(
            (parent / "changes").toString(),
            testDir.toMPPPath(),
            ExecutionOptions()
        ) as ProjectFile
        assertEquals((parent.normalize() / "changes.tex").toString(), projectFile.path.normalize().toString())
        assertEquals("tex", projectFile.fileType.extension)
        assertEquals(ConfigurationUtils.defaultFileTypePatterns["tex"], projectFile.fileType.pattern)
    }

    @Test
    fun `succeed finding tex file with extension in safe mode`() {
        val testDir = temporaryDirectory.resolve("listing").apply { createDirectories() }
        val parent = (testDir / "test/quack/").createDirectories()
        (parent / "changes.tex").writeText("quack")
        val pathToTest = parent / "changes.tex"
        val projectFile = FileSearching.lookupFile(
            pathToTest.toString(),
            testDir.toMPPPath(),
            ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
        ) as ProjectFile
        assertEquals(pathToTest.normalize().toString(), projectFile.path.normalize().toString())
        assertEquals("tex", projectFile.fileType.extension)
        assertEquals(ConfigurationUtils.defaultFileTypePatterns["tex"], projectFile.fileType.pattern)
    }

    @Test
    fun `not succeed finding tex file without extension in safe mode`() {
        assertNull(FileSearching.lookupFile(
            MPPPath("src/test/resources/executiontests/changes/changes").toString(),
            MPPPath("."),
            ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
        ))
    }
}
