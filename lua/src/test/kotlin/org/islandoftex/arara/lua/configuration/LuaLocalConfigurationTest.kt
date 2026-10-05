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
 * Verifies the Lua configuration parser: the full key surface with
 * its grouped families, the defaults for omitted keys, the
 * rejection of unknown keys - at the root and inside every group -
 * of wrong types and of unknown preamble policies, and the values
 * arara puts in scope for the script to compute its paths from.
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
                  header = false,
                  database = "logs/myrun/arara.yaml",
                  log = {
                    enabled = true,
                    file = "mylog"
                  },
                  console = {
                    mode = "styled",
                    animations = false
                  },
                  preamble = {
                    default = "twopdftex",
                    prepend = "without-directives",
                    definitions = {
                      twopdftex = {
                        "% arara: pdflatex",
                        "% arara: pdflatex"
                      }
                    }
                  }
                }
                """.trimIndent()
            ),
            MPPPath(".")
        )
        assertEquals(
            mapOf("twopdftex" to "% arara: pdflatex\n% arara: pdflatex"),
            configuration.preambles,
            "the preamble definitions join into the directives they name"
        )
        assertEquals("twopdftex", configuration.defaultPreamble, "the default preamble")
        assertEquals(false, configuration.prependPreambleIfDirectivesGiven, "the without-directives policy applies only to files without directives")

        val loggingOptions = configuration.toLoggingOptions()
        assertTrue(loggingOptions.enableLogging, "logging is enabled as configured")
        // MPPPath keeps the user-provided string as `providedPath` (so
        // `isAbsolute` reflects what the user wrote) while `toString` and
        // friends operate on the absolutized `referencePath`; the configured
        // name therefore survives as the file name
        assertEquals("mylog", loggingOptions.logFile.fileName, "the log file name keeps the configured value")

        val executionOptions = configuration.toExecutionOptions(
            Project("test", MPPPath("."), setOf()),
            ExecutionOptions()
        )
        assertEquals(
            MPPPath("logs/myrun/arara.yaml"),
            executionOptions.databaseName,
            "the database name honors the configured path"
        )

        val uiOptions = configuration.toUserInterfaceOptions()
        assertEquals(MPPLocale("de"), uiOptions.locale, "the locale is loaded from the configuration")
        assertEquals("styled", configuration.console, "the console mode is loaded from the configuration")
        assertEquals(false, uiOptions.enableAnimations, "the animations key silences the breath")
    }

    @Test
    fun `falls back to defaults for omitted keys`() {
        val configuration = load("""return { language = "en" }""")
        assertEquals(emptyMap(), configuration.preambles, "an omitted definitions key yields an empty map")
        assertEquals(null, configuration.defaultPreamble, "an omitted default preamble yields null")
        assertEquals(true, configuration.prependPreambleIfDirectivesGiven, "an omitted policy applies everywhere")
    }

    @Test
    fun `the always policy applies everywhere`() {
        val configuration = load(
            """return { preamble = { prepend = "always" } }"""
        )
        assertEquals(true, configuration.prependPreambleIfDirectivesGiven, "the always policy applies to every file")
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
    fun `rejects unknown keys inside the groups`() {
        assertFailsWith<AraraException>(
            "a typo inside a group names itself the same way"
        ) {
            load("""return { log = { enbabled = true } }""")
        }
        assertFailsWith<AraraException>(
            "a typo inside a group names itself the same way"
        ) {
            load("""return { preamble = { defalt = "x" } }""")
        }
    }

    @Test
    fun `rejects a group that is not a table`() {
        assertFailsWith<AraraException>(
            "a group that is not a table is an error"
        ) {
            load("""return { console = "styled" }""")
        }
        assertFailsWith<AraraException>(
            "a group that is not a table is an error"
        ) {
            load("""return { log = true }""")
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
            load("""return { file_types = { { pattern = "x" } } }""")
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
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            load("""return { log = { enabled = "yes" } }""")
        }
        assertFailsWith<AraraException>(
            "wrongly typed configuration keys must be rejected"
        ) {
            load("""return { console = { animations = "no" } }""")
        }
    }

    @Test
    fun `rejects wrong preamble definition shapes`() {
        assertFailsWith<AraraException>(
            "the definitions require a table"
        ) {
            load("""return { preamble = { definitions = 42 } }""")
        }
        assertFailsWith<AraraException>(
            "a definition requires a list of directive lines"
        ) {
            load(
                """
                return {
                  preamble = {
                    definitions = { broken = "% arara: pdflatex" }
                  }
                }
                """.trimIndent()
            )
        }
        assertFailsWith<AraraException>(
            "every directive line requires a string"
        ) {
            load(
                """
                return {
                  preamble = {
                    definitions = { broken = { "% arara: pdflatex", 42 } }
                  }
                }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `rejects unknown preamble policies`() {
        assertFailsWith<AraraException>(
            "the policy vocabulary is two words"
        ) {
            load("""return { preamble = { prepend = "sometimes" } }""")
        }
        assertFailsWith<AraraException>(
            "a wrongly typed policy is not a word at all"
        ) {
            load("""return { preamble = { prepend = 42 } }""")
        }
    }

    @Test
    fun `rejects wrong file type list shapes`() {
        assertFailsWith<AraraException>(
            "the file type list requires tables"
        ) {
            load("""return { file_types = 42 }""")
        }
        assertFailsWith<AraraException>(
            "the file type list requires tables"
        ) {
            load("""return { file_types = { 42 } }""")
        }
        // a wrongly typed pattern never degrades the entry to the
        // extension-only type: the rejection names the expectation
        assertFailsWith<AraraException>(
            "a non string pattern is rejected"
        ) {
            load("""return { file_types = { { extension = "tex", pattern = 42 } } }""")
        }
    }

    @Test
    fun `puts the configured file types at the head of the priority order`() {
        val options = load(
            """
            return {
              file_types = {
                { extension = "ltx" },
                { extension = "dtx" }
              }
            }
            """.trimIndent()
        ).toExecutionOptions(
            Project("test", MPPPath("."), setOf()),
            ExecutionOptions()
        )
        assertEquals(
            listOf("ltx", "dtx", "tex", "dtx", "ltx", "drv", "ins"),
            options.fileTypes.map { it.extension },
            "the configured types lead in their order, then the defaults follow"
        )
    }
}
