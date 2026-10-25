// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile

class RunPresentationTest {
    @Test
    fun ledgerUsesExecutionOrderAndKeepsUnreachedFilesDistinctFromFailures() {
        val clock = TestTimeSource()
        val ledger = RunPresentation(clock)
        val root = MPPPath(".")
        val first = ProjectFile(root.resolve("first.tex"), FileType("tex", "^%"), priority = 10)
        val second = ProjectFile(root.resolve("second.tex"), FileType("tex", "^%"))
        val pending = ProjectFile(root.resolve("pending.tex"), FileType("tex", "^%"))
        val base = Project("base", root, linkedSetOf(second, first))
        val dependent = Project("dependent", root, setOf(pending), setOf("base"))
        ledger.prepare(listOf(dependent, base))
        ledger.start(first, false)
        ledger.task()
        ledger.task()
        ledger.skip()
        clock += 1250.milliseconds
        ledger.finish(7)
        val result = ledger.summary(false, false)
        assertEquals(listOf("first.tex", "second.tex", "pending.tex"), result.files.map { it.path.fileName })
        assertEquals(listOf("base", "base", "dependent"), result.files.map { it.project })
        assertEquals(7, result.files.first().exitCode)
        assertEquals(1250.milliseconds, result.files.first().elapsed)
        assertEquals(2, result.files.first().tasks)
        assertEquals(1, result.files.first().skipped)
        assertNull(result.files[1].exitCode)
        assertNull(result.files[1].elapsed)
        assertNull(result.files[2].exitCode)
        ledger.task()
        ledger.finish(0)
        assertEquals(result, ledger.summary(false, false))
    }

    @Test
    fun snapshotsSurviveFurtherEvaluationAndPreparationClearsTheInvocation() {
        val ledger = RunPresentation(TestTimeSource())
        val root = MPPPath(".")
        val file = ProjectFile(root.resolve("run.tex"), FileType("tex", "^%"))
        ledger.prepare(listOf(Project("project", root, setOf(file))))
        ledger.start(file, true)
        ledger.task()
        val before = ledger.summary(true, true)
        ledger.deprecatedYaml()
        ledger.finish(2)
        assertEquals(true, ledger.deprecatedYamlUsed)
        assertNull(before.files.single().exitCode)
        assertEquals(true, before.files.single().dryRun)
        assertEquals(2, ledger.summary(false, true).files.single().exitCode)
        ledger.prepare(emptyList())
        assertEquals(false, ledger.deprecatedYamlUsed)
        assertEquals(emptyList(), ledger.summary(true, false).files)
    }
}
