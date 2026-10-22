// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.DirectiveConditional

class RuleFilesTest {
    @Test
    fun ruleLookupPreservesDirectoryAndFilenamePriorityAndEngineIsolation() {
        val root = Files.createTempDirectory("arara rule lookup ").toMPPPath()
        try {
            val first = root.resolve("first").apply { createDirectories() }
            val second = root.resolve("second").apply { createDirectories() }
            first.resolve("probe.kts").writeText("error(\"must not execute\")")
            val yaml = second.resolve("probe.yaml").apply { writeText("") }
            val directive = DirectiveImpl("probe", emptyMap(), DirectiveConditional(), listOf(1))
            val options = ExecutionOptions(rulePaths = linkedSetOf(MPPPath("first"), second))
            assertEquals(yaml, RuleFiles.find(directive, options, root, RuleEngine.YAML))
            val prefixed = first.resolve("arara-rule-probe.yaml").apply { writeText("") }
            assertEquals(prefixed, RuleFiles.find(directive, options, root, RuleEngine.YAML))
            val direct = first.resolve("probe.yaml").apply { writeText("") }
            assertEquals(direct, RuleFiles.find(directive, options, root, RuleEngine.YAML))
            val lua = second.resolve("probe.lua").apply { writeText("") }
            assertEquals(lua, RuleFiles.find(directive, options, root, RuleEngine.LUA))
            assertEquals(first.resolve("probe.lua"),
                RuleFiles.construct(MPPPath("first"), "probe", RuleFormat.LUA, root))
            assertEquals(second.resolve("probe.yaml"),
                RuleFiles.construct(second, "probe", RuleFormat.MVEL, root))
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun unsupportedRuleFormatsReportAnAraraException() {
        val file = MPPPath("probe.kts")
        val failure = assertFailsWith<AraraException> { RuleFiles.parseRule(file, "probe") }
        assertEquals(LanguageController.messages.errorParseRuleUnsupportedFormat("probe", file.toString()), failure.message)
    }
}
