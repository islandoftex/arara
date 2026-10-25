// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.ruleset.RuleEngine
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.DirectiveConditional

class RuleDiscoveryTest {
    @Test
    fun missingRulesReportTheSelectedExtension() {
        val root = Files.createTempDirectory("arara discovery ").toMPPPath()
        try {
            val input = ProjectFile(root.resolve("run.tex"), FileType("tex", "^%"))
            listOf(RuleEngine.LUA to "lua", RuleEngine.YAML to "yaml").forEach { (engine, extension) ->
                val error = assertFailsWith<AraraException> {
                    Interpreter(ExecutionOptions(rulePaths = setOf(root)), input, root, engine).execute(
                        DirectiveImpl("probe", emptyMap(), DirectiveConditional(), listOf(1), engine))
                }
                assertTrue(error.message.orEmpty().contains("probe.$extension"))
            }
        } finally {
            root.removeDirectory()
        }
    }
}
