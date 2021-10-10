// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.mamoe.yamlkt.Yaml
import org.islandoftex.arara.api.files.MPPPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectTest {
    private val testYaml = Yaml {
        serializersModule = SerializersModule {
            contextual(MPPPathSerializer)
        }
    }

    @Test
    fun shouldDeserializeSingleProject() {
        val text = """
            name: Test project
            workingDirectory: "."
            files:
            - path: quack.tex
              fileType:
                  extension: tex
                  pattern: quack
        """.trimIndent()
        val project = testYaml.decodeFromString<Project>(text)
        assertEquals("Test project", project.name)
        assertTrue(project.dependencies.isEmpty())
        assertTrue(project.workingDirectory.isDirectory)
        assertEquals(setOf(ProjectFile(MPPPath("quack.tex"), FileType("tex", "quack"))), project.files)
    }

    @Test
    fun shouldDeserializeListOfProjects() {
        val text = """
            - name: Test project
              workingDirectory: "."
              files:
              - path: quack.tex
                fileType:
                    extension: tex
                    pattern: quack
        """.trimIndent()
        val projects = testYaml.decodeFromString<List<Project>>(text)
        assertTrue(projects.size == 1)
        val project = projects[0]
        assertEquals("Test project", project.name)
        assertTrue(project.dependencies.isEmpty())
        assertTrue(project.workingDirectory.isDirectory)
        assertEquals(setOf(ProjectFile(MPPPath("quack.tex"), FileType("tex", "quack"))), project.files)
    }
}
