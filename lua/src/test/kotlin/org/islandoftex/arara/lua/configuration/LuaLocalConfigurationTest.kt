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

/**
 * Verifies the Lua configuration parser: the full key set, the defaults
 * for omitted keys and the rejection of unknown keys and wrong types.
 */
class LuaLocalConfigurationTest {
    private fun configFile(content: String): MPPPath {
        val file = Files.createTempFile("araraconfig", ".lua")
        Files.writeString(file, content)
        file.toFile().deleteOnExit()
        return MPPPath(file.toString())
    }

    @Test
    fun `loads a full configuration`() {
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
            configuration.preambles,
            "the preambles are loaded from the configuration"
        )
        assertEquals("twopdftex", configuration.defaultPreamble, "the default preamble")
        assertEquals(false, configuration.prependPreambleIfDirectivesGiven, "preamble prepending follows the configuration")

        val loggingOptions = configuration.toLoggingOptions()
        assertTrue(loggingOptions.enableLogging, "logging is enabled as configured")
        // MPPPath keeps the user-provided string as `providedPath` (so
        // `isAbsolute` reflects what the user wrote) while `toString` and
        // friends operate on the absolutized `referencePath`; the configured
        // name therefore survives as the file name
        assertEquals("mylog", loggingOptions.logFile.fileName, "the log file name keeps the configured value")

        val uiOptions = configuration.toUserInterfaceOptions()
        assertEquals(MPPLocale("de"), uiOptions.locale, "the locale is loaded from the configuration")
    }

    @Test
    fun `falls back to defaults for omitted keys`() {
        val configuration = LuaLocalConfiguration.load(
            configFile("""return { language = "en" }""")
        )
        assertEquals(emptyMap(), configuration.preambles, "an omitted preambles key yields an empty map")
        assertEquals(null, configuration.defaultPreamble, "an omitted default preamble yields null")
        assertEquals(true, configuration.prependPreambleIfDirectivesGiven, "preamble prepending defaults to true")
    }

    @Test
    fun `rejects non table results`() {
        assertFailsWith<AraraException>(
            "a non-table result must be rejected"
        ) {
            LuaLocalConfiguration.load(configFile("return 42"))
        }
        // the exception type itself carries the contract
    }

    @Test
    fun `rejects unknown keys`() {
        assertFailsWith<AraraException>(
            "unknown configuration keys must be rejected"
        ) {
            LuaLocalConfiguration.load(
                configFile("""return { unknownKey = true }""")
            )
        }
    }

    @Test
    fun `rejects wrongly typed values`() {
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            LuaLocalConfiguration.load(
                configFile("""return { loops = "many" }""")
            )
        }
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            LuaLocalConfiguration.load(
                configFile("""return { paths = "not-a-list" }""")
            )
        }
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            LuaLocalConfiguration.load(
                configFile("""return { filetypes = { { pattern = "x" } } }""")
            )
        }
    }
}
