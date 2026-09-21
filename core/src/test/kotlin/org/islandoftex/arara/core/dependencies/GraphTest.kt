// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the topological ordering of the dependency graph, including
 * the deliberate partial order for cyclic vertices (the linear executor
 * enforces completeness at the project level).
 */
class GraphTest {
    @Test
    fun `operates correctly on an empty graph`() {
        assertEquals(emptyList<Int>(), Graph<Int>().kahn(), "an empty graph has an empty order")
    }
    @Test
    fun `operates correctly on a singleton graph`() {
        assertEquals(listOf(1), Graph<Int>().apply { addVertex(1) }.kahn(), "a singleton graph keeps its single vertex")
    }
    @Test
    fun `omits cyclic vertices from the order`() {
        // Kahn's algorithm deliberately returns a partial order when the
        // graph contains a cycle; the linear executor enforces that the
        // topological order covers every project, so cycles surface as
        // proper errors at the project level
        assertEquals(emptyList<Int>(), Graph<Int>().apply { addEdge(1, 1) }.kahn(), "vertices in a cycle are omitted from the order")
    }
    @Test
    fun `sorts the vertices topologically`() {
        assertEquals(
            Graph<Int>().apply {
                addEdge(6, 4)
                addEdge(6, 5)
                addEdge(4, 3)
                addEdge(4, 2)
                addEdge(3, 1)
            }.kahn(),
            listOf(6, 4, 5, 3, 2, 1),
            "the topological order puts dependencies first"
        )
    }
}
