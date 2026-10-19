// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.core.session.Session
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutionRegressionTest {
    private fun execute(
        vararg commands: String,
        conditional: DirectiveConditional = DirectiveConditional(),
        haltOnErrors: Boolean = false
    ): Int {
        val directory = Files.createTempDirectory("arara execution regression ").toMPPPath()
        try {
            val file = directory.resolve("run.tex")
            directory.resolve("probe.yaml").writeText("""
                !config
                identifier: probe
                name: Probe
                commands:
            """.trimIndent() + "\n" + commands.joinToString("\n") {
                "  - command: \"@{ ${it.replace("\"", "\\\"")} }\""
            } + "\narguments: []\n")
            val options = ExecutionOptions(rulePaths = setOf(directory), haltOnErrors = haltOnErrors)
            val directive = DirectiveImpl(
                "probe", mapOf("reference" to file.toString()), conditional, listOf(1)
            )
            return Interpreter(options, ProjectFile(file, FileType("tex", "^%")), directory)
                .execute(directive).exitCode
        } finally {
            directory.removeDirectory()
            HaltRequest.clear()
            Session.clear()
        }
    }

    @Test
    fun successfulLoopIterationsDoNotEraseEarlierFailures() {
        Session.put("iteration", 0)
        assertEquals(1, execute(
            "n = getSession().get('iteration'); getSession().put('iteration', n + 1); return n != 0;",
            conditional = DirectiveConditional(DirectiveConditionalType.WHILE, "getSession().get('iteration') < 2")
        ))
    }

    @Test
    fun aSuccessfulHaltDoesNotEraseAnEarlierCommandFailure() {
        assertEquals(1, execute("return false;", "halt(0);"))
    }

    @Test
    fun anExplicitErrorHaltKeepsItsOwnStatus() {
        assertEquals(9, execute("return false;", "halt(9);"))
    }

    @Test
    fun successfulCommandsDoNotEraseFailuresInTheSameRule() {
        assertEquals(1, execute("return false;", "return true;"))
    }

    @Test
    fun haltOnErrorsPreventsSubsequentCommands() {
        assertEquals(1, execute(
            "return false;", "throw new RuntimeException('unreachable');", haltOnErrors = true
        ))
    }
}
