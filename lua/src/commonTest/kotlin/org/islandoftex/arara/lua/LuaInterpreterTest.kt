// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.files.MPPPath
import kotlin.test.Test
import kotlin.test.assertEquals

class LuaInterpreterTest {
    @Test
    fun shouldParseSingleProject() {
        val script = """
            return {
              name = "My awesome book",
              workingDirectory = ".",
              files = {
                ["a.mp"] = { directives = { "metapost" } },
                ["file.tex"] = {
                  dependencies = { "a.mp" }
                }
              },
              dependencies = { "project b" }
            }
        """.trimIndent()
        val projects = LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)

        assertEquals(1, projects.size)
        val project = projects[0]

        assertEquals("My awesome book", project.name)
        assertEquals(2, project.files.size)
        assertEquals(1, project.dependencies.size)
    }

    @Test
    fun shouldParseMultipleProjects() {
        val script = """
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
}
