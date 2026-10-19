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

        assertEquals(1, projects.size)
        val project = projects[0]

        assertEquals("My awesome book", project.name)
        assertEquals(2, project.files.size)
        assertEquals(0, project.dependencies.size)
    }

    @Test
    fun shouldParseProjectFile() {
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

        assertEquals(1, projects.size)
        assertEquals(1, projects[0].files.size)
        val projectFile = projects[0].files.first()

        assertEquals(MPPPath("file.tex").normalize(), projectFile.path)
        assertEquals(1, projectFile.priority)
        assertEquals(1, (projectFile as? LuaProjectFile)?.directives?.size)
        assertEquals("tex", projectFile.fileType.extension)
        assertEquals(".*", projectFile.fileType.pattern)
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

    @Test
    fun shouldFailOnUnsatisfiedDependency() {
        val script = """
            return {
              name = "My awesome book",
              files = {
                ["a.tex"] = { }
              },
              dependencies = { "My awesome book v2" }
            }
        """.trimIndent()
        assertFailsWith<AraraException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
                .also { println(it) }
        }
    }

    @Test
    fun shouldNotFailOnSatisfiedDependency() {
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
        assertEquals(2, LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script).size)
    }

    @Test
    fun rejectsMalformedFieldsWithoutSilentlyDroppingEntries() {
        val invalid = listOf(
            "name = 42, files = { ['a.tex'] = {} }",
            "name = ' ', files = { ['a.tex'] = {} }",
            "files = { ['a.tex'] = false }",
            "files = { [42] = {} }",
            "files = { ['a.tex'] = { priority = 1.5 } }",
            "files = { ['a.tex'] = { priority = 2147483648 } }",
            "files = { ['a.tex'] = { directives = { 1 } } }",
            "files = { ['a.tex'] = { directives = { [2] = 'x' } } }",
            "files = { ['a.tex'] = { fileType = false } }",
            "files = { ['a.tex'] = { fileType = { extension = 4 } } }",
            "files = { ['a.tex'] = {} }, dependencies = { false }",
            "files = { ['a.tex'] = {} }, typo = true"
        )
        invalid.forEach { fields ->
            assertFailsWith<ProjectParseException>(fields) {
                LuaInterpreter(MPPPath(".")).parseProjectsFromLua("return { $fields }")
            }
        }
    }

    @Test
    fun rejectsMalformedProjectListsAndPreservesFieldContext() {
        for (script in listOf("return {}", "return { false }", "return { [2] = {} }",
            "return { { files = { ['a.tex'] = {} } } }")) {
            assertFailsWith<ProjectParseException> {
                LuaInterpreter(MPPPath(".")).parseProjectsFromLua(script)
            }
        }
        val error = assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                "return { files = { ['a.tex'] = { priority = false } } }"
            )
        }
        assertEquals("project.files[a.tex].priority must be an integer.", error.message)
    }


    @Test
    fun resolvesWorkingDirectoryAgainstTheInvocationDirectory() {
        val base = MPPPath("project-root").normalize()
        val project = LuaInterpreter(base).parseProjectsFromLua(
            "return { workingDirectory = 'chapter/../sources', files = { ['a.tex'] = {} } }"
        ).single()
        assertEquals(base.resolve("sources").normalize(), project.workingDirectory)
        assertEquals(base.resolve("sources/a.tex").normalize(), project.files.single().path)
    }

    @Test
    fun resolvesImplicitCompoundTypesWithoutReplacingExplicitTypesOrDirectives() {
        val project = LuaInterpreter(MPPPath(".")).parseProjectsFromLua("""
            return { files = {
              ['a.custom.tex'] = { priority = 3, directives = { 'marker' } },
              ['b.tex'] = { fileType = { extension = 'tex', pattern = 'explicit' } }
            } }
        """.trimIndent()).single()
        val type = org.islandoftex.arara.core.files.FileType("custom.tex", "configured")
        val options = org.islandoftex.arara.core.configuration.ExecutionOptions(fileTypes = listOf(type))
        val resolved = LuaInterpreter.resolvePendingFileTypes(project, options)
        val implicit = resolved.files.single { it.path.fileName == "a.custom.tex" } as LuaProjectFile
        assertEquals(type, implicit.fileType)
        assertEquals(3, implicit.priority)
        assertEquals(listOf("marker"), implicit.directives)
        assertEquals("explicit", resolved.files.single { it.path.fileName == "b.tex" }.fileType.pattern)
        assertFailsWith<ProjectParseException> {
            LuaInterpreter.resolvePendingFileTypes(
                project, org.islandoftex.arara.core.configuration.ExecutionOptions(fileTypes = emptyList())
            )
        }
    }

}
