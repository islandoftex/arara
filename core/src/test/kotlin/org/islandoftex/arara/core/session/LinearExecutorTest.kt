// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.files.UNKNOWN_TYPE
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.rules.RuleMethods

/**
 * Verifies the linear executor in isolation: cyclic project
 * dependencies are rejected naming their members, acyclic chains
 * run without any hooks installed, the executor window rejects
 * execution option changes, failures travel across the
 * directives, files and projects of a run, and a halt request
 * lives exactly as long as the file that raised it. The
 * directives are stubs with chosen exit codes, so the executor's
 * loops are measured without the interpreter behind them.
 */
class LinearExecutorTest {
    private val originalOptions = LinearExecutor.executionOptions
    private val originalHooks = LinearExecutor.hooks

    @AfterTest
    fun restoreTheExecutor() {
        LinearExecutor.executionOptions = originalOptions
        LinearExecutor.hooks = originalHooks
    }

    @Test
    fun `rejects execution option changes while a file is under run`() {
        // the executor window holds the current file open for the hook,
        // so an option change inside that window must be rejected; a
        // stale file would otherwise freeze the options forever
        org.islandoftex.arara.core.rules.executeWithinProject(
            listOf("main.tex" to 0)
        ) { _, _ ->
            assertFailsWith<AraraException>(
                "an open file rejects execution option changes"
            ) {
                LinearExecutor.executionOptions = LinearExecutor.executionOptions
            }
        }
    }

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

    /**
     * A directive stub reporting a chosen exit code.
     */
    private fun directive(code: Int): Directive = object : Directive {
        override val identifier = "stub"
        override val parameters = mapOf("reference" to "")
        override val conditional = DirectiveConditional()
        override val lineNumbers = listOf(1)
        override fun execute() = code
    }

    /**
     * A temporary project file the stubs run against.
     */
    private fun projectFile(suffix: String): ProjectFile {
        val file = Files.createTempFile("arara-executor-", suffix)
        return ProjectFile(
            path = MPPPath(file.toString()),
            fileType = FileType.UNKNOWN_TYPE
        )
    }

    /**
     * A single project holding the provided files.
     */
    private fun projectOf(vararg files: ProjectFile) = Project(
        "Untitled",
        MPPPath("."),
        files.toSet()
    )

    @Test
    fun `a run with error-halting reports its first error`() {
        LinearExecutor.executionOptions = ExecutionOptions(haltOnErrors = true)
        LinearExecutor.hooks = ExecutorHooks(
            processDirectives = { _, _ ->
                listOf(directive(0), directive(1), directive(2))
            }
        )
        val report = LinearExecutor.execute(
            listOf(projectOf(projectFile(".tex")))
        )
        assertEquals(
            1,
            report.exitCode,
            "the first error stops the run and names its code"
        )
    }

    @Test
    fun `a run without error-halting remembers its last error`() {
        LinearExecutor.executionOptions = ExecutionOptions(haltOnErrors = false)
        LinearExecutor.hooks = ExecutorHooks(
            processDirectives = { _, _ ->
                listOf(directive(1), directive(3), directive(0))
            }
        )
        val report = LinearExecutor.execute(
            listOf(projectOf(projectFile(".tex")))
        )
        assertEquals(
            3,
            report.exitCode,
            "a later success cannot mask the last error the run saw"
        )
    }

    @Test
    fun `a failure in an early file survives a later file's success`() {
        LinearExecutor.executionOptions = ExecutionOptions(haltOnErrors = false)
        val first = projectFile("-first.tex")
        val second = projectFile("-second.tex")
        LinearExecutor.hooks = ExecutorHooks(
            processDirectives = { file, _ ->
                if (file == first) listOf(directive(1)) else listOf(directive(0))
            }
        )
        val report = LinearExecutor.execute(listOf(projectOf(first, second)))
        assertEquals(
            1,
            report.exitCode,
            "the run keeps the error of the file that failed"
        )
    }

    @Test
    fun `a halt request dies with the file that raised it`() {
        val halting = projectFile("-halting.tex")
        val after = projectFile("-after.tex")
        var requestSeenDuringProcessing = false
        var afterFileRan = false
        LinearExecutor.executionOptions = ExecutionOptions(haltOnErrors = true)
        LinearExecutor.hooks = ExecutorHooks(
            processDirectives = { file, _ ->
                if (file == halting) {
                    listOf(
                        object : Directive {
                            override val identifier = "halting"
                            override val parameters = mapOf("reference" to "")
                            override val conditional = DirectiveConditional()
                            override val lineNumbers = listOf(1)
                            override fun execute(): Int {
                                RuleMethods.halt(0)
                                requestSeenDuringProcessing =
                                    Session.contains(LinearExecutor.haltKeyFor(halting))
                                return 0
                            }
                        }
                    )
                } else {
                    listOf(
                        object : Directive {
                            override val identifier = "after"
                            override val parameters = mapOf("reference" to "")
                            override val conditional = DirectiveConditional()
                            override val lineNumbers = listOf(1)
                            override fun execute(): Int {
                                afterFileRan = true
                                return 0
                            }
                        }
                    )
                }
            }
        )
        LinearExecutor.execute(listOf(projectOf(halting, after)))
        assertTrue(
            requestSeenDuringProcessing,
            "the halt request is recorded while the file runs"
        )
        assertTrue(
            afterFileRan,
            "a succeeding halt lets the run proceed to the next file"
        )
        assertFalse(
            Session.contains(LinearExecutor.haltKeyFor(halting)),
            "the file's own end consumes the halt request it raised"
        )
    }
}
