// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Files
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies the JVM conveniences: property files load through the
 * lenient reader - an unreadable file warns instead of failing - and
 * property maps convert losslessly to Kotlin maps.
 */
class JVMExtensionsTest {
    @Test
    fun `loads properties from files`() {
        val file = Files.createTempFile("arara-props-", ".properties")
        Files.writeString(file, "key=value\nother=text")
        val properties = Properties()
        properties.load(file)
        assertEquals("value", properties.getProperty("key"))
        assertEquals("text", properties.getProperty("other"))
        val explicit = Properties()
        explicit.load(file, Charsets.ISO_8859_1)
        assertEquals("value", explicit.getProperty("key"))
    }

    @Test
    fun `keeps properties empty when the file cannot be read`() {
        val directory = Files.createTempDirectory("arara-props-missing")
        val properties = Properties()
        properties.load(directory.resolve("not-there.properties"))
        assertTrue(properties.isEmpty, "an unreadable file leaves the properties empty")
    }

    @Test
    fun `converts properties to maps`() {
        val properties = Properties().also {
            it.setProperty("key", "value")
            it.setProperty("other", "text")
        }
        assertEquals(mapOf("key" to "value", "other" to "text"), properties.toMap())
    }
}
