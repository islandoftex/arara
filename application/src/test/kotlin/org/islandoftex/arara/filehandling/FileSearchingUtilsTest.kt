package org.islandoftex.arara.filehandling

import io.kotlintest.shouldBe
import io.kotlintest.specs.ShouldSpec
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

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
        val lookupFile = FileSearchingUtils::class.declaredMemberFunctions
                .first { it.name == "lookupFile" }
        lookupFile.isAccessible = true
        lookupFile.call(FileSearchingUtils, "QUACK") shouldBe null
    }

    should("fail on existing directory") {
        val lookupFile = FileSearchingUtils::class.declaredMemberFunctions
                .first { it.name == "lookupFile" }
        lookupFile.isAccessible = true
        lookupFile.call(FileSearchingUtils, "../buildSrc") shouldBe null
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
