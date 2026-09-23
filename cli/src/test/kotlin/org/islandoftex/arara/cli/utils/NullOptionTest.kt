// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Pins the semantics of null-valued directive parameters: they count as if
 * the parameter had not been provided at all, so required arguments report
 * the proper missing-argument error and optional arguments fall back to
 * their defaults.
 *
 * Null values cannot be expressed through the YAML directive syntax (the
 * parser rejects them outright), so the null is injected into the processed
 * directive via the [ExecutorHooks.processDirectives] hook, mirroring a
 * parameter source that does allow nulls. The runs go through the full
 * linear executor pipeline because the rule helpers rely on its state.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NullOptionTest {
    private val rulePath = MPPPath("src/test/resources/executiontests/null-option")
        .normalize()

    // the executor state saved for restoration once the spec is done
    private val savedOptions = LinearExecutor.executionOptions
    private val savedHooks = LinearExecutor.hooks

    @BeforeAll
    fun setUp() {
        DirectiveUtils.initializeDirectiveCore()
    }

    @AfterAll
    fun tearDown() {
        // this spec mutates the executor state, so it has to be
        // restored once the spec is done, no matter whether the
        // scenarios held
        LinearExecutor.executionOptions = savedOptions
        LinearExecutor.hooks = savedHooks
    }

    private fun executeWithNullifiedParameter(name: String): Int {
        LinearExecutor.executionOptions =
            ExecutionOptions(rulePaths = setOf(rulePath))
        LinearExecutor.hooks = ExecutorHooks(
            processDirectives = { file, list ->
                DirectiveUtils.process(file, list).map { directive ->
                    @Suppress("UNCHECKED_CAST")
                    DirectiveImpl(
                        directive.identifier,
                        directive.parameters.plus(name to null)
                            as Map<String, Any>,
                        directive.conditional,
                        directive.lineNumbers
                    )
                }
            }
        )
        return LinearExecutor.execute(
            listOf(
                Project(
                    "Test", rulePath,
                    setOf(
                        FileSearching.resolveFile(
                            "null-option.tex", rulePath,
                            LinearExecutor.executionOptions
                        )
                    )
                )
            )
        ).exitCode
    }

    @Test
    fun `treats null directive parameters as absent so defaults apply`() {
        // 'opt' is explicitly provided but replaced by null; the rule's
        // command guard throws unless the default is used, so a zero exit
        // code proves that the null fell back to the default
        assertEquals(
            0,
            executeWithNullifiedParameter("opt"),
            "the nullified optional parameter falls back to its default"
        )
    }

    @Test
    fun `still requires required arguments provided as null`() {
        // 'req' is explicitly provided but replaced by null, which counts
        // as absent; the message is localized, but every locale names the
        // offending argument. Previously the null bypassed the required
        // check and crashed the flag evaluation instead
        val exception = assertFailsWith<AraraException>(
            "a nullified required argument must report the missing " +
                "argument error"
        ) {
            executeWithNullifiedParameter("req")
        }
        assertTrue(
            exception.message?.contains("req") == true,
            "the error names the required argument"
        )
    }
}
