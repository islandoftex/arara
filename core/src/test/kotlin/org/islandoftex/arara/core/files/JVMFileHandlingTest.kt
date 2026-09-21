// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JVMFileHandlingTest {
    @Test
    fun `detects changes on a file`(@TempDir tempDir: Path) {
        val file = Files.createTempFile(tempDir, "arara", ".txt").toMPPPath()
        val databaseFile = Files.createTempFile(tempDir, "arara-db", ".yaml")
            .toMPPPath()
        databaseFile.toJVMPath().deleteIfExists()
        assertTrue(
            FileHandling.hasChanged(file, databaseFile),
            "a fresh file counts as changed"
        )
        assertFalse(
            FileHandling.hasChanged(file, databaseFile),
            "an unchanged file is not reported again"
        )
        file.writeText("QUACK")
        assertTrue(
            FileHandling.hasChanged(file, databaseFile),
            "a modified file counts as changed"
        )
        assertFalse(
            FileHandling.hasChanged(file, databaseFile),
            "the modified file is not reported again"
        )
        file.writeText("QUACK2")
        assertTrue(
            FileHandling.hasChanged(file, databaseFile),
            "a second modification counts as changed"
        )
        file.toJVMPath().deleteExisting()
        assertTrue(
            FileHandling.hasChanged(file, databaseFile),
            "a deleted file counts as changed"
        )
        assertFalse(
            FileHandling.hasChanged(file, databaseFile),
            "the removed entry is not reported again"
        )
    }
}
