// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

/**
 * A graph model for dependency resolution.
 */
internal open class Graph<T>(private val vertexName: (T) -> String = { it.toString() }) {
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

        if (vertices[source]!!.add(target)) updateInDegree(target)
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
        if (order.size != vertices.size) {
            val cyclic = vertices.keys.filter { vertex ->
                work[vertex] != 0 && reachesBack(vertex)
            }.joinToString(", ", transform = vertexName)
            throw org.islandoftex.arara.api.AraraException("Project dependencies contain a cycle: $cyclic.")
        }
        return order
    }

    // Kahn's remainder includes downstream vertices; only report vertices that reach themselves.
    private fun reachesBack(vertex: T): Boolean {
        val seen = mutableSetOf<T>()
        val queue = ArrayDeque<T>()
        vertices[vertex].orEmpty().forEach(queue::addLast)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == vertex) return true
            if (seen.add(current)) vertices[current].orEmpty().forEach(queue::addLast)
        }
        return false
    }
}
