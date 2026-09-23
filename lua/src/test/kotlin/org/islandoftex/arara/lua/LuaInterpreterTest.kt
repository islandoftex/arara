// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Verifies the Lua project specification parser: single projects, file
 * entries with their explicit directives, project lists, the
 * dependency validation and the rejection of wrongly typed entries.
 */
class LuaInterpreterTest {
    @Test
    fun `parses a single project`() {
        val script = """
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

        assertEquals(1, projects.size, "a single table denotes a single project")
        val project = projects[0]

        assertEquals("My awesome book", project.name, "the project name")
        assertEquals(2, project.files.size, "the project holds both files")
        assertEquals(0, project.dependencies.size, "the project has no dependencies")
    }

    @Test
    fun `parses a project file entry`() {
        val script = """
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

        assertEquals(1, projects.size, "the file entry denotes a single project")
        assertEquals(1, projects[0].files.size, "the project holds the file")
        val projectFile = projects[0].files.first()

        assertEquals(MPPPath("file.tex"), projectFile.path, "the resolved path of the file entry")
        assertEquals(1, projectFile.priority, "the explicit file priority")
        assertEquals(1, (projectFile as? LuaProjectFile)?.directives?.size, "the explicit directive is present")
        assertEquals("tex", projectFile.fileType.extension, "the explicit file type extension")
        assertEquals(".*", projectFile.fileType.pattern, "the explicit file type pattern")
    }

    @Test
    fun `parses multiple projects`() {
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

        assertEquals(2, projects.size, "the list denotes two projects")

        assertEquals("My awesome book", projects[0].name, "the first project name")
        assertEquals("My awesome book v2", projects[1].name, "the second project name")
    }

    @Test
    fun `fails on an unsatisfied dependency`() {
        val script = """
            return {
              name = "My awesome book",
              files = {
                ["a.tex"] = { }
              },
              dependencies = { "My awesome book v2" }
            }
        """.trimIndent()
        assertFailsWith<AraraException>(
            "an unsatisfied dependency must be rejected"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
        }
    }

    @Test
    fun `accepts a satisfied dependency`() {
        val script = """
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
        assertEquals(2, LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script).size, "both projects in the list are parsed")
    }

    @Test
    fun `rejects string file specifications`() {
        val script = """
            return {
              files = {
                ["file.tex"] = "% arara: pdftex"
              }
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a string file specification must not be silently dropped"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
        }
        assertTrue(
            exception.message!!.contains("has to be a table"),
            "the error names the expected shape: ${exception.message}"
        )
    }

    @Test
    fun `rejects non string directive entries`() {
        val script = """
            return {
              files = {
                ["file.tex"] = {
                  directives = { 42 }
                }
              }
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a number directive entry must not be silently dropped"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
        }
        assertTrue(
            exception.message!!.contains("has to be a string"),
            "the error names the expected entry type: ${exception.message}"
        )
    }

    @Test
    fun `rejects string priorities`() {
        val script = """
            return {
              files = {
                ["file.tex"] = {
                  priority = "5"
                }
              }
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a string priority must not be silently defaulted"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
        }
        assertTrue(
            exception.message!!.contains("requires a number"),
            "the error names the expected type: ${exception.message}"
        )
    }

    @Test
    fun `rejects number dependency entries`() {
        val script = """
            return {
              {
                name = "Main project",
                files = {
                  ["a.tex"] = { }
                },
                dependencies = { 42 }
              },
              {
                name = "Other project",
                files = {
                  ["b.tex"] = { }
                }
              }
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a number dependency must not be silently dropped"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
        }
        assertTrue(
            exception.message!!.contains("has to be a string"),
            "the error names the expected entry type: ${exception.message}"
        )
    }

    @Test
    fun `rejects number file keys`() {
        val script = """
            return {
              files = {
                [42] = { }
              }
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a number file key must not be silently stringified"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
        }
        assertTrue(
            exception.message!!.contains("keyed by"),
            "the error names the expected key shape: ${exception.message}"
        )
    }

    @Test
    fun `rejects non table project list entries`() {
        val script = """
            return {
              "not a project"
            }
        """.trimIndent()
        val exception = assertFailsWith<AraraException>(
            "a string root entry must not be silently filtered"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
        }
        assertTrue(
            exception.message!!.contains("has to be a table"),
            "the error names the expected shape: ${exception.message}"
        )
    }

    @Test
    fun `defaults absent entries to their documented values`() {
        val script = """
            return {
              files = {
                ["file.tex"] = { }
              }
            }
        """.trimIndent()
        val projects = LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
        val projectFile = projects[0].files.first()
        assertEquals("Untitled", projects[0].name, "an absent project name defaults")
        assertEquals(
            org.islandoftex.arara.core.files.ProjectFile.DEFAULT_PRIORITY,
            projectFile.priority,
            "an absent priority defaults"
        )
    }
}
