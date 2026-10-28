// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.api.rules.RuleFormat
import org.islandoftex.arara.core.configuration.ExecutionOptions

class RuleLookupTest {
    @Test
    fun ruleLookupPreservesDirectoryAndFilenamePriorityWithFormatFallback() {
        val root = Files.createTempDirectory("arara rule lookup ").toMPPPath()
        try {
            val first = root.resolve("first").apply { createDirectories() }
            val second = root.resolve("second").apply { createDirectories() }
            first.resolve("probe.kts").writeText("error(\"must not execute\")")
            val yaml = second.resolve("probe.yaml").apply { writeText("") }
            val options = ExecutionOptions(rulePaths = linkedSetOf(MPPPath("first"), second))
            assertEquals(yaml, RuleLookup.resolve("probe", options, root, RuleEngine.YAML))
            assertEquals(yaml, RuleLookup.resolve("probe", options, root, RuleEngine.LUA))
            val prefixed = first.resolve("arara-rule-probe.yaml").apply { writeText("") }
            assertEquals(prefixed, RuleLookup.resolve("probe", options, root, RuleEngine.YAML))
            val direct = first.resolve("probe.yaml").apply { writeText("") }
            assertEquals(direct, RuleLookup.resolve("probe", options, root, RuleEngine.YAML))
            second.resolve("probe.lua").apply { writeText("") }
            assertEquals(direct, RuleLookup.resolve("probe", options, root, RuleEngine.LUA))
            val localLua = first.resolve("arara-rule-probe.lua").apply { writeText("") }
            assertEquals(localLua, RuleLookup.resolve("probe", options, root, RuleEngine.LUA))
            assertEquals(direct, RuleLookup.resolve("probe", options, root, RuleEngine.YAML))
            val fallback = second.resolve("only.lua").apply { writeText("") }
            assertEquals(fallback, RuleLookup.resolve("only", options, root, RuleEngine.YAML))
            assertEquals(first.resolve("probe.lua"),
                RuleLookup.construct(MPPPath("first"), "probe", RuleFormat.LUA, root))
            assertEquals(second.resolve("probe.yaml"),
                RuleLookup.construct(second, "probe", RuleFormat.MVEL, root))
        } finally {
            root.removeDirectory()
        }
    }
}
