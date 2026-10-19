// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LuaConfigurationTest {
    private fun fixture(check: (MPPPath, MPPPath) -> Unit) {
        val snapshot = ConfigurationSnapshot.capture()
        val previousHome = System.getProperty("user.home")
        val root = Files.createTempDirectory("arara config discovery ").toMPPPath()
        try {
            val home = root.resolve("home").apply { createDirectories() }
            System.setProperty("user.home", home.toString())
            LinearExecutor.executionOptions = ExecutionOptions()
            Session.clear()
            check(root, home)
        } finally {
            if (previousHome == null) System.clearProperty("user.home")
            else System.setProperty("user.home", previousHome)
            snapshot.restore()
            Session.clear()
            root.removeDirectory()
        }
    }

    @Test
    fun localLuaPrecedesGlobalYamlAndDirectoriesAreIgnored() = fixture { root, home ->
        val project = Project("Test", root, emptySet())
        root.resolve(".araraconfig.lua").createDirectories()
        home.resolve(".araraconfig.yaml").writeText("!config\nloops: 2")
        val local = root.resolve("arararc.lua").apply { writeText("return { loops = 3 }") }
        assertEquals(local, ConfigurationUtils.configFileForProject(project))
        ConfigurationUtils.load(local, project)
        assertEquals(3, LinearExecutor.executionOptions.maxLoops)
    }

    @Test
    fun luaPrecedesYamlWithinEachDirectoryAndBasenamesKeepTheirOrder() = fixture { root, home ->
        val names = listOf(
            ".araraconfig.lua", "araraconfig.lua", ".arararc.lua", "arararc.lua",
            ".araraconfig.yaml", "araraconfig.yaml", ".arararc.yaml", "arararc.yaml"
        )
        val project = Project("Test", root, emptySet())
        for (directory in listOf(root, home)) {
            val files = names.map { directory.resolve(it).apply { writeText("unused") } }
            for (file in files) {
                assertEquals(file, ConfigurationUtils.configFileForProject(project))
                Files.delete(file.toJVMPath())
            }
        }
        assertEquals(null, ConfigurationUtils.configFileForProject(project))
    }

    @Test
    fun localYamlPrecedesGlobalLua() = fixture { root, home ->
        val project = Project("Test", root, emptySet())
        home.resolve(".araraconfig.lua").writeText("return { loops = 2 }")
        val local = root.resolve("arararc.yaml").apply { writeText("!config\nloops: 3") }
        assertEquals(local, ConfigurationUtils.configFileForProject(project))
        ConfigurationUtils.load(local, project)
        assertEquals(3, LinearExecutor.executionOptions.maxLoops)
    }

    @Test
    fun invalidPreferredConfigurationDoesNotFallBack() = fixture { root, _ ->
        val project = Project("Test", root, emptySet())
        val yaml = root.resolve("arararc.yaml").apply { writeText("!config\nloops: 2") }
        val lua = root.resolve("arararc.lua").apply { writeText("return { verbose = 'true' }") }
        assertEquals(lua, ConfigurationUtils.configFileForProject(project))
        assertFailsWith<AraraException> { ConfigurationUtils.load(lua, project) }
        Files.delete(lua.toJVMPath())
        yaml.writeText("invalid YAML configuration")
        assertEquals(yaml, ConfigurationUtils.configFileForProject(project))
        assertFailsWith<AraraException> { ConfigurationUtils.load(yaml, project) }
        val unknown = root.resolve("configuration.txt").apply { writeText("return {}") }
        assertFailsWith<AraraException> { ConfigurationUtils.load(unknown, project) }
    }

    @Test
    fun yamlAndLuaConfigurationsApplyEquivalentOptions() = fixture { root, _ ->
        val project = Project("Test", root, emptySet())
        val logPath = root.resolve("custom.log").toString().replace("\\", "/").replace("'", "''")
        val yaml = root.resolve("arararc.yaml").apply { writeText("""
            !config
            paths: ['.']
            filetypes: [{ extension: src, pattern: '^@ +' }]
            language: de
            loops: 3
            verbose: true
            logging: true
            header: false
            dbname: cache.yaml
            logname: '$logPath'
            preambles: { build: '@ arara: marker' }
            defaultPreamble: build
            prependPreambleIfDirectivesGiven: false
        """.trimIndent()) }
        val lua = root.resolve("arararc.lua").apply { writeText("""
            return { paths = { '.' }, file_types = { { extension = 'src', pattern = '^@ +' } },
                language = 'de', loops = 3, verbose = true, header = false, database = 'cache.yaml',
                log = { enabled = true, file = cwd .. '/custom.log' },
                preamble = { default = 'build', prepend = 'without-directives',
                    definitions = { build = { '@ arara: marker' } } } }
        """.trimIndent()) }
        val base = ConfigurationSnapshot.capture()
        ConfigurationUtils.load(yaml, project)
        val fromYaml = ConfigurationSnapshot.capture()
        base.restore()
        ConfigurationUtils.load(lua, project)
        assertEquals(fromYaml, ConfigurationSnapshot.capture())
    }

    @Test
    fun configurationFormatAndRuleEngineAreIndependentAndHostSettingsAreRestored() {
        for (format in listOf("yaml", "lua")) {
            for (engine in listOf("yaml", "lua")) {
                fixture { root, _ ->
                    root.resolve("arararc.$format").writeText(if (format == "yaml") """
                        !config
                        paths: ['.']
                        filetypes: [{ extension: src, pattern: '^@ +' }]
                        preambles: { build: '@ arara: marker' }
                        defaultPreamble: build
                    """.trimIndent() else """
                        return { paths = { cwd }, file_types = { { extension = 'src', pattern = '^@ +' } },
                            preamble = { default = 'build', definitions = { build = { '@ arara: marker' } } } }
                    """.trimIndent())
                    root.resolve("marker.$engine").writeText(if (engine == "yaml") """
                        !config
                        identifier: marker
                        name: Marker
                        commands:
                          - command: "@{ getSession().put('configured', true); return true; }"
                        arguments: []
                    """.trimIndent() else """
                        return { identifier = 'marker', name = 'Marker', commands = {
                            { command = function(ctx) get_session().put('configured', true); return true end }
                        } }
                    """.trimIndent())
                    root.resolve("input.src").writeText("plain input\n")
                    val before = ConfigurationSnapshot.capture()
                    val status = assertFailsWith<ProgramResult> {
                        CLI().parse(listOf("-d", root.toString(), "--engine", engine, "input.src"))
                    }.statusCode
                    assertEquals(0, status, "$format config / $engine rules")
                    assertEquals(true, Session["configured"])
                    assertEquals(before, ConfigurationSnapshot.capture())
                }
            }
        }
    }
}
