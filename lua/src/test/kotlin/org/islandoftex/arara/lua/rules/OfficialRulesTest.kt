// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath

/**
 * Verifies every shipped Lua rule by parsing it through the real parser
 * and the real helper environment, so a rule that stopped loading — a
 * syntax error, a broken table shape, a command entry without its
 * function — fails the build instead of reaching users. The identifier
 * handed to the parser is derived from the rule file name the same way
 * directive dispatch derives it, which also pins the naming contract
 * that makes every shipped rule reachable by its directive.
 */
class OfficialRulesTest {
    @Test
    fun `every shipped Lua rule loads through the real parser`() {
        val rules = File("../rules")
            .listFiles()
            .orEmpty()
            .filter {
                it.name.startsWith("arara-rule-") && it.name.endsWith(".lua")
            }
        assertTrue(rules.isNotEmpty(), "the shipped Lua rules were not found")
        rules.sortedBy { it.name }.forEach { file ->
            val identifier = file.name.removePrefix("arara-rule-").removeSuffix(".lua")
            val rule = LuaRuleParser.parse(MPPPath(file.path), identifier)
            assertEquals(identifier, rule.identifier, "identifier of ${file.name}")
            assertTrue(rule.commands.isNotEmpty(), "commands of ${file.name}")
        }
    }
}
