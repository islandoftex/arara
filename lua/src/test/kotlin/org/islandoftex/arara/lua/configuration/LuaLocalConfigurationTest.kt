// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.configuration

import java.nio.file.Files
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LuaLocalConfigurationTest {
    private fun configFile(content: String): MPPPath {
        val file = Files.createTempFile("araraconfig", ".lua")
        Files.writeString(file, content)
        file.toFile().deleteOnExit()
        return MPPPath(file.toString())
    }

    @Test
    fun shouldLoadAFullConfiguration() {
        val configuration = LuaLocalConfiguration.load(
            configFile(
                """
                return {
                  paths = { "rules" },
                  filetypes = {
                    { extension = "tex" },
                    { extension = "texw", pattern = "^\\s*%\\s+" }
                  },
                  language = "de",
                  loops = 5,
                  verbose = true,
                  logging = true,
                  header = false,
                  dbname = "database",
                  logname = "mylog",
                  preambles = {
                    ["twopdftex"] = "% arara: pdftex\n% arara: pdftex"
                  },
                  defaultPreamble = "twopdftex",
                  prependPreambleIfDirectivesGiven = false
                }
                """.trimIndent()
            )
        )
        assertEquals(
            mapOf("twopdftex" to "% arara: pdftex\n% arara: pdftex"),
            configuration.preambles
        )
        assertEquals("twopdftex", configuration.defaultPreamble)
        assertEquals(false, configuration.prependPreambleIfDirectivesGiven)

        val loggingOptions = configuration.toLoggingOptions()
        assertTrue(loggingOptions.enableLogging)
        // MPPPath keeps the user-provided string as `providedPath` (so
        // `isAbsolute` reflects what the user wrote) while `toString` and
        // friends operate on the absolutized `referencePath`; the configured
        // name therefore survives as the file name
        assertEquals("mylog", loggingOptions.logFile.fileName)

        val uiOptions = configuration.toUserInterfaceOptions()
        assertEquals(MPPLocale("de"), uiOptions.locale)
    }

    @Test
    fun shouldFallBackToDefaultsForOmittedKeys() {
        val configuration = LuaLocalConfiguration.load(
            configFile("""return { language = "en" }""")
        )
        assertEquals(emptyMap(), configuration.preambles)
        assertEquals(null, configuration.defaultPreamble)
        assertEquals(true, configuration.prependPreambleIfDirectivesGiven)
    }

    @Test
    fun shouldRejectNonTableResults() {
        assertFailsWith<AraraException> {
            LuaLocalConfiguration.load(configFile("return 42"))
        }
    }

    @Test
    fun shouldRejectUnknownKeys() {
        assertFailsWith<AraraException> {
            LuaLocalConfiguration.load(
                configFile("""return { unknownKey = true }""")
            )
        }
    }

    @Test
    fun shouldRejectWrongTypes() {
        assertFailsWith<AraraException> {
            LuaLocalConfiguration.load(
                configFile("""return { loops = "many" }""")
            )
        }
        assertFailsWith<AraraException> {
            LuaLocalConfiguration.load(
                configFile("""return { paths = "not-a-list" }""")
            )
        }
        assertFailsWith<AraraException> {
            LuaLocalConfiguration.load(
                configFile("""return { filetypes = { { pattern = "x" } } }""")
            )
        }
    }
}
