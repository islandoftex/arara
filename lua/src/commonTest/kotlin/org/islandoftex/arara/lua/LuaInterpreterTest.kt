// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

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
        val projects = LuaInterpreter.parseProjectsFromLua(script)

        assertEquals(1, projects.size)
        val project = projects[0]

        assertEquals("My awesome book", project.name)
        assertEquals(2, project.files.size)
        assertEquals(1, project.dependencies.size)
    }
}
