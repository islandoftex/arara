// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.junit.jupiter.api.io.TempDir

class DatabaseTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `normalize paths`() {
        val db = Database()
        db[MPPPath("./././")] = 1L
        assertTrue(MPPPath("./") in db)
    }

    @Test
    fun `remove existent elements`() {
        val db = Database()
        assertFalse(MPPPath(".") in db)
        db[MPPPath(".")] = 1L
        assertTrue(MPPPath(".") in db)
        db.remove(MPPPath("."))
        assertFalse(MPPPath(".") in db)
    }

    @Test
    fun `throw when removing non-existent path`() {
        assertContains(assertFailsWith<NoSuchElementException> { Database().remove(MPPPath(".")) }
            .message.orEmpty(), "non-existent path")
    }

    @Test
    fun `be able to perform save-load cycle`() {
        val tmp = temporaryDirectory.resolve("db.yaml").toMPPPath()
        val db = Database()
        db[MPPPath(".")] = 1L
        db[MPPPath("quack")] = 2L
        db.save(tmp)
        val dbCopy = Database.load(tmp)
        assertTrue(MPPPath(".") in db)
        assertTrue(MPPPath(".") in dbCopy)
        assertTrue(MPPPath("quack") in db)
        assertTrue(MPPPath("quack") in dbCopy)
        assertFalse(MPPPath("no") in db)
        assertFalse(MPPPath("no") in dbCopy)
    }
}
