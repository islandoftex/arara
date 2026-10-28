// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraIOException
import org.junit.jupiter.api.io.TempDir

class JVMMPPPathTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `retain checked IO exceptions on concrete Java entry points`() {
        val operations = setOf("readLines", "readText", "metadata", "read", "listEntries", "listFiles",
            "writeText", "createDirectories", "removeDirectory")
        MPPPath::class.java.methods.filter { it.name in operations }.also { methods ->
            assertEquals(operations, methods.map { it.name }.toSet())
        }.forEach { method ->
            assertEquals(listOf(AraraIOException::class.java), method.exceptionTypes.toList())
        }
    }

    @Test
    fun `read from file`() {
        val tmp = temporaryDirectory.resolve("file.txt").toMPPPath()
        tmp.writeText("quack")
        assertEquals("quack", tmp.readText())
        assertEquals(listOf("quack"), tmp.readLines())
    }

    @Test
    fun `write to file`() {
        val tmp = temporaryDirectory.resolve("file.txt").toMPPPath()
        tmp.writeText("quack")
        assertEquals("quack", tmp.readText())
    }

    @Test
    fun `fail reading from directory`() {
        val tmp = temporaryDirectory.resolve("read").toMPPPath().apply { createDirectories() }
        assertContains(assertFailsWith<AraraIOException> {
            tmp.readText()
        }.message.orEmpty(), "read text from file")
    }

    @Test
    fun `fail writing to directory`() {
        val tmp = temporaryDirectory.resolve("write").toMPPPath().apply { createDirectories() }
        assertContains(assertFailsWith<AraraIOException> {
            tmp.writeText("quack")
        }.message.orEmpty(), "write text to file")
    }
}
