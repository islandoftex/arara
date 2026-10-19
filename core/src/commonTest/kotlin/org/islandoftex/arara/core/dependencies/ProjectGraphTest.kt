// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException

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

}
