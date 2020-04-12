// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path

class FileSearchingTest : ShouldSpec({
    fun prepareFileSystem(): Path {
        val tempDir = Files.createTempDirectory(System.nanoTime().toString())
        tempDir.resolve("quack/quack").toFile().mkdirs()
        listOf("quack", "quack/quack", "quack/quack/quack").forEach {
            tempDir.resolve("$it.tex").toFile().writeText(" ")
            tempDir.resolve("$it.txt").toFile().writeText(" ")
        }
        return tempDir
    }

    should("find file by extension") {
        val tempDir = prepareFileSystem()
        FileSearching.listFilesByExtensions(tempDir.toFile(),
                listOf("tex"), false).toSet() shouldBe
                setOf(tempDir.resolve("quack.tex").toFile())
        FileSearching.listFilesByExtensions(tempDir.toFile(),
                listOf("tex"), true).toSet() shouldBe
                listOf("quack", "quack/quack", "quack/quack/quack")
                        .map { tempDir.resolve("$it.tex").toFile() }.toSet()
    }

    should("find file by pattern") {
        val tempDir = prepareFileSystem()
        FileSearching.listFilesByPatterns(tempDir.toFile(),
                listOf("*q*.txt"), false).toSet() shouldBe
                setOf(tempDir.resolve("quack.txt").toFile())
        FileSearching.listFilesByPatterns(tempDir.toFile(),
                listOf("q*.txt"), true).toSet() shouldBe
                listOf("quack", "quack/quack", "quack/quack/quack")
                        .map { tempDir.resolve("$it.txt").toFile() }.toSet()
    }
})
