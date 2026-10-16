// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProjectWorkspaceTest {

    @Test
    fun existingProjectsKeepTheirSourceAndExecutionIdentity() {
        val source = MPPPath(".").normalize()
        val project = object : Project {
            override val name = "legacy"
            override val workingDirectory = source
            override val files = emptySet<ProjectFile>()
            override val dependencies = emptySet<String>()
        }
        val file = object : ProjectFile {
            override val path = source.resolve("book.tex")
            override val fileType = object : FileType {
                override val extension = "tex"
                override val pattern = "^% +"
            }
            override val priority = 0
            override fun fetchDirectives(parseOnlyHeader: Boolean) = emptyList<org.islandoftex.arara.api.rules.Directive>()
        }
        assertNull(project.buildDirectory)
        assertEquals(source, project.executionDirectory)
        assertEquals(file.path, file.sourcePath)
    }

    @Test
    fun buildDirectoriesResolveAgainstTheSourceDirectory() {
        val source = MPPPath("source").normalize()
        for (directory in listOf(MPPPath("build/../workspace"), source.resolve("workspace"))) {
            val options = BuildDirectory(directory)
            val project = object : Project {
                override val name = "isolated"
                override val workingDirectory = source
                override val files = emptySet<ProjectFile>()
                override val dependencies = emptySet<String>()
                override val buildDirectory = options
            }
            assertEquals(source.resolve("workspace"), project.executionDirectory)
            assertEquals(source, project.workingDirectory)
            assertNull(options.inputs)
            assertEquals(emptyList(), options.mappings)
            assertEquals(BuildSymlinks.PRESERVE, options.symlinks)
        }
    }

    @Test
    fun selectionsDistinguishLiteralPathsFromGlobPatterns() {
        val path = MPPPath("notes[1].txt")
        val options = BuildDirectory(MPPPath("build"), inputs = listOf(
            BuildSelection.Path(path), BuildSelection.Glob("*.bib")))
        val selections = requireNotNull(options.inputs)
        assertEquals(path, (selections[0] as BuildSelection.Path).path)
        assertEquals("*.bib", (selections[1] as BuildSelection.Glob).pattern)
    }

}
