// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException

class OptionInterpolationTest {
    @Test
    fun scalarAndListOptionsKeepTheirShape() {
        assertEquals("value-3", DirectiveUtils.interpolateOptions("value-{{1+2}}", RuleEngine.LUA))
        assertEquals(listOf("a", "3"), DirectiveUtils.interpolateOptions(listOf("a", "{{1+2}}"), RuleEngine.LUA))
        assertEquals("3", DirectiveUtils.interpolateOptions("@{1+2}", RuleEngine.YAML).toString())
        assertEquals(listOf("a", "b"), DirectiveUtils.interpolateOptions(listOf("a", "b"), RuleEngine.YAML))
        assertFailsWith<AraraException> { DirectiveUtils.interpolateOptions("{{1+2}}", RuleEngine.YAML) }
        assertFailsWith<AraraException> { DirectiveUtils.interpolateOptions("@{1+2}", RuleEngine.LUA) }
    }
}
