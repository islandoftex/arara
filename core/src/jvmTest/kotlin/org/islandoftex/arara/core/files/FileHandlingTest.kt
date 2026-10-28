// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

class FileHandlingTest {
    @Test
    fun `change extension of file with extension`() {
        assertEquals(FileHandling.changeExtension(MPPPath("quack.tex"), "log").fileName, MPPPath("quack.log").fileName)
    }

    @Test
    fun `change extension of file without extension`() {
        assertEquals(FileHandling.changeExtension(MPPPath("quack"), "log").fileName, MPPPath("quack.log").fileName)
    }

    @Test
    fun `get subdirectory relationship right`() {
        assertTrue(FileHandling.isSubDirectory(MPPPath("../docs"), MPPPath("..")))
        assertFalse(FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../docs")))
    }

    @Test
    fun `not treat files as subdirectories`() {
        assertFalse(FileHandling.isSubDirectory(MPPPath("../LICENSE"), MPPPath("..")))
        assertFalse(FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../LICENSE")))
    }

    @Test
    fun `fail generating CRC sum on inexistent files`() {
        assertFailsWith<AraraException> {
            FileHandling.calculateHash(MPPPath("QUACK"))
        }
    }

    @Test
    fun `generate correct CRC sum`() {
        assertEquals(1616727774L, FileHandling.calculateHash(MPPPath("../LICENSE")))
    }
}
