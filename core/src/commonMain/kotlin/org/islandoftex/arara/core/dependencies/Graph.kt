// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

/**
 * A graph model for dependency resolution.
 */
internal open class Graph<T> {
    private val vertices = mutableMapOf<T, MutableSet<T>>()
    private val indegree = mutableMapOf<T, Int>()

    private fun updateInDegree(vertex: T) {
        if (vertex in indegree)
            indegree[vertex] = indegree[vertex]!!.inc()
        else
            throw NoSuchElementException("Vertex $vertex in graph expected")
    }

    /**
     * Add a vertex to the graph. It will be initialized with 0 neighbors.
     *
     * @param vertex The new vertex.
     */
    fun addVertex(vertex: T) {
        if (vertex !in vertices)
            vertices[vertex] = mutableSetOf()
        if (vertex !in indegree)
            indegree[vertex] = 0
    }

    /**
     * Add an edge between to vertices. Implicitly creates these vertices if
     * not yet done.
     *
     * @param source The vertex where the edge starts.
     * @param target The vertex where the edge ends.
     */
    fun addEdge(source: T, target: T) {
        addVertex(source)
        addVertex(target)

        vertices[source]!!.add(target)
        updateInDegree(target)
    }

    /**
     * Execute Kahn's algorithm on this graph to generate the right order of
     * execution.
     *
     * @return The vertices in order.
     */
    fun kahn(): List<T> {
        val order = mutableListOf<T>()
        val queue = mutableListOf<T>()

        val work = indegree.toMutableMap()

        work.entries
            .filter { it.value == 0 }
            .forEach { queue.add(it.key) }

        while (queue.isNotEmpty()) {
            val vertex: T = queue.removeAt(0)
            order.add(vertex)
            vertices[vertex]?.forEach {
                work[it] = work[it]!!.dec()
                if (work[it] == 0) {
                    queue.add(it)
                }
            } ?: throw NoSuchElementException("Expected $vertex in map")
        }
        return order
    }
}
