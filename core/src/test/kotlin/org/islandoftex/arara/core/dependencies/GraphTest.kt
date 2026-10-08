// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the two answers of the dependency graph: the topological
 * order Kahn's algorithm yields - including its deliberate partial
 * order for cyclic vertices, which the linear executor completes
 * at the project level - and the cycle membership the walk names,
 * down to the members alone, without their dependents or the
 * bridges between two cycles.
 */
class GraphTest {
    @Test
    fun `an empty graph orders to nothing`() {
        assertEquals(emptyList<Int>(), Graph<Int>().kahn(), "an empty graph has an empty order")
    }
    @Test
    fun `a lone vertex orders to itself`() {
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
    @Test
    fun `names a self-dependent vertex as its own cycle`() {
        assertEquals(
            listOf(1),
            Graph<Int>().apply { addEdge(1, 1) }.cyclicVertices(),
            "a self-dependent vertex is the cycle"
        )
    }
    @Test
    fun `names the cycle members alone, not their dependents`() {
        // the walk omits the cycle together with every vertex
        // depending on it, so each omitted vertex confirms its own
        // membership - the dependent 3 and the deeper 4 are not
        // accused of the cycle they hang from
        assertEquals(
            listOf(1, 2),
            Graph<Int>().apply {
                addEdge(1, 2)
                addEdge(2, 1)
                addEdge(1, 3)
                addEdge(3, 4)
            }.cyclicVertices(),
            "only the vertices on the cycle are named"
        )
    }
    @Test
    fun `names only the members of two cycles sharing a dependent`() {
        assertEquals(
            listOf(1, 2, 3, 4),
            Graph<Int>().apply {
                addEdge(1, 2)
                addEdge(2, 1)
                addEdge(3, 4)
                addEdge(4, 3)
                addEdge(1, 5)
                addEdge(3, 5)
            }.cyclicVertices(),
            "the shared dependent is not a member of either cycle"
        )
    }
    @Test
    fun `does not name the bridge between two cycles`() {
        // 5 depends on the first cycle, and the second cycle's 3
        // depends on 5 - the bridge lies on a path between the
        // cycles without lying on any cycle itself
        assertEquals(
            listOf(1, 2, 3, 4),
            Graph<Int>().apply {
                addEdge(1, 2)
                addEdge(2, 1)
                addEdge(3, 4)
                addEdge(4, 3)
                addEdge(1, 5)
                addEdge(5, 3)
            }.cyclicVertices(),
            "the bridge between the cycles is not a member"
        )
    }
}
