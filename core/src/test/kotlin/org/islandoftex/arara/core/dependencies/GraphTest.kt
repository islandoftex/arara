// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class GraphTest : ShouldSpec({
    should("operate correctly on empty list") {
        Graph<Int>().kahn() shouldBe listOf()
    }
    should("operate correctly on single element list") {
        Graph<Int>().apply { addVertex(1) }.kahn() shouldBe listOf(1)
    }
    should("be empty on cycles") {
        // TODO: is that the correct behavior we want to enforce?
        Graph<Int>().apply { addEdge(1, 1) }.kahn() shouldBe listOf()
    }
    should("return the right order") {
        Graph<Int>().apply {
            addEdge(6, 4)
            addEdge(6, 5)
            addEdge(4, 3)
            addEdge(4, 2)
            addEdge(3, 1)
        }.kahn() shouldBe listOf(6, 4, 5, 3, 2, 1)
    }
})
