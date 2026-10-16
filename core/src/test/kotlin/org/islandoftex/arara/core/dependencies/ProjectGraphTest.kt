// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project

class ProjectGraphTest {
    @Test
    fun shouldBeAbleToAddAll() {
        assertEquals(
            ProjectGraph().apply {
                addAll(
                    listOf(
                        Project("Test", MPPPath(""), setOf(), setOf("Test 2")),
                        Project("Test 2", MPPPath(""), setOf())
                    )
                )
            }.kahn().map { it.name },
            listOf("Test 2", "Test")
        )
    }

    @Test
    fun rejectsCyclesMissingDependenciesAndDuplicateNames() {
        val a = Project("A", MPPPath("."), emptySet(), setOf("B"))
        val b = Project("B", MPPPath("."), emptySet(), setOf("A"))
        for (projects in listOf(listOf(a, b), listOf(a), listOf(b, b),
            listOf(Project("Self", MPPPath("."), emptySet(), setOf("Self"))))) {
            assertFailsWith<AraraException> { ProjectGraph().apply { addAll(projects) }.kahn() }
        }
    }

    @Test
    fun cycleDiagnosticsNameOnlyCycleMembers() {
        val projects = listOf(
            Project("Cycle A", MPPPath("."), emptySet(), setOf("Cycle B", "Prerequisite")),
            Project("Cycle B", MPPPath("."), emptySet(), setOf("Cycle A")),
            Project("Dependent", MPPPath("."), emptySet(), setOf("Cycle A")),
            Project("Prerequisite", MPPPath("."), emptySet()),
            Project("Independent", MPPPath("."), emptySet())
        )
        val failure = assertFailsWith<AraraException> { ProjectValidation.executionOrder(projects) }
        assertEquals("Project dependencies contain a cycle: Cycle A, Cycle B.", failure.message)
    }

    @Test
    fun validationDiagnosticsIdentifyDuplicateNamesAndUnknownDependencies() {
        val project = Project("Owner", MPPPath("."), emptySet(), setOf("Missing"))
        val duplicate = assertFailsWith<AraraException> {
            ProjectValidation.executionOrder(listOf(project, project))
        }
        assertEquals("Project names must be unique: Owner.", duplicate.message)
        val missing = assertFailsWith<AraraException> { ProjectValidation.executionOrder(listOf(project)) }
        assertEquals("Project 'Owner' has unknown dependency 'Missing'.", missing.message)
    }

    @Test
    fun independentProjectsHaveStableOrderRegardlessOfInputOrder() {
        val projects = listOf("C", "B", "A").map { Project(it, MPPPath("."), emptySet()) }
        assertEquals(listOf("A", "B", "C"), ProjectGraph().apply { addAll(projects) }.kahn().map { it.name })
        assertEquals(listOf("A", "B", "C"), ProjectGraph().apply {
            addAll(projects.reversed())
        }.kahn().map { it.name })
    }

    @Test
    fun repeatingAnEdgeDoesNotLeaveAFakeCycle() {
        val graph = Graph<String>()
        graph.addEdge("A", "B")
        graph.addEdge("A", "B")
        assertEquals(listOf("A", "B"), graph.kahn())
    }

    @Test
    fun invocationValidationRejectsSharedPathsRegardlessOfAttributes() {
        val type = org.islandoftex.arara.core.files.FileType("tex", "^%")
        val first = org.islandoftex.arara.core.files.ProjectFile(MPPPath("a.tex"), type)
        val second = org.islandoftex.arara.core.files.ProjectFile(MPPPath("./a.tex"), type, 1)
        assertFailsWith<AraraException> {
            ProjectValidation.executionOrder(listOf(
                Project("A", MPPPath("."), setOf(first)),
                Project("B", MPPPath("."), setOf(second))
            ))
        }
        assertFailsWith<AraraException> {
            ProjectValidation.executionOrder(listOf(Project("A", MPPPath("."), setOf(first, second))))
        }
    }
}
