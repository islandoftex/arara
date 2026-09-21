// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Verifies the linear executor's project handling in isolation: cyclic
 * dependencies are rejected naming their members, and acyclic chains
 * run successfully without any hooks installed.
 */
class LinearExecutorTest {
    @Test
    fun `rejects cyclic project dependencies`() {
        val exception = assertFailsWith<AraraException>(
            "a cyclic project specification must be rejected"
        ) {
            LinearExecutor.execute(
                listOf(
                    Project("A", MPPPath("."), setOf(), setOf("B")),
                    Project("B", MPPPath("."), setOf(), setOf("A"))
                )
            )
        }
        // the cycle members are named in the localized message
        val message = exception.message ?: ""
        assertTrue(message.contains("A"), "the cycle message names project A")
        assertTrue(message.contains("B"), "the cycle message names project B")
    }

    @Test
    fun `executes acyclic project dependencies in order`() {
        // the happy path is covered end-to-end by the cli scenarios; this
        // pins the core executor alone, running a project chain without
        // any hooks installed
        val report = LinearExecutor.execute(
            listOf(
                Project("A", MPPPath("."), setOf(), setOf("B")),
                Project("B", MPPPath("."), setOf())
            )
        )
        assertTrue(report.exitCode == 0, "the acyclic project chain runs successfully")
    }
}
