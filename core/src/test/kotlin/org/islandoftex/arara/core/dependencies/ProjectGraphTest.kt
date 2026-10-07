// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
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

    @Test
    fun `reports a stray dependency in the merge validation's shape`() {
        val exception = assertFailsWith<AraraException> {
            ProjectGraph().apply {
                addAll(
                    listOf(
                        Project("Test", MPPPath(""), setOf(), setOf("Nowhere"))
                    )
                )
            }
        }
        assertTrue(
            "(Nowhere)" in exception.message!!,
            "the stray name renders as the parenthesized enumeration: ${exception.message}"
        )
    }
}
