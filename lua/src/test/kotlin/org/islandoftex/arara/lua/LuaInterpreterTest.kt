// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.DefaultConfiguration
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.UNKNOWN_TYPE
import org.islandoftex.arara.core.session.LinearExecutor

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
              working_directory = ".",
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
                  directives = { "% arara: pdflatex" },
                  priority = 1,
                  file_type = {
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
            exception.message!!.contains("requires an integer"),
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

    @Test
    fun `rejects invalid Lua scripts`() {
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua("return {")
        }
    }

    @Test
    fun `lua failures cite the specification file`() {
        val exception = assertFailsWith<ProjectParseException>(
            "a failing script reports its source"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                "error('boom')",
                chunkName = "figures.proj.lua"
            )
        }
        assertTrue(
            exception.message!!.contains("figures.proj.lua"),
            "the error cites the specification file: ${exception.message}"
        )
    }

    @Test
    fun `rejects non table results`() {
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua("return 42")
        }
    }

    @Test
    fun `rejects project list entries that are not tables`() {
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua("return { 1 }")
        }
    }

    @Test
    fun `rejects projects without files`() {
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua("return { files = { } }")
        }
    }

    @Test
    fun `rejects single projects with dependencies`() {
        assertFailsWith<ProjectValidationException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """
                return {
                  files = { ["a.tex"] = { } },
                  dependencies = { "other" }
                }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `rejects wrongly typed project keys`() {
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { name = 42, files = { ["a.tex"] = { } } }"""
            )
        }
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { working_directory = 42, files = { ["a.tex"] = { } } }"""
            )
        }
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua("return { files = 42 }")
        }
    }

    @Test
    fun `rejects wrongly typed dependency entries`() {
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """
                return {
                  {
                    files = { ["a.tex"] = { } },
                    dependencies = { 42 }
                  }
                }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `rejects non string file keys`() {
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                "return { files = { [42] = { } } }"
            )
        }
    }

    @Test
    fun `rejects directory project files`() {
        val directory = Files.createTempDirectory("arara-lua-project")
        Files.createDirectories(directory.resolve("sub"))
        assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(directory.toString())).parseProjectsFromLua(
                """return { files = { ["sub"] = { } } }"""
            )
        }
    }

    @Test
    fun `rejects wrongly typed file specifications`() {
        fun parse(content: String) =
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(content)
        assertFailsWith<ProjectParseException> {
            parse("""return { files = { ["a.tex"] = { file_type = "x" } } }""")
        }
        assertFailsWith<ProjectParseException> {
            parse("""return { files = { ["a.tex"] = { file_type = { pattern = "x" } } } }""")
        }
        assertFailsWith<ProjectParseException> {
            parse("""return { files = { ["a.tex"] = { file_type = { extension = "quack" } } } }""")
        }
        assertFailsWith<ProjectParseException> {
            parse("""return { files = { ["a.tex"] = { priority = "high" } } }""")
        }
        assertFailsWith<ProjectParseException> {
            parse("""return { files = { ["a.tex"] = { directives = "list" } } }""")
        }
        assertFailsWith<ProjectParseException> {
            parse("""return { files = { ["a.tex"] = { directives = { 42 } } } }""")
        }
    }

    @Test
    fun `rejects unknown project keys`() {
        val exception = assertFailsWith<ProjectParseException>(
            "an unknown project key must not be silently ignored"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { name = "p", files = { ["a.tex"] = { } }, preamble = "x" }"""
            )
        }
        assertTrue(
            exception.message!!.contains("preamble"),
            "the error names the unknown key: ${exception.message}"
        )
    }

    @Test
    fun `rejects the outdated spellings of the project format`() {
        // the arara 7 spellings were silently ignored before; the
        // strictness turns them into a named error
        val workingDirectory = assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { workingDirectory = "book", files = { ["a.tex"] = { } } }"""
            )
        }
        assertTrue(
            workingDirectory.message!!.contains("workingDirectory"),
            "the error names the outdated key: ${workingDirectory.message}"
        )
        val fileType = assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { files = { ["a.tex"] = { fileType = { extension = "tex" } } } }"""
            )
        }
        assertTrue(
            fileType.message!!.contains("a.tex.fileType"),
            "the error qualifies the key by its file: ${fileType.message}"
        )
    }

    @Test
    fun `rejects unknown file entry and file type keys`() {
        val entry = assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { files = { ["a.tex"] = { priorityy = 1 } } }"""
            )
        }
        assertTrue(
            entry.message!!.contains("a.tex.priorityy"),
            "the file entry key names itself, qualified: ${entry.message}"
        )
        val type = assertFailsWith<ProjectParseException> {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { files = { ["a.tex"] = { file_type = { extension = "tex", patern = "x" } } } }"""
            )
        }
        assertTrue(
            type.message!!.contains("file_type.patern"),
            "the file type key names itself, qualified: ${type.message}"
        )
    }

    @Test
    fun `rejects named roots posing as project lists`() {
        val exception = assertFailsWith<ProjectParseException>(
            "a root with named keys reports them instead of being misread as a list"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { filess = { ["a.tex"] = { } } }"""
            )
        }
        assertTrue(
            exception.message!!.contains("filess"),
            "the error names the root's key: ${exception.message}"
        )
    }

    @Test
    fun `rejects files listed twice under two spellings`() {
        val exception = assertFailsWith<ProjectParseException>(
            "the same file may not be listed twice, whatever its spelling"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { files = { ["a.tex"] = { }, ["./a.tex"] = { } } }"""
            )
        }
        assertTrue(
            exception.message!!.contains("./a.tex"),
            "the error names the spelling that collided: ${exception.message}"
        )
    }

    @Test
    fun `rejects nameless projects in a list`() {
        assertFailsWith<ProjectParseException>(
            "a list project without a name cannot be identified"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """
                return {
                  { files = { ["a.tex"] = { } } },
                  { name = "other", files = { ["b.tex"] = { } } }
                }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `rejects duplicate project names in a list`() {
        val exception = assertFailsWith<ProjectValidationException>(
            "two projects of one run may not share a name"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """
                return {
                  { name = "same", files = { ["a.tex"] = { } } },
                  { name = "same", files = { ["b.tex"] = { } } }
                }
                """.trimIndent()
            )
        }
        assertTrue(
            exception.message!!.contains("same"),
            "the error names the repeated name: ${exception.message}"
        )
    }

    @Test
    fun `rejects empty project lists`() {
        assertFailsWith<ProjectParseException>(
            "an empty specification is not a specification"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua("return { }")
        }
    }

    @Test
    fun `detects the file type of an entry by its extension`() {
        // the detection reads the file types in effect, so the test
        // pins them to the defaults and leaves what it found
        val standing = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions()
        try {
            val projects = LuaInterpreter(MPPPath("."))
                .parseProjectsFromLua(
                    """return { files = { ["file.tex"] = { } } }"""
                )
            val fileType = projects[0].files.first().fileType
            assertEquals(
                "tex", fileType.extension,
                "the extension of the entry detects its file type"
            )
            assertEquals(
                DefaultConfiguration.defaultFileTypePatterns["tex"],
                fileType.pattern,
                "the detected type carries the default pattern"
            )
        } finally {
            LinearExecutor.executionOptions = standing
        }
    }

    @Test
    fun `resolves a pending entry through the file types of the run`() {
        val standing = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions()
        try {
            // the defaults do not know the extension, so the parse
            // leaves the entry pending
            val projects = LuaInterpreter(MPPPath("."))
                .parseProjectsFromLua(
                    """return { files = { ["file.my"] = { } } }"""
                )
            assertEquals(
                FileType.UNKNOWN_TYPE,
                projects[0].files.first().fileType,
                "an extension the defaults do not know stays pending"
            )
            // the project's own configuration may know it, and the
            // pending entry resolves through the types in effect
            LinearExecutor.executionOptions = ExecutionOptions().copy(
                fileTypes = listOf(
                    org.islandoftex.arara.core.files.FileType(
                        "my", "^\\s*//\\s*"
                    )
                ) + DefaultConfiguration.defaultFileTypes
            )
            val fileType = LuaInterpreter
                .resolvePendingFileTypes(projects[0])
                .files.first().fileType
            assertEquals(
                "my", fileType.extension,
                "the pending entry resolves"
            )
            assertEquals(
                "^\\s*//\\s*", fileType.pattern,
                "the resolved type carries the configured pattern"
            )
        } finally {
            LinearExecutor.executionOptions = standing
        }
    }

    @Test
    fun `rejects extensions no file type of the run carries`() {
        val standing = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions()
        try {
            val projects = LuaInterpreter(MPPPath("."))
                .parseProjectsFromLua(
                    """return { files = { ["file.my"] = { } } }"""
                )
            val exception = assertFailsWith<AraraException>(
                "a file type nobody carries is an error before anything runs"
            ) {
                LuaInterpreter.resolvePendingFileTypes(projects[0])
            }
            assertTrue(
                exception.message!!.contains("file.my"),
                "the error names the file: ${exception.message}"
            )
        } finally {
            LinearExecutor.executionOptions = standing
        }
    }

    @Test
    fun `carried directives survive the pending file type resolution`() {
        val standing = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions()
        try {
            val projects = LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """
                return {
                  files = {
                    ["file.my"] = {
                      directives = { "% arara: pdflatex" }
                    }
                  }
                }
                """.trimIndent()
            )
            LinearExecutor.executionOptions = ExecutionOptions().copy(
                fileTypes = listOf(
                    org.islandoftex.arara.core.files.FileType(
                        "my", "^\\s*//\\s*"
                    )
                ) + DefaultConfiguration.defaultFileTypes
            )
            val resolved = LuaInterpreter.resolvePendingFileTypes(projects[0])
            assertEquals(
                "my",
                resolved.files.first().fileType.extension,
                "the pending entry resolves"
            )
            assertEquals(
                listOf("% arara: pdflatex"),
                (resolved.files.first() as LuaProjectFile).directives,
                "the carried directives reach the resolved copy"
            )
        } finally {
            LinearExecutor.executionOptions = standing
        }
    }

    @Test
    fun `the file's own directive failures report through the wrap`() {
        val file = Files.createTempFile("arara-own-directives", ".tex")
        Files.writeString(file, "% arara: --> stray continuation\n")
        val exception = assertFailsWith<AraraException>(
            "a failure of the file's own directives names the file"
        ) {
            LuaProjectFile(
                MPPPath(file.toString()),
                org.islandoftex.arara.core.files.FileType(
                    "tex", "^\\s*%\\s*"
                )
            ).fetchDirectives(true)
        }
        assertTrue(
            exception.message!!.contains(file.fileName.toString()),
            "the error names the file: ${exception.message}"
        )
    }

    @Test
    fun `honours the redeclared type over the default for detection`() {
        val standing = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions().copy(
            fileTypes = listOf(
                org.islandoftex.arara.core.files.FileType(
                    "dtx", "^\\s*custom\\s*"
                )
            ) + DefaultConfiguration.defaultFileTypes
        )
        try {
            val projects = LuaInterpreter(MPPPath("."))
                .parseProjectsFromLua(
                    """return { files = { ["file.dtx"] = { } } }"""
                )
            assertEquals(
                "^\\s*custom\\s*",
                projects[0].files.first().fileType.pattern,
                "the first spelling of the extension is the one detection finds"
            )
        } finally {
            LinearExecutor.executionOptions = standing
        }
    }

    @Test
    fun `puts arara's values in scope to the script`() {
        val directory = Files.createTempDirectory("arara-lua-scope")
        val workingDirectory = MPPPath(directory.toString()).normalize()
        Files.writeString(
            directory.resolve("scoped.tex"), "Quack\n"
        )
        val projects = LuaInterpreter(MPPPath(directory.toString()))
            .parseProjectsFromLua(
                """
                assert(type(user) == "table")
                assert(type(user.home) == "string")
                assert(type(user.name) == "string")
                assert(type(cwd) == "string")
                return {
                  working_directory = cwd,
                  files = {
                    ["scoped.tex"] = { }
                  }
                }
                """.trimIndent()
            )
        assertEquals(
            workingDirectory, projects[0].workingDirectory,
            "the cwd value names the working directory"
        )
        assertEquals(
            1, projects[0].files.size,
            "the file entry resolves against the scoped working directory"
        )
    }

    @Test
    fun `rejects a blank project name`() {
        val exception = assertFailsWith<ProjectParseException>(
            "a present name cannot be blank"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { name = "", files = { ["a.tex"] = { } } }"""
            )
        }
        assertTrue(
            exception.message!!.contains("cannot be blank"),
            "the error states the constraint: ${exception.message}"
        )
    }

    @Test
    fun `rejects a whitespace-only project name`() {
        assertFailsWith<ProjectParseException>(
            "a whitespace name is blank"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { name = "  ", files = { ["a.tex"] = { } } }"""
            )
        }
    }

    @Test
    fun `resolves a relative working directory against the application's`() {
        val directory = Files.createTempDirectory("arara-wd-resolve")
        Files.createDirectory(directory.resolve("book"))
        val projects = LuaInterpreter(MPPPath(directory.toString()))
            .parseProjectsFromLua(
                """return {
                  working_directory = "book",
                  files = { ["main.tex"] = { } }
                }"""
            )
        assertEquals(
            MPPPath(directory.resolve("book").toString()).normalize().toString(),
            projects[0].workingDirectory.normalize().toString(),
            "a relative working directory anchors at the application's, not the process CWD"
        )
    }

    @Test
    fun `resolves an absolute working directory as itself`() {
        val base = Files.createTempDirectory("arara-wd-base")
        val outside = Files.createTempDirectory("arara-wd-outside")
        val projects = LuaInterpreter(MPPPath(base.toString()))
            .parseProjectsFromLua(
                """
                return {
                  working_directory = "${outside.toString().replace("\\", "\\\\")}",
                  files = { ["main.tex"] = { } }
                }
                """.trimIndent()
            )
        assertEquals(
            MPPPath(outside.toString()).normalize().toString(),
            projects[0].workingDirectory.normalize().toString(),
            "an absolute working directory is itself, never anchored at the application's"
        )
    }

    @Test
    fun `rejects a fractional priority`() {
        assertFailsWith<ProjectParseException>(
            "a fractional priority is a type error, not a silent truncation"
        ) {
            LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                """return { files = { ["a.tex"] = { priority = 2.5 } } }"""
            )
        }
    }

    @Test
    fun `an integral number priority keeps its integer value`() {
        val projects = LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
            """return { files = { ["a.tex"] = { priority = 2.0 } } }"""
        )
        assertEquals(
            2,
            projects[0].files.first().priority,
            "an integral double names the integer it equals"
        )
    }
}
