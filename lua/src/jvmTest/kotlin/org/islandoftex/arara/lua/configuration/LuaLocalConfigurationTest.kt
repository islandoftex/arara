// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.configuration

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.files.Project

class LuaLocalConfigurationTest {
    private fun withConfiguration(script: String, check: (LocalConfiguration, MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara lua configuration ").toMPPPath()
        try {
            val file = root.resolve("arararc.lua")
            file.writeText(script.trimIndent())
            check(LuaLocalConfiguration.load(file, root), root)
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun typedConfigurationComputesPathsAndConvertsAllSupportedSettings() {
        withConfiguration("""
            return {
                paths = { cwd .. '/rules', user.home .. '/custom', user.name },
                file_types = { { extension = 'tex' }, { extension = 'src', pattern = '^@' } },
                language = 'de', loops = 4, verbose = true, header = false,
                database = 'cache.yaml', log = { format = 'text', file = cwd .. '/build.log' },
                preamble = { default = 'build', prepend = 'without-directives',
                    definitions = { build = { '% arara: first', '% arara: second' } } }
            }
        """) { model, root ->
            val options = model.toExecutionOptions(Project("Test", root, emptySet()))
            assertEquals(root.resolve("rules"), options.rulePaths.first())
            assertTrue(MPPPath(System.getProperty("user.home")).resolve("custom") in options.rulePaths)
            assertTrue(root.resolve(System.getProperty("user.name")) in options.rulePaths)
            assertEquals(listOf("tex", "src"), options.fileTypes.take(2).map { it.extension })
            assertEquals(4, options.maxLoops)
            assertTrue(options.verbose)
            assertFalse(options.parseOnlyHeader)
            assertEquals(root.resolve("cache.yaml"), options.databaseName)
            assertEquals(LogFormat.TEXT, model.toLoggingOptions().format)
            assertEquals(root.resolve("build.log"), model.toLoggingOptions().logFile)
            assertEquals(MPPLocale("de").displayLanguage, model.toUserInterfaceOptions().locale.displayLanguage)
            assertEquals(mapOf("build" to "% arara: first\n% arara: second"), model.preambles)
            assertEquals("build", model.defaultPreamble)
            assertFalse(model.prependPreambleIfDirectivesGiven)
        }
    }

    @Test
    fun omittedSettingsInheritOptionsAndEmptyTablesAreAccepted() {
        withConfiguration("return { paths = {}, file_types = {}, log = {}, preamble = { definitions = {} } }") {
                model, root ->
            val base = ExecutionOptions(maxLoops = 6, verbose = true)
            assertEquals(base, model.toExecutionOptions(Project("Test", root, emptySet()), base))
            val logging = LoggingOptions(LogFormat.TEXT, false, root.resolve("inherited.log"))
            assertEquals(logging, model.toLoggingOptions(logging))
            assertTrue(model.prependPreambleIfDirectivesGiven)
        }
    }

    @Test
    fun luaPathsDoNotExpandYamlPlaceholders() {
        withConfiguration("return { paths = { '@{user.home}/literal' } }") { model, root ->
            val options = model.toExecutionOptions(Project("Test", root, emptySet()))
            assertEquals(root.resolve("@{user.home}/literal"), options.rulePaths.first())
        }
    }

    @Test
    fun malformedShapesTypesAndUnknownKeysReportTheConfigurationFile() {
        val invalid = listOf(
            "return true", "return { verbose = 'true' }", "return { language = 4 }",
            "return { loops = 0 }", "return { loops = 1.5 }", "return { loops = 2147483648 }",
            "return { paths = { [2] = 'gap' } }", "return { paths = { true } }",
            "return { log = false }", "return { log = { format = 1 } }", "return { log = { enabled = true } }",
            "return { log = { append = true } }", "return { database = false }",
            "return { filetypes = {} }", "return { console = {} }", "return { [1] = true }",
            "return { file_types = { { extension = '' } } }",
            "return { file_types = { { extension = 'unknown' } } }",
            "return { file_types = { { extension = 'tex', pattern = false } } }",
            "return { file_types = { { extension = 'tex', typo = true } } }",
            "return { preamble = { prepend = 'sometimes' } }",
            "return { preamble = { definitions = { name = 'not a list' } } }",
            "return { preamble = { definitions = { [1] = {} } } }", "return {"
        )
        for (script in invalid) {
            val error = assertFailsWith<AraraException>(script) { withConfiguration(script) { _, _ -> } }
            assertTrue(error.message.orEmpty().contains("arararc.lua"), script)
            assertTrue(error.exception != null, script)
        }
    }

    @Test
    fun eachScriptGetsFreshGlobals() {
        withConfiguration("leaked = true; user.home = 'changed'; return {}") { _, _ -> }
        withConfiguration("assert(leaked == nil); assert(user.home ~= 'changed'); return {}") { _, _ -> }
    }
}
