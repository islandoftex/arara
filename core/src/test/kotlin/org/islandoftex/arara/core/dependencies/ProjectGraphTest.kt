// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project

/**
 * Verifies that project graphs order dependencies before their
 * dependents.
 */
class ProjectGraphTest {
    @Test
    fun `adds projects and their dependencies to the graph`() {
        assertEquals(
            ProjectGraph().apply {
                addAll(
                    listOf(
                        Project("Test", MPPPath(""), setOf(), setOf("Test 2")),
                        Project("Test 2", MPPPath(""), setOf())
                    )
                )
            }.kahn().map { it.name },
            listOf("Test 2", "Test"),
            "dependencies are executed before their dependents"
        )
    }
}
