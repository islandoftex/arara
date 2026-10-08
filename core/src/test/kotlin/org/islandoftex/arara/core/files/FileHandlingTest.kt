// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath

/**
 * Verifies the file handling helpers: extension swapping, directory
 * relationships and CRC32 checksums.
 */
class FileHandlingTest {
    @Test
    fun `changes the extension of a file with an extension`() {
        assertEquals(
            MPPPath("quack.log").fileName,
            FileHandling.changeExtension(MPPPath("quack.tex"), "log").fileName,
            "the sibling path with the new extension"
        )
    }

    @Test
    fun `changes the extension of a file without an extension`() {
        assertEquals(
            MPPPath("quack.log").fileName,
            FileHandling.changeExtension(MPPPath("quack"), "log").fileName,
            "the sibling path with the new extension"
        )
    }

    @Test
    fun `a nested path is a subdirectory of its parent, and not the other way around`() {
        assertTrue(
            FileHandling.isSubDirectory(MPPPath("../docs"), MPPPath("..")),
            "a nested directory is a subdirectory of its parent"
        )
        assertFalse(
            FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../docs")),
            "a parent is not a subdirectory of its child"
        )
    }

    @Test
    fun `does not treat files as subdirectories`() {
        assertFalse(
            FileHandling.isSubDirectory(MPPPath("../LICENSE"), MPPPath("..")),
            "a file is not a subdirectory of its parent directory"
        )
        assertFalse(
            FileHandling.isSubDirectory(MPPPath(".."), MPPPath("../LICENSE")),
            "a directory is not a subdirectory of a file"
        )
    }

    @Test
    fun `fails generating a checksum for an inexistent file`() {
        assertFailsWith<AraraException>(
            "hashing an inexistent file must be rejected"
        ) {
            FileHandling.calculateHash(MPPPath("QUACK"))
        }
    }

    @Test
    fun `generates the correct checksum`() {
        assertEquals(
            3302180338,
            FileHandling.calculateHash(MPPPath("src/test/resources/checksum.txt")),
            "CRC32 checksum of the immutable fixture file"
        )
    }
}
