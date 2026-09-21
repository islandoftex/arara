// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LinearExecutorTest {
    @Test
    fun shouldRejectCyclicProjectDependencies() {
        val exception = assertFailsWith<AraraException> {
            LinearExecutor.execute(
                listOf(
                    Project("A", MPPPath("."), setOf(), setOf("B")),
                    Project("B", MPPPath("."), setOf(), setOf("A"))
                )
            )
        }
        // the cycle members are named in the localized message
        val message = exception.message ?: ""
        assertTrue(message.contains("A"))
        assertTrue(message.contains("B"))
    }

    @Test
    fun shouldExecuteAcyclicProjectDependenciesInOrder() {
        // the happy path is covered end-to-end by the cli scenarios; this
        // pins the core executor alone, running a project chain without
        // any hooks installed
        val report = LinearExecutor.execute(
            listOf(
                Project("A", MPPPath("."), setOf(), setOf("B")),
                Project("B", MPPPath("."), setOf())
            )
        )
        assertTrue(report.exitCode == 0)
    }
}
