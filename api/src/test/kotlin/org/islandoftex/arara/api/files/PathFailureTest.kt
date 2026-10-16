// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.IOException
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraIOException
import org.junit.jupiter.api.Assumptions.assumeTrue

class PathFailureTest {
    @Test
    fun nativeReadWriteAndMetadataFailuresKeepTheirOriginalException() {
        val root = Files.createTempDirectory("arara path failures ").toMPPPath()
        try {
            val missing = root.resolve("missing.txt")
            val operations: List<() -> Unit> = listOf(
                { missing.readLines() }, { missing.readText() },
                { missing.fileSize }, { missing.lastModified },
                { root.resolve("absent/child.txt").writeText("text") }
            )
            for (operation in operations) {
                val error = assertFailsWith<AraraIOException> { operation() }
                assertTrue(error.exception is IOException, error.toString())
                assertTrue(error.message.orEmpty().contains(root.toString()))
            }
            assertFalse(missing.exists)
            assertFailsWith<AraraIOException> { root.readLines() }
            assertFailsWith<AraraIOException> { root.readText() }
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun emptyFilesAndLineTerminatorsKeepNativeReadLinesBehavior() {
        val root = Files.createTempDirectory("arara path lines ").toMPPPath()
        try {
            val file = root.resolve("lines.txt")
            for ((text, lines) in listOf(
                "" to emptyList(), "\n" to listOf(""),
                "first\r\nsecond\n" to listOf("first", "second")
            )) {
                file.writeText(text)
                assertEquals(lines, file.readLines())
            }
            file.writeText("last", append = true)
            assertEquals(listOf("first", "second", "last"), file.readLines())
            val missing = root.resolve("append.txt")
            assertFailsWith<AraraIOException> { missing.writeText("text", append = true) }
            assertFalse(missing.exists)
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun directoryFailuresKeepTheirNativeCauseAndNeverDeleteFiles() {
        val root = Files.createTempDirectory("arara directory failures ").toMPPPath()
        try {
            val file = root.resolve("sentinel").apply { writeText("preserve") }
            val failure = assertFailsWith<AraraIOException> { file.resolve("child").createDirectories() }
            assertTrue(failure.exception is IOException)
            assertFailsWith<AraraIOException> { file.createDirectories() }
            assertFailsWith<AraraIOException> { file.removeDirectory() }
            assertEquals("preserve", file.readText())
            root.resolve("missing").removeDirectory()
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun recursiveRemovalDoesNotFollowDirectoryLinks() {
        val root = Files.createTempDirectory("arara directory links ").toMPPPath()
        try {
            val outside = root.resolve("outside").apply { createDirectories() }
            val sentinel = outside.resolve("keep.txt").apply { writeText("preserve") }
            val tree = root.resolve("tree").apply { createDirectories() }
            val supported = try {
                Files.createSymbolicLink(tree.resolve("link").toJVMPath(), outside.toJVMPath())
                true
            } catch (_: IOException) {
                false
            } catch (_: UnsupportedOperationException) {
                false
            } catch (_: SecurityException) {
                false
            }
            assumeTrue(supported, "Symbolic links are unavailable on this host")
            tree.removeDirectory()
            assertFalse(tree.exists)
            assertEquals("preserve", sentinel.readText())
        } finally {
            root.removeDirectory()
        }
    }
}
