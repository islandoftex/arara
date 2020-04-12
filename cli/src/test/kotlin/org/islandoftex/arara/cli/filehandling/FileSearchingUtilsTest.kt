// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.islandoftex.arara.api.files.ProjectFile

class FileSearchingUtilsTest : ShouldSpec({
    // TODO: test implicit extensions

    fun prepareFileSystem(): Path {
        val tempDir = Files.createTempDirectory(System.nanoTime().toString())
        tempDir.resolve("quack/quack").toFile().mkdirs()
        listOf("quack", "quack/quack", "quack/quack/quack").forEach {
            tempDir.resolve("$it.tex").toFile().writeText(" ")
            tempDir.resolve("$it.txt").toFile().writeText(" ")
        }
        return tempDir
    }

    should("fail looking up inexistent file") {
        FileSearchingUtils.lookupFile("QUACK", File(".")) shouldBe null
    }
    should("fail on existing directory") {
        FileSearchingUtils.lookupFile("../buildSrc", File(".")) shouldBe null
    }
    should("succeed finding tex file with extension") {
        val pathToTest = Paths.get("src/test/resources/executiontests/changes/changes.tex")
        val projectFile = FileSearchingUtils.lookupFile(
                pathToTest.toString(),
                File(".")
        ) as ProjectFile
        projectFile.path.toFile().canonicalFile.absolutePath shouldBe
                File(".").resolve(pathToTest.toFile()).canonicalFile.absolutePath
        projectFile.fileType.extension shouldBe "tex"
        projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
    }
    should("succeed finding tex file without extension") {
        val projectFile = FileSearchingUtils.lookupFile(
                Paths.get("src/test/resources/executiontests/changes/changes").toString(),
                File(".")
        ) as ProjectFile
        projectFile.path.toFile().canonicalFile.absolutePath shouldBe
                File("./src/test/resources/executiontests/changes/changes.tex").canonicalFile.absolutePath
        projectFile.fileType.extension shouldBe "tex"
        projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
    }

    should("find file by extension") {
        val tempDir = prepareFileSystem()
        FileSearchingUtils.listFilesByExtensions(tempDir.toFile(),
                listOf("tex"), false).toSet() shouldBe
                setOf(tempDir.resolve("quack.tex").toFile())
        FileSearchingUtils.listFilesByExtensions(tempDir.toFile(),
                listOf("tex"), true).toSet() shouldBe
                listOf("quack", "quack/quack", "quack/quack/quack")
                        .map { tempDir.resolve("$it.tex").toFile() }.toSet()
    }
    should("find file by pattern") {
        val tempDir = prepareFileSystem()
        FileSearchingUtils.listFilesByPatterns(tempDir.toFile(),
                listOf("*q*.txt"), false).toSet() shouldBe
                setOf(tempDir.resolve("quack.txt").toFile())
        FileSearchingUtils.listFilesByPatterns(tempDir.toFile(),
                listOf("q*.txt"), true).toSet() shouldBe
                listOf("quack", "quack/quack", "quack/quack/quack")
                        .map { tempDir.resolve("$it.txt").toFile() }.toSet()
    }
})
