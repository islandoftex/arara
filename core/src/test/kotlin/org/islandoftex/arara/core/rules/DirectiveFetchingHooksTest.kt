// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType

/**
 * Verifies the directive fetching hooks' defaults: potential directives
 * pass through untouched, and the uninitialized builders reject with
 * the developer error - the command line application has to install
 * the real hooks before extracting directives.
 */
class DirectiveFetchingHooksTest {
    private val noConditional = object : DirectiveConditional {
        override val type = DirectiveConditionalType.NONE
        override val condition = ""
    }

    @Test
    fun `potential directives pass through the default`() {
        assertEquals(
            "directive",
            DirectiveFetchingHooks().processPotentialDirective(1, "directive"),
            "the default passes the text through unchanged"
        )
    }

    @Test
    fun `the uninitialized builders reject with the developer error`() {
        val hooks = DirectiveFetchingHooks()
        assertFailsWith<IllegalStateException> {
            hooks.buildDirectiveRaw("id", null, noConditional, listOf(1))
        }
        assertFailsWith<IllegalStateException> {
            hooks.buildDirective("id", mapOf(), noConditional, listOf(1))
        }
    }
}
