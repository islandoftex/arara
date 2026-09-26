// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Verifies the file change database: path normalization on insertion,
 * removal semantics and the YAML save-load round trip.
 */
class DatabaseTest {
    @Test
    fun `normalizes paths on insertion`() {
        val db = Database()
        db[MPPPath("./././")] = 1L
        assertTrue(
            MPPPath("./") in db,
            "the database must normalize the inserted path"
        )
    }

    @Test
    fun `removes existent elements`() {
        val db = Database()
        assertFalse(
            MPPPath(".") in db,
            "the database does not contain the path before insertion"
        )
        db[MPPPath(".")] = 1L
        assertTrue(
            MPPPath(".") in db,
            "the database contains the path after insertion"
        )
        db.remove(MPPPath("."))
        assertFalse(
            MPPPath(".") in db,
            "the database does not contain the path after removal"
        )
    }

    @Test
    fun `throws when removing a nonexistent path`() {
        val exception = assertFailsWith<NoSuchElementException>(
            "removing an unknown path must be rejected"
        ) {
            Database().remove(MPPPath("."))
        }
        assertTrue(
            exception.message?.contains("non-existent path") == true,
            "the removal error names the non-existent path"
        )
    }

    @Test
    fun `performs the save load cycle`(@TempDir tempDir: Path) {
        val tmp = tempDir.resolve("arara.yaml").toMPPPath()
        val db = Database()
        db[MPPPath(".")] = 1L
        db[MPPPath("quack")] = 2L
        db.save(tmp)
        val dbCopy = Database.load(tmp)
        assertTrue(MPPPath(".") in db, "the original database holds the first entry")
        assertTrue(
            MPPPath(".") in dbCopy,
            "the loaded copy holds the first entry"
        )
        assertTrue(
            MPPPath("quack") in db,
            "the original database holds the second entry"
        )
        assertTrue(
            MPPPath("quack") in dbCopy,
            "the loaded copy holds the second entry"
        )
        assertFalse(MPPPath("no") in db, "the original database omits an unknown path")
        assertFalse(
            MPPPath("no") in dbCopy,
            "the loaded copy omits an unknown path"
        )
    }

    @Test
    fun `rejects databases without the header`(@TempDir tempDir: Path) {
        val file = tempDir.resolve("arara.yaml")
        Files.writeString(file, "just: plain content")
        assertFailsWith<AraraException>(
            "a database without its header is not a database"
        ) {
            Database.load(file.toMPPPath())
        }
    }

    @Test
    fun `wraps unparseable databases`(@TempDir tempDir: Path) {
        val file = tempDir.resolve("arara.yaml")
        Files.writeString(file, "!database\n{ broken")
        assertFailsWith<AraraException>(
            "an unparseable database reports the load error"
        ) {
            Database.load(file.toMPPPath())
        }
    }
}
