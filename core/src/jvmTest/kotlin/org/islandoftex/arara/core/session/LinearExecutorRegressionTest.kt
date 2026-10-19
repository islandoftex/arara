// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.DirectiveConditional
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LinearExecutorRegressionTest {
    private val previousHooks = LinearExecutor.hooks
    private val previousOptions = LinearExecutor.executionOptions

    @BeforeTest
    fun setup() {
        Session.clear()
        LinearExecutor.hooks = ExecutorHooks()
        LinearExecutor.executionOptions = ExecutionOptions(haltOnErrors = false)
    }

    @AfterTest
    fun restore() {
        LinearExecutor.hooks = previousHooks
        LinearExecutor.executionOptions = previousOptions
        Session.clear()
    }

    private fun file(name: String, vararg actions: () -> Int): ProjectFile = object : ProjectFile(
        MPPPath(name), FileType("tex", "^%")
    ) {
        override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> = actions.map { action ->
            object : Directive {
                override val identifier = "test"
                override val parameters = emptyMap<String, Any>()
                override val conditional = DirectiveConditional()
                override val lineNumbers = emptyList<Int>()
                override fun execute(): Int = action()
            }
        }
    }

    @Test
    fun laterSuccessfulDirectivesAndFilesDoNotHideFailures() {
        val project = Project("Test", MPPPath("."), linkedSetOf(
            file("one.tex", { 7 }, { 0 }), file("two.tex", { 0 })
        ))
        assertEquals(7, LinearExecutor.execute(listOf(project)).exitCode)
    }

    @Test
    fun laterSuccessfulProjectsDoNotHideFailures() {
        val one = Project("One", MPPPath("."), setOf(file("one.tex", { 9 })))
        val two = Project("Two", MPPPath("."), setOf(file("two.tex", { 0 })), setOf("One"))
        assertEquals(9, LinearExecutor.execute(listOf(one, two)).exitCode)
    }

    @Test
    fun exceptionsClearCurrentFileAndProjectAndAllowAnotherRun() {
        val broken = Project("Broken", MPPPath("."), setOf(file("broken.tex", { throw AraraException("broken") })))
        assertFailsWith<AraraException> { LinearExecutor.execute(listOf(broken)) }
        assertNull(LinearExecutor.currentFile)
        assertNull(LinearExecutor.currentProject)
        LinearExecutor.executionOptions = ExecutionOptions()
        val next = Project("Next", MPPPath("."), setOf(file("next.tex", { 0 })))
        assertEquals(0, LinearExecutor.execute(listOf(next)).exitCode)
    }

    @Test
    fun aSuccessfulHaltStopsOnlyItsOwnFileAndDoesNotLeakToTheNextRun() {
        val visited = mutableListOf<String>()
        val one = file("one.tex", { HaltRequest.request(0); 0 }, { visited.add("unreachable"); 0 })
        val two = file("two.tex", { visited.add("two"); 0 })
        assertEquals(0, LinearExecutor.execute(listOf(Project("Test", MPPPath("."), linkedSetOf(one, two)))).exitCode)
        assertEquals(listOf("two"), visited)
        assertEquals(0, LinearExecutor.execute(listOf(Project("Again", MPPPath("."), setOf(file("one.tex", { 0 }))))).exitCode)
    }

    @Test
    fun hooksObserveTheirDocumentedFileAndProjectLifetimeOnFailure() {
        val events = mutableListOf<String>()
        val project = Project("Hooks", MPPPath("."), setOf(file("hook.tex", { 7 })))
        LinearExecutor.hooks = ExecutorHooks(
            executeBeforeProject = { assertEquals(project, LinearExecutor.currentProject); events.add("project-start") },
            executeBeforeFile = { assertNull(LinearExecutor.currentFile); events.add("file-start") },
            executeAfterFile = {
                assertNull(LinearExecutor.currentFile)
                assertEquals(project, LinearExecutor.currentProject)
                assertEquals(7, it.exitCode)
                events.add("file-end")
            },
            executeAfterProject = { assertEquals(project, LinearExecutor.currentProject); events.add("project-end") },
            executeAfterExecution = { assertEquals(7, it.exitCode); events.add("execution-end") }
        )
        LinearExecutor.executionOptions = ExecutionOptions(haltOnErrors = true)
        assertEquals(7, LinearExecutor.execute(listOf(project)).exitCode)
        assertEquals(listOf("project-start", "file-start", "file-end", "project-end", "execution-end"), events)
        assertNull(LinearExecutor.currentProject)
    }

    @Test
    fun aFailingBeforeProjectHookClearsContextAndAllowsAnotherRun() {
        val project = Project("Hook", MPPPath("."), setOf(file("hook.tex", { 0 })))
        LinearExecutor.hooks = ExecutorHooks(executeBeforeProject = { throw AraraException("hook failure") })
        assertFailsWith<AraraException> { LinearExecutor.execute(listOf(project)) }
        assertNull(LinearExecutor.currentProject)
        assertNull(LinearExecutor.currentFile)
        LinearExecutor.hooks = ExecutorHooks()
        assertEquals(0, LinearExecutor.execute(listOf(project)).exitCode)
    }

    @Test
    fun prioritiesTakePrecedenceOverNormalizedPathTieBreaking() {
        val visited = mutableListOf<String>()
        val a = file("a.tex", { visited.add("a"); 0 })
        val b = file("b.tex", { visited.add("b"); 0 })
        val high = object : ProjectFile(MPPPath("z.tex"), FileType("tex", "^%"), 1) {
            override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> = emptyList()
        }
        LinearExecutor.hooks = ExecutorHooks(executeBeforeFile = { if (it === high) visited.add("high") })
        val project = Project("Order", MPPPath("."), linkedSetOf(b, a, high))
        assertEquals(0, LinearExecutor.execute(listOf(project)).exitCode)
        assertEquals(listOf("high", "a", "b"), visited)
    }

    @Test
    fun cyclesFailBeforeAnyExecutionHookRuns() {
        var started = false
        LinearExecutor.hooks = ExecutorHooks(executeBeforeExecution = { started = true })
        val project = Project("Cycle", MPPPath("."), setOf(file("a.tex", { 0 })), setOf("Cycle"))
        assertFailsWith<AraraException> { LinearExecutor.execute(listOf(project)) }
        assertEquals(false, started)
    }

}
