// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Path
import kotlin.io.path.deleteExisting
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.junit.jupiter.api.io.TempDir

class JVMFileHandlingTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `detect changes on file`() {
        val file = temporaryDirectory.resolve("input.txt").toMPPPath().apply { writeText("") }
        val databaseFile = temporaryDirectory.resolve("database.yaml").toMPPPath()
        assertTrue(FileHandling.hasChanged(file, databaseFile))
        assertFalse(FileHandling.hasChanged(file, databaseFile))
        file.writeText("QUACK")
        assertTrue(FileHandling.hasChanged(file, databaseFile))
        assertFalse(FileHandling.hasChanged(file, databaseFile))
        file.writeText("QUACK2")
        assertTrue(FileHandling.hasChanged(file, databaseFile))
        file.toJVMPath().deleteExisting()
        assertTrue(FileHandling.hasChanged(file, databaseFile))
        assertFalse(FileHandling.hasChanged(file, databaseFile))

    }
}
