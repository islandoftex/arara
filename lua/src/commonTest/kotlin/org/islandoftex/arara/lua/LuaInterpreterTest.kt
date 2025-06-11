// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LuaInterpreterTest {
    @Test
    fun shouldParseSingleProject() {
        val script =
            """
            return {
              name = "My awesome book",
              workingDirectory = ".",
              files = {
                ["a.mp"] = { },
                ["file.tex"] = { }
              }
            }
            """.trimIndent()
        val projects = LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)

        assertEquals(1, projects.size)
        val project = projects[0]

        assertEquals("My awesome book", project.name)
        assertEquals(2, project.files.size)
        assertEquals(0, project.dependencies.size)
    }

    @Test
    fun shouldParseProjectFile() {
        val script =
            """
            return {
              files = {
                ["file.tex"] = {
                  directives = { "% arara: pdftex" },
                  priority = 1,
                  fileType = {
                    extension = "tex",
                    pattern = ".*"
                  }
                }
              }
            }
            """.trimIndent()
        val projects = LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)

        assertEquals(1, projects.size)
        assertEquals(1, projects[0].files.size)
        val projectFile = projects[0].files.first()

        assertEquals(MPPPath("file.tex"), projectFile.path)
        assertEquals(1, projectFile.priority)
        assertEquals(1, (projectFile as? LuaProjectFile)?.directives?.size)
        assertEquals("tex", projectFile.fileType.extension)
        assertEquals(".*", projectFile.fileType.pattern)
    }

    @Test
    fun shouldParseMultipleProjects() {
        val script =
            """
            return {
              {
                name = "My awesome book",
                files = {
                  ["a.tex"] = { }
                }
              },
              {
                name = "My awesome book v2",
                files = {
                  ["b.tex"] = { }
                }
              }
            }
            """.trimIndent()
        val projects = LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)

        assertEquals(2, projects.size)

        assertEquals("My awesome book", projects[0].name)
        assertEquals("My awesome book v2", projects[1].name)
    }

    @Test
    fun shouldFailOnUnsatisfiedDependency() {
        val script =
            """
            return {
              name = "My awesome book",
              files = {
                ["a.tex"] = { }
              },
              dependencies = { "My awesome book v2" }
            }
            """.trimIndent()
        assertFailsWith<AraraException> {
            LuaInterpreter(MPPPath("."))
                .parseProjectsFromLua(script)
                .also { println(it) }
        }
    }

    @Test
    fun shouldNotFailOnSatisfiedDependency() {
        val script =
            """
            return {
              {
                name = "My awesome book",
                files = {
                  ["a.tex"] = { }
                },
                dependencies = { "My awesome book v2" }
              },
              {
                name = "My awesome book v2",
                files = {
                  ["b.tex"] = { }
                }
              }
            }
            """.trimIndent()
        assertEquals(
            2,
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script).size,
        )
    }
}
