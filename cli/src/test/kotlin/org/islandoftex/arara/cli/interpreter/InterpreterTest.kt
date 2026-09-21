// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.Session
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Verifies the interpreter's halt contract: when a halt was requested for
 * the file being processed, [org.islandoftex.arara.cli.interpreter.Interpreter]
 * returns the requested status as the exit code without running any further
 * commands of the rule.
 *
 * The halt request itself (the `halt()` helper storing the status under the
 * halt key of the original file) requires the executor's project state, so
 * it is exercised end-to-end by the pipeline scenarios in
 * [org.islandoftex.arara.cli.utils.ExecutionTest]; this spec seeds the
 * session explicitly to observe the interpreter's decision in isolation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterpreterTest {
    private val texFile = FileType("tex", "^\\s*%\\s+")

    @BeforeAll
    fun setUp() {
        // the directive machinery has to be initialized, as the directive
        // construction relies on the installed hooks
        DirectiveUtils.initializeDirectiveCore()
    }

    @AfterAll
    fun tearDown() {
        // the scenarios seed halt keys to observe the interpreter's
        // decision; leaving them behind would short-circuit the halt
        // scenarios of the other specs
        Session.clear()
    }

    private fun haltDirectiveFor(filePath: MPPPath): org.islandoftex.arara.api.rules.Directive =
        Directives.extractDirectives(
            listOf("% arara: halt"),
            false, texFile
        ).single().run {
            DirectiveImpl(
                identifier, parameters.plus("reference" to filePath.toString()),
                conditional, lineNumbers
            )
        }

    private fun executeWithHaltStatus(
        rulePath: MPPPath,
        filePath: MPPPath,
        haltStatus: Int
    ): Int {
        // a fresh session guarantees the observed halt status is the one
        // this scenario seeds, not a leftover of a previous scenario
        Session.clear()
        Session.put("arara:${filePath.fileName}:halt", haltStatus)
        return Interpreter(
            ExecutionOptions(rulePaths = setOf(rulePath)),
            ProjectFile(filePath, texFile), filePath.parent
        ).execute(haltDirectiveFor(filePath)).exitCode
    }

    @Test
    fun `returns a zero exit code for an ordinary halt`() {
        val rulePath = MPPPath("../rules").normalize()
        val filePath = MPPPath("src/test/resources/executiontests/halt")
            .normalize() / "halt.tex"
        assertEquals(
            0,
            executeWithHaltStatus(rulePath, filePath, 0),
            "an ordinary halt exits with zero"
        )
    }

    @Test
    fun `returns a non-zero exit code for an error halt`() {
        val rulePath = MPPPath("src/test/resources/executiontests/halt-error")
            .normalize()
        val filePath = MPPPath(rulePath / "halt-error.tex")
        assertNotEquals(
            0,
            executeWithHaltStatus(rulePath, filePath, 1),
            "an error halt exits with a non-zero code"
        )
    }
}
