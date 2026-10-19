// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.ruleset.RuleEngine
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.DirectiveConditional
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.core.session.Session

class LuaExecutionRegressionTest {
    private fun execute(vararg bodies: String, conditional: DirectiveConditional = DirectiveConditional()): Int {
        val directory = Files.createTempDirectory("arara Lua execution ").toMPPPath()
        try {
            val file = directory.resolve("run.tex")
            directory.resolve("probe.lua").writeText(
                "return {identifier='probe', name='Probe', commands={" +
                    bodies.joinToString(",") { "{command=function(ctx) $it end}" } + "}}"
            )
            val directive = DirectiveImpl("probe", mapOf("reference" to file), conditional, listOf(1), RuleEngine.LUA)
            return Interpreter(ExecutionOptions(haltOnErrors = false, rulePaths = setOf(directory)),
                ProjectFile(file, FileType("tex", "^%")), directory, RuleEngine.LUA).execute(directive).exitCode
        } finally {
            directory.removeDirectory()
            Session.clear()
            HaltRequest.clear()
        }
    }

    @Test
    fun laterLuaResultsAndSuccessfulHaltDoNotEraseFailures() {
        assertEquals(1, execute("return {false, {true}}", "return true"))
        assertEquals(1, execute("return false", "halt(0)"))
        assertEquals(9, execute("return false", "halt(9)"))
    }

    @Test
    fun successfulLuaIterationsDoNotEraseEarlierFailures() {
        Session.put("lua-iteration", 0)
        assertEquals(1, execute(
            "local s=get_session(); local n=s.get('lua-iteration'); s.put('lua-iteration', n+1); return n ~= 0",
            conditional = DirectiveConditional(DirectiveConditionalType.WHILE, "get_session().get('lua-iteration') < 2")
        ))
    }
}
