// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals

class GraphTest {
    @Test
    fun shouldOperateCorrectlyOnEmptyList() {
        assertEquals(Graph<Int>().kahn(), emptyList())
    }
    @Test
    fun shouldOperateCorrectlyOnSingletonList() {
        assertEquals(Graph<Int>().apply { addVertex(1) }.kahn(), listOf(1))
    }
    @Test
    fun shouldBeEmptyOnCycles() {
        // TODO: is that the correct behavior we want to enforce?
        assertEquals(Graph<Int>().apply { addEdge(1, 1) }.kahn(), emptyList())
    }
    @Test
    fun shouldSortCorrectly() {
        assertEquals(
            Graph<Int>().apply {
                addEdge(6, 4)
                addEdge(6, 5)
                addEdge(4, 3)
                addEdge(4, 2)
                addEdge(3, 1)
            }.kahn(), listOf(6, 4, 5, 3, 2, 1)
        )
    }
}
