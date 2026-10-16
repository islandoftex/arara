// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.encoding.Base64
import kotlin.test.assertEquals
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
 * removal semantics, and the record format of the on-disk cache - the
 * round trips, the guard against foreign files, the rejections of
 * malformed, duplicated and stale records, the idempotence of the
 * sorted writes, and the wrapping of unwritable save targets.
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
        val tmp = tempDir.resolve("arara.db").toMPPPath()
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
    fun `round trips every file name through the record grammar`(
        @TempDir tempDir: Path
    ) {
        val hostile = mutableMapOf(
            "/proj/victim.tex 1234567890\njunk.tex" to 7L,
            "/proj/dir with spaces/file name.tex" to 8L,
            "/proj/ação 目录/ファイル.tex" to 9L
        )
        val tmp = tempDir.resolve("arara.db").toMPPPath()
        Database(hostile).save(tmp)
        assertEquals(
            Database(hostile),
            Database.load(tmp),
            "the record format round trips every file name as it is"
        )
        assertFalse(
            MPPPath("/proj/victim.tex") in Database.load(tmp),
            "a crafted file name cannot forge the record of another file"
        )
        assertEquals(
            hostile.size,
            tmp.readText().lines().count { it.isNotBlank() },
            "every file name keeps its own single line"
        )
    }

    @Test
    fun `refuses a file that does not follow the record grammar`(
        @TempDir tempDir: Path
    ) {
        val file = tempDir.resolve("arara.db")
        Files.writeString(file, "just: plain content")
        val exception = assertFailsWith<AraraException>(
            "a file outside the record grammar is not a database"
        ) {
            Database.load(file.toMPPPath())
        }
        assertTrue(
            exception.message?.contains("arara.db") == true,
            "the load error names the file: ${exception.message}"
        )
    }

    @Test
    fun `refuses the record format of an older version`(
        @TempDir tempDir: Path
    ) {
        val file = tempDir.resolve("arara.db")
        Files.writeString(file, "!database\nmap:\n  \"/old\": 1\n")
        assertFailsWith<AraraException>(
            "the stale format is not read anymore"
        ) {
            Database.load(file.toMPPPath())
        }
    }

    @Test
    fun `refuses malformed records`(@TempDir tempDir: Path) {
        val file = tempDir.resolve("arara.db")
        listOf(
            "not-a-record",
            " leading separator 123",
            "token not-a-number",
            "!!! 123",
            "token 123 456"
        ).forEach { content ->
            Files.writeString(file, content)
            assertFailsWith<AraraException>(
                "the malformed record reports the load error: $content"
            ) {
                Database.load(file.toMPPPath())
            }
        }
    }

    @Test
    fun `refuses duplicated records`(@TempDir tempDir: Path) {
        val file = tempDir.resolve("arara.db")
        val record = "${Base64.encode("/dup".encodeToByteArray())} 1"
        Files.writeString(file, "$record\n$record\n")
        assertFailsWith<AraraException>(
            "a duplicated record breaks the coherence of the file"
        ) {
            Database.load(file.toMPPPath())
        }
    }

    @Test
    fun `saves an empty database as an empty file`(@TempDir tempDir: Path) {
        val tmp = tempDir.resolve("arara.db").toMPPPath()
        Database().save(tmp)
        assertEquals("", tmp.readText(), "an empty database is an empty file")
        assertEquals(
            Database(),
            Database.load(tmp),
            "the empty file loads back empty"
        )
    }

    @Test
    fun `writes sorted records and identical bytes across saves`(
        @TempDir tempDir: Path
    ) {
        val tmp = tempDir.resolve("arara.db").toMPPPath()
        val db = Database(
            mutableMapOf(
                "/b.tex" to 2L,
                "/a.tex" to 1L,
                "/dir with spaces/c c.tex" to 3L
            )
        )
        db.save(tmp)
        val first = tmp.readText()
        db.save(tmp)
        assertEquals(
            first,
            tmp.readText(),
            "saving the same database writes the same bytes"
        )
        assertTrue(first.endsWith("\n"), "a non-empty file ends with a newline")
        val names = first.lines()
            .filter { it.isNotBlank() }
            .map { Base64.decode(it.substringBefore(' ')).decodeToString() }
        assertEquals(
            listOf("/a.tex", "/b.tex", "/dir with spaces/c c.tex"),
            names,
            "the records sit in the sorted order of their names"
        )
    }

    @Test
    fun `wraps unwritable save targets`(@TempDir tempDir: Path) {
        // a directory cannot hold the database text, so the save fails
        // and reports the localized error naming the target
        val exception = assertFailsWith<AraraException>(
            "an unwritable target reports the save error"
        ) {
            Database().save(tempDir.toMPPPath())
        }
        assertTrue(
            exception.message?.contains("I could not save") == true,
            "the localized save error carries: ${exception.message}"
        )
    }
}
