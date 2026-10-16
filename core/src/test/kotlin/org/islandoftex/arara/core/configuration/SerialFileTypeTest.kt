// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the serializable file type model: explicit patterns pass
 * through, known extensions fall back to the default patterns, and
 * unknown extensions without a pattern are rejected with the known
 * extensions as a plain enumeration.
 */
class SerialFileTypeTest {
    @Test
    fun `converts to file types with explicit patterns`() {
        val fileType = SerialFileType("tex", "^\\s*%").toFileType()
        assertEquals("tex", fileType.extension, "the extension passes through")
        assertEquals("^\\s*%", fileType.pattern, "the explicit pattern passes through")
    }

    @Test
    fun `falls back to the default patterns for known extensions`() {
        val fileType = SerialFileType("tex").toFileType()
        assertEquals("tex", fileType.extension, "the extension passes through")
        assertEquals(
            DefaultConfiguration.defaultFileTypePatterns["tex"],
            fileType.pattern,
            "the known extension falls back to its default pattern"
        )
    }

    @Test
    fun `rejects unknown extensions without patterns`() {
        assertFailsWith<AraraException>(
            "an unknown extension without a pattern cannot become a file type"
        ) {
            SerialFileType("quack").toFileType()
        }
    }

    @Test
    fun `reports the known extensions as a plain enumeration`() {
        // the message lists the standing options, so the test pins
        // them to the defaults while it asks for the unknown, and
        // leaves the ones it found for whoever comes next
        val standing = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions()
        try {
            val exception = assertFailsWith<AraraException>(
                "an unknown extension without a pattern is rejected"
            ) {
                SerialFileType("quack").toFileType()
            }
            assertTrue(
                exception.message!!.endsWith(".tex, .dtx, .ltx, .drv, .ins"),
                "the known extensions speak as a plain enumeration: " +
                    "${exception.message}"
            )
        } finally {
            LinearExecutor.executionOptions = standing
        }
    }
}
