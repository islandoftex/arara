// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.configuration

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.Environment

/**
 * Verifies the Lua configuration parser: the full key set, the
 * defaults for omitted keys, the rejection of unknown keys and wrong
 * types, and the values arara puts in scope for the script to compute
 * its paths from.
 */
class LuaLocalConfigurationTest {
    private fun configFile(content: String): MPPPath {
        val file = Files.createTempFile("araraconfig", ".lua")
        Files.writeString(file, content)
        file.toFile().deleteOnExit()
        return MPPPath(file.toString())
    }

    private fun load(content: String): LocalConfiguration =
        LuaLocalConfiguration.load(configFile(content), MPPPath("."))

    @Test
    fun `loads a full configuration`() {
        val configuration = LuaLocalConfiguration.load(
            configFile(
                """
                return {
                  paths = { "rules" },
                  file_types = {
                    { extension = "tex" },
                    { extension = "texw", pattern = "^\\s*%\\s+" }
                  },
                  language = "de",
                  loops = 5,
                  verbose = true,
                  logging = true,
                  header = false,
                  db_name = "database",
                  log_name = "mylog",
                  preambles = {
                    ["twopdftex"] = "% arara: pdftex\n% arara: pdftex"
                  },
                  default_preamble = "twopdftex",
                  prepend_preamble_if_directives_given = false
                }
                """.trimIndent()
            ),
            MPPPath(".")
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
        val configuration = load("""return { language = "en" }""")
        assertEquals(emptyMap(), configuration.preambles, "an omitted preambles key yields an empty map")
        assertEquals(null, configuration.defaultPreamble, "an omitted default preamble yields null")
        assertEquals(true, configuration.prependPreambleIfDirectivesGiven, "preamble prepending defaults to true")
    }

    @Test
    fun `computes paths from the values in scope`() {
        val directory = Files.createTempDirectory("arara-lua-config")
        val workingDirectory = MPPPath(directory.toString())
        val userHome = Environment.getSystemPropertyOrNull("user.home") ?: ""
        val userName = Environment.getSystemPropertyOrNull("user.name") ?: ""
        val configuration = LuaLocalConfiguration.load(
            configFile(
                """
                return {
                  paths = {
                    user.home .. "/marker-rules",
                    user.name .. "-rules",
                    cwd .. "/nested-rules",
                    "plain-relative-rules"
                  }
                }
                """.trimIndent()
            ),
            workingDirectory
        )
        val paths = configuration.toExecutionOptions(
            Project("test", workingDirectory, setOf()),
            ExecutionOptions()
        ).rulePaths
        assertTrue(
            MPPPath("$userHome/marker-rules").normalize() in paths,
            "the user home value computes: $paths"
        )
        assertTrue(
            (workingDirectory / "$userName-rules").normalize() in paths,
            "the user name value computes into a relative path: $paths"
        )
        assertTrue(
            (workingDirectory / "nested-rules").normalize() in paths,
            "the working directory value computes: $paths"
        )
        assertTrue(
            (workingDirectory / "plain-relative-rules").normalize() in paths,
            "relative paths still resolve against the project: $paths"
        )
    }

    @Test
    fun `rejects unknown values in the script`() {
        assertFailsWith<AraraException>(
            "a misspelled value cannot concatenate and must be rejected"
        ) {
            load("""return { paths = { user.hoe .. "/rules" } }""")
        }
    }

    @Test
    fun `rejects non table results`() {
        assertFailsWith<AraraException>(
            "a non-table result must be rejected"
        ) {
            load("return 42")
        }
        // the exception type itself carries the contract
    }

    @Test
    fun `rejects unknown keys`() {
        assertFailsWith<AraraException>(
            "unknown configuration keys must be rejected"
        ) {
            load("""return { unknownKey = true }""")
        }
    }

    @Test
    fun `rejects wrongly typed values`() {
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            load("""return { loops = "many" }""")
        }
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            load("""return { paths = "not-a-list" }""")
        }
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            load("""return { filetypes = { { pattern = "x" } } }""")
        }
    }

    @Test
    fun `rejects wrong scalar value types`() {
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            load("""return { language = 42 }""")
        }
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            load("""return { verbose = "yes" }""")
        }
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            load("""return { paths = { 42 } }""")
        }
    }

    @Test
    fun `rejects wrong preamble map shapes`() {
        assertFailsWith<AraraException>(
            "the preamble map requires string pairs"
        ) {
            load("""return { preambles = 42 }""")
        }
        assertFailsWith<AraraException>(
            "the preamble map requires string pairs"
        ) {
            load("""return { preambles = { key = 42 } }""")
        }
        assertFailsWith<AraraException>(
            "the preamble map requires string pairs"
        ) {
            load("""return { preambles = { "orphan" } }""")
        }
    }

    @Test
    fun `rejects wrong file type list shapes`() {
        assertFailsWith<AraraException>(
            "the file type list requires tables"
        ) {
            load("""return { filetypes = 42 }""")
        }
        assertFailsWith<AraraException>(
            "the file type list requires tables"
        ) {
            load("""return { filetypes = { 42 } }""")
        }
    }
}
