// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

/**
 * Verifies the file type model: equality only considers the extension,
 * and a malformed pattern is rejected at construction.
 */
class FileTypeTest {
    @Test
    fun `only depends on the extension for equality`() {
        assertEquals(
            FileType("test", "^\\s*%\\s+"), FileType("test", "^\\s*"),
            "equality only considers the extension"
        )
        assertEquals(
            FileType("test", "^\\s*%\\s+"), FileType("test", "^\\s*%\\s+"),
            "identical file types are equal"
        )
    }
    @Test
    fun `only depends on the extension for inequality`() {
        assertNotEquals(
            FileType("tes", "^\\s*%\\s+"), FileType("test", "^\\s*"),
            "different extensions make file types unequal"
        )
        assertNotEquals(
            FileType("tes", "^\\s*%\\s+"), FileType("test", "^\\s*%\\s+"),
            "different extensions make file types unequal regardless of the pattern"
        )
    }
    @Test
    fun `rejects an invalid pattern`() {
        assertFailsWith<AraraException>(
            "a malformed pattern must be rejected"
        ) {
            FileType("test", "[a")
        }
    }
}
