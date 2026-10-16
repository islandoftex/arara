// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.rules.DirectiveConditionalType

/**
 * Verifies the textual representation of the directive conditional, as
 * shown in the validated directives log block.
 */
class DirectiveConditionalTest {
    @Test
    fun `renders a bare conditional without an expression`() {
        assertEquals(
            "{ NONE }",
            DirectiveConditional().toString(),
            "a conditional without a type renders with the braces closed"
        )
    }

    @Test
    fun `renders a typed conditional with its expression`() {
        assertEquals(
            "{ IF, expression: exists('log') }",
            DirectiveConditional(
                DirectiveConditionalType.IF, "exists('log') "
            ).toString(),
            "a typed conditional renders with the expression inside " +
                "closed braces"
        )
    }
}
