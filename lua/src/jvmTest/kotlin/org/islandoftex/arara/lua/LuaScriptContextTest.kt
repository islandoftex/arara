// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectParseException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.lua.configuration.LuaLocalConfiguration

class LuaScriptContextTest {
    @Test
    fun configurationAndProjectScriptsShareValuesButNotMutableGlobals() {
        val root = Files.createTempDirectory("arara script context ").toMPPPath()
        try {
            val parser = LuaInterpreter(root)
            val config = root.resolve("arararc.lua")
            config.writeText("""
                assert(type(user.home) == 'string' and type(user.name) == 'string')
                assert(get_command == nil and getSession == nil)
                leaked = true
                local path = user.home
                user.home = 'changed'
                return { paths = { cwd, path } }
            """.trimIndent())
            val model = LuaLocalConfiguration.load(config, root)
            val script = """
                assert(leaked == nil and user.home ~= 'changed')
                assert(get_command == nil and getSession == nil)
                local project = { name = user.name, working_directory = cwd,
                    files = { [cwd .. '/input.tex'] = {} } }
                user.name = 'changed'; leaked = true
                return project
            """.trimIndent()
            val project = parser.parseProjectsFromLua(script).single()
            assertEquals(System.getProperty("user.name"), project.name)
            assertEquals(root, project.workingDirectory)
            assertEquals(root.resolve("input.tex"), project.files.single().path)
            val options = model.toExecutionOptions(project)
            assertEquals(root, options.rulePaths.first())
            assertEquals(MPPPath(System.getProperty("user.home")), options.rulePaths.elementAt(1))
            assertEquals(project, parser.parseProjectsFromLua(script).single())
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun scriptWorkingDirectoriesAreSuppliedPerLoad() {
        val root = Files.createTempDirectory("arara script directory ").toMPPPath()
        try {
            val child = root.resolve("child").apply { createDirectories() }
            val config = root.resolve("arararc.lua")
            config.writeText("return { paths = { cwd } }")
            for (directory in listOf(root, child)) {
                val project = LuaInterpreter(directory).parseProjectsFromLua(
                    "return { working_directory = cwd, files = { ['input.tex'] = {} } }"
                ).single()
                assertEquals(directory, project.workingDirectory)
                assertEquals(directory, LuaLocalConfiguration.load(config, directory)
                    .toExecutionOptions(project).rulePaths.first())
            }
            assertFailsWith<ProjectParseException> {
                LuaInterpreter(root).parseProjectsFromLua("error('context failure')", chunkName = "context.proj.lua")
            }
        } finally {
            root.removeDirectory()
        }
    }
}
