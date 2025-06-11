// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class FileTypeTest {
    @Test
    fun shouldOnlyDependOnExtensionForEquality() {
        assertEquals(FileType("test", "^\\s*"), FileType("test", "^\\s*%\\s+"))
        assertEquals(
            FileType("test", "^\\s*%\\s+"),
            FileType("test", "^\\s*%\\s+"),
        )
    }

    @Test
    fun shouldOnlyDependOnExtensionForInequality() {
        assertNotEquals(
            FileType("test", "^\\s*"),
            FileType("tes", "^\\s*%\\s+"),
        )
        assertNotEquals(
            FileType("test", "^\\s*%\\s+"),
            FileType("tes", "^\\s*%\\s+"),
        )
    }

    @Test
    fun shouldExpectValidPattern() {
        assertFailsWith<AraraException> {
            FileType("test", "[a")
        }
    }
}
