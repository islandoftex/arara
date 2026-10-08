// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project

/**
 * Verifies the configuration file resolution: the documented order in
 * which YAML configurations take precedence over Lua ones, the
 * fallback to the Lua format when no YAML file exists, and the
 * rejections of unsupported formats and invalid values.
 */
class ConfigurationFilesTest {
    private fun projectIn(directory: java.nio.file.Path): Project =
        Project("test", MPPPath(directory.toString()), setOf())

    @Test
    fun `prefers the YAML configuration over the Lua one`() {
        val directory = Files.createTempDirectory("arara-config-order")
        Files.writeString(directory.resolve("arararc.yaml"), "!config\n")
        Files.writeString(directory.resolve("arararc.lua"), "return { }")
        val resolved = ConfigurationFiles.configFileForProject(projectIn(directory))
        assertEquals(
            "arararc.yaml",
            resolved?.fileName,
            "the YAML configuration wins when both formats exist"
        )
    }

    @Test
    fun `falls back to the Lua configuration`() {
        val directory = Files.createTempDirectory("arara-config-lua")
        Files.writeString(directory.resolve(".araraconfig.lua"), "return { }")
        val resolved = ConfigurationFiles.configFileForProject(projectIn(directory))
        assertEquals(
            ".araraconfig.lua",
            resolved?.fileName,
            "the Lua configuration is found in its dotted name"
        )
    }

    @Test
    fun `rejects configuration files of unsupported formats`() {
        val directory = Files.createTempDirectory("arara-config-unsupported")
        val bogus = directory.resolve("arararc.quack")
        Files.writeString(bogus, "not a supported configuration")
        assertFailsWith<AraraException>(
            "an unsupported configuration format is rejected"
        ) {
            ConfigurationFiles.load(
                MPPPath(bogus.toString()), projectIn(directory)
            )
        }
    }

    @Test
    fun `a malformed pattern in a configuration is a load error`() {
        val directory = Files.createTempDirectory("arara-config-pattern")
        val config = directory.resolve("arararc.yaml")
        Files.writeString(
            config,
            "!config\nfiletypes:\n  - extension: my\n    pattern: '[a'\n"
        )
        assertFailsWith<AraraException>(
            "a malformed pattern is a load error"
        ) {
            ConfigurationFiles.load(
                MPPPath(config.toString()), projectIn(directory)
            )
        }
    }

    @Test
    fun `rejects invalid loop counts in the YAML configuration`() {
        val directory = Files.createTempDirectory("arara-config-loops")
        val config = directory.resolve("arararc.yaml")
        Files.writeString(config, "!config\nloops: 0\n")
        assertFailsWith<AraraException>(
            "a non positive loop count is rejected"
        ) {
            ConfigurationFiles.load(
                MPPPath(config.toString()), projectIn(directory)
            )
        }
    }
}
