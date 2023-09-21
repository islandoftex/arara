// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectGraphTest {
    @Test
    fun shouldBeAbleToAddAll() {
        assertEquals(
            ProjectGraph().apply {
                addAll(
                    listOf(
                        Project("Test", MPPPath(""), setOf(), setOf("Test 2")),
                        Project("Test 2", MPPPath(""), setOf()),
                    ),
                )
            }.kahn().map { it.name },
            listOf("Test 2", "Test"),
        )
    }
}
