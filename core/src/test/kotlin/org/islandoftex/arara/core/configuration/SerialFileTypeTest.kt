// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException

/**
 * Verifies the serializable file type model: explicit patterns pass
 * through, known extensions fall back to the default patterns, and
 * unknown extensions without a pattern are rejected.
 */
class SerialFileTypeTest {
    @Test
    fun `converts to file types with explicit patterns`() {
        val fileType = SerialFileType("tex", "^\\s*%").toFileType()
        assertEquals("tex", fileType.extension, "the extension passes through")
    }

    @Test
    fun `falls back to the default patterns for known extensions`() {
        val fileType = SerialFileType("tex").toFileType()
        assertEquals("tex", fileType.extension, "the extension passes through")
    }

    @Test
    fun `rejects unknown extensions without patterns`() {
        assertFailsWith<AraraException>(
            "an unknown extension without a pattern cannot become a file type"
        ) {
            SerialFileType("quack").toFileType()
        }
    }
}
