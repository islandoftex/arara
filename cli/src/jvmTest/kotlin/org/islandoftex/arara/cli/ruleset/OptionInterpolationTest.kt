// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.core.localization.LanguageController

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

    @Test
    fun wrongEngineTagsUseTheSelectedLanguage() {
        val previous = LanguageController.messages
        try {
            for (locale in listOf("en", "de", "it", "nl", "pt-BR", "en-QN")) {
                LanguageController.loadMessagesFor(MPPLocale(locale))
                val error = assertFailsWith<AraraException> {
                    DirectiveUtils.interpolateOptions("{{ 1 }}", RuleEngine.YAML)
                }
                assertEquals(LanguageController.messages.ERROR_LUA_TAGS_IN_YAML, error.exception?.message)
            }
        } finally {
            LanguageController.messages = previous
        }
    }
}
