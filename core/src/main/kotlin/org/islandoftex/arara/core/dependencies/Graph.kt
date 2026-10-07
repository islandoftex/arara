// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

/**
 * A graph model for dependency resolution.
 */
internal open class Graph<T> {
    /**
     * The vertices of the graph, each mapped to the set of its
     * outgoing neighbours.
     */
    private val vertices = mutableMapOf<T, MutableSet<T>>()
    /**
     * The in-degree of each vertex: the number of edges pointing
     * into it.
     */
    private val indegree = mutableMapOf<T, Int>()

    /**
     * Updates the in-degree of a vertex after an edge
     * pointing into it is added.
     *
     * @param vertex The vertex whose in-degree grows.
     */
    private fun updateInDegree(vertex: T) {
        val current = indegree[vertex]
            ?: throw NoSuchElementException("Vertex $vertex in graph expected")
        indegree[vertex] = current + 1
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
     * Add an edge between two vertices. Implicitly creates these vertices
     * if not yet done.
     *
     * @param source The vertex where the edge starts.
     * @param target The vertex where the edge ends.
     */
    fun addEdge(source: T, target: T) {
        addVertex(source)
        addVertex(target)

        checkNotNull(vertices[source]).add(target)
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
        val queue = ArrayDeque<T>()

        val work = indegree.toMutableMap()

        work.entries
            .filter { it.value == 0 }
            .forEach { queue.add(it.key) }

        while (queue.isNotEmpty()) {
            val vertex: T = queue.removeFirst()
            order.add(vertex)
            val neighbors = vertices[vertex]
                ?: throw NoSuchElementException("Expected $vertex in map")
            neighbors.forEach {
                val remaining = checkNotNull(work[it]) {
                    "Vertex $it in graph expected"
                }
                work[it] = remaining - 1
                if (work[it] == 0) {
                    queue.add(it)
                }
            }
        }
        return order
    }

    /**
     * Computes the vertices that lie on a cycle. The topological
     * walk omits the cycle members together with every vertex
     * depending on them and every vertex bridging two cycles, so
     * each omitted vertex confirms its own membership: a vertex
     * lies on a cycle when one of its own out-neighbours reaches
     * it back.
     *
     * @return The vertices lying on a cycle, in insertion order.
     */
    fun cyclicVertices(): List<T> {
        val ordered = kahn().toSet()
        return vertices.keys.filter { vertex ->
            vertex !in ordered && reachesBack(vertex)
        }
    }

    /**
     * Tells whether one of the vertex's own out-neighbours
     * reaches the vertex back - the definition of lying on a
     * cycle, checked with a breadth-first walk over the graph's
     * edges.
     *
     * @param vertex The vertex asking about its own membership.
     * @return Whether a path leads out of the vertex and back to
     *   it.
     */
    private fun reachesBack(vertex: T): Boolean {
        val seen = mutableSetOf<T>()
        val queue = ArrayDeque<T>()
        vertices[vertex].orEmpty().forEach(queue::add)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == vertex) {
                return true
            }
            if (seen.add(current)) {
                vertices[current].orEmpty().forEach(queue::add)
            }
        }
        return false
    }
}
