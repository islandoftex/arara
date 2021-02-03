// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions

class FileSearchingTest : ShouldSpec({
    context("file listings") {
        fun prepareFileSystem(): Path {
            val tempDir = Files.createTempDirectory(System.nanoTime().toString())
            (tempDir / "quack/quack").createDirectories()
            listOf("quack", "quack/quack", "quack/quack/quack").forEach {
                (tempDir / "$it.tex").writeText(" ")
                (tempDir / "$it.txt").writeText(" ")
            }
            return tempDir
        }

        should("find file in listing by extension") {
            val tempDir = prepareFileSystem().toMPPPath()
            FileSearching.listFilesByExtensions(tempDir,
                    listOf("tex"), false).toSet() shouldBe
                    setOf(tempDir / "quack.tex")
            FileSearching.listFilesByExtensions(tempDir,
                    listOf("tex"), true).toSet() shouldBe
                    listOf("quack", "quack/quack", "quack/quack/quack")
                            .map { tempDir / "$it.tex" }.toSet()
        }

        should("find file in listing by pattern") {
            val tempDir = prepareFileSystem().toMPPPath()
            FileSearching.listFilesByPatterns(tempDir,
                    listOf("*q*.txt"), false).toSet() shouldBe
                    setOf(tempDir / "quack.txt")
            FileSearching.listFilesByPatterns(tempDir,
                    listOf("q*.txt"), true).toSet() shouldBe
                    listOf("quack", "quack/quack", "quack/quack/quack")
                            .map { tempDir / "$it.txt" }.toSet()
        }
    }

    context("file lookup") {
        should("fail looking up inexistent file") {
            FileSearching.lookupFile("QUACK", MPPPath("."),
                    ExecutionOptions()
            ) shouldBe null
        }

        should("fail on existing directory") {
            FileSearching.lookupFile("../buildSrc", MPPPath("."),
                    ExecutionOptions()
            ) shouldBe null
        }

        should("succeed finding tex file with extension") {
            withContext(Dispatchers.IO) {
                val testDir = Files.createTempDirectory(System.nanoTime().toString())
                val parent = (testDir / "test/quack/").createDirectories()
                val pathToTest = parent / "changes.tex"
                pathToTest.writeText("quack")
                val projectFile = FileSearching.lookupFile(
                        pathToTest.toString(),
                        testDir.toMPPPath(),
                        ExecutionOptions()
                ) as ProjectFile
                projectFile.path.normalize().toString() shouldBe
                        pathToTest.normalize().toString()
                projectFile.fileType.extension shouldBe "tex"
                projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
            }
        }

        should("succeed finding tex file without extension") {
            withContext(Dispatchers.IO) {
                val testDir = Files.createTempDirectory(System.nanoTime().toString())
                val parent = (testDir / "test/quack/").createDirectories()
                (parent / "changes.tex").writeText("quack")
                val projectFile = FileSearching.lookupFile(
                        (parent / "changes").toString(),
                        testDir.toMPPPath(),
                        ExecutionOptions()
                ) as ProjectFile
                projectFile.path.normalize().toString() shouldBe
                        (parent.normalize() / "changes.tex").toString()
                projectFile.fileType.extension shouldBe "tex"
                projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
            }
        }

        should("succeed finding tex file with extension in safe mode") {
            withContext(Dispatchers.IO) {
                val testDir = Files.createTempDirectory(System.nanoTime().toString())
                val parent = (testDir / "test/quack/").createDirectories()
                (parent / "changes.tex").writeText("quack")
                val pathToTest = parent / "changes.tex"
                val projectFile = FileSearching.lookupFile(
                        pathToTest.toString(),
                        testDir.toMPPPath(),
                        ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
                ) as ProjectFile
                projectFile.path.normalize().toString() shouldBe
                        pathToTest.normalize().toString()
                projectFile.fileType.extension shouldBe "tex"
                projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
            }
        }

        should("not succeed finding tex file without extension in safe mode") {
            FileSearching.lookupFile(
                    MPPPath("src/test/resources/executiontests/changes/changes").toString(),
                    MPPPath("."),
                    ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
            ) shouldBe null
        }
    }
})
