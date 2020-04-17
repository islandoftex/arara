// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.configuration.ExecutionOptions

@ExperimentalTime
class FileSearchingTest : ShouldSpec({
    "file listings" {
        fun prepareFileSystem(): Path {
            val tempDir = Files.createTempDirectory(System.nanoTime().toString())
            tempDir.resolve("quack/quack").toFile().mkdirs()
            listOf("quack", "quack/quack", "quack/quack/quack").forEach {
                tempDir.resolve("$it.tex").toFile().writeText(" ")
                tempDir.resolve("$it.txt").toFile().writeText(" ")
            }
            return tempDir
        }

        should("find file in listing by extension") {
            val tempDir = prepareFileSystem()
            FileSearching.listFilesByExtensions(tempDir.toFile(),
                    listOf("tex"), false).toSet() shouldBe
                    setOf(tempDir.resolve("quack.tex").toFile())
            FileSearching.listFilesByExtensions(tempDir.toFile(),
                    listOf("tex"), true).toSet() shouldBe
                    listOf("quack", "quack/quack", "quack/quack/quack")
                            .map { tempDir.resolve("$it.tex").toFile() }.toSet()
        }

        should("find file in listing by pattern") {
            val tempDir = prepareFileSystem()
            FileSearching.listFilesByPatterns(tempDir.toFile(),
                    listOf("*q*.txt"), false).toSet() shouldBe
                    setOf(tempDir.resolve("quack.txt").toFile())
            FileSearching.listFilesByPatterns(tempDir.toFile(),
                    listOf("q*.txt"), true).toSet() shouldBe
                    listOf("quack", "quack/quack", "quack/quack/quack")
                            .map { tempDir.resolve("$it.txt").toFile() }.toSet()
        }
    }

    "file lookup" {
        should("fail looking up inexistent file") {
            FileSearching.lookupFile("QUACK", Paths.get("."),
                    ExecutionOptions()) shouldBe null
        }

        should("fail on existing directory") {
            FileSearching.lookupFile("../buildSrc", Paths.get("."),
                    ExecutionOptions()) shouldBe null
        }

        should("succeed finding tex file with extension") {
            withContext(Dispatchers.IO) {
                val testDir = Files.createTempDirectory(System.nanoTime().toString())
                val parent = Files.createDirectories(testDir.resolve("test/quack/"))
                parent.resolve("changes.tex").toFile().writeText("quack")
                val pathToTest = parent.resolve("changes.tex")
                val projectFile = FileSearching.lookupFile(
                        pathToTest.toString(),
                        testDir,
                        ExecutionOptions()
                ) as ProjectFile
                projectFile.path.toFile().canonicalFile.absolutePath shouldBe
                        pathToTest.toFile().canonicalFile.absolutePath
                projectFile.fileType.extension shouldBe "tex"
                projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
            }
        }

        should("succeed finding tex file without extension") {
            withContext(Dispatchers.IO) {
                val testDir = Files.createTempDirectory(System.nanoTime().toString())
                val parent = Files.createDirectories(testDir.resolve("test/quack/"))
                parent.resolve("changes.tex").toFile().writeText("quack")
                val projectFile = FileSearching.lookupFile(
                        parent.resolve("changes").toString(),
                        testDir,
                        ExecutionOptions()
                ) as ProjectFile
                projectFile.path.toFile().canonicalFile.absolutePath shouldBe
                        parent.resolve("changes.tex").toFile().canonicalFile.absolutePath
                projectFile.fileType.extension shouldBe "tex"
                projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
            }
        }

        should("succeed finding tex file with extension in safe mode") {
            withContext(Dispatchers.IO) {
                val testDir = Files.createTempDirectory(System.nanoTime().toString())
                val parent = Files.createDirectories(testDir.resolve("test/quack/"))
                parent.resolve("changes.tex").toFile().writeText("quack")
                val pathToTest = parent.resolve("changes.tex")
                val projectFile = FileSearching.lookupFile(
                        pathToTest.toString(),
                        testDir,
                        ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
                ) as ProjectFile
                projectFile.path.toFile().canonicalFile.absolutePath shouldBe
                        pathToTest.toFile().canonicalFile.absolutePath
                projectFile.fileType.extension shouldBe "tex"
                projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
            }
        }

        should("not succeed finding tex file without extension in safe mode") {
            FileSearching.lookupFile(
                    Paths.get("src/test/resources/executiontests/changes/changes").toString(),
                    Paths.get("."),
                    ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
            ) shouldBe null
        }
    }
})
