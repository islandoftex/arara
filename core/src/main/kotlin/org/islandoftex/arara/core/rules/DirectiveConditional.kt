// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType

/**
 * The conditional class, it represents the type of conditional available
 * for a directive and its corresponding expression to be evaluated.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
data class DirectiveConditional(
    override val type: DirectiveConditionalType = DirectiveConditionalType.NONE,
    override val condition: String = ""
) : DirectiveConditional {
    /**
     * Provides a textual representation of the conditional object.
     * @return A string representation of this object.
     */
    override fun toString(): String {
        return "{ $type" +
                if (type != DirectiveConditionalType.NONE)
                    ", expression: ${condition.trim()}"
                else "" + " }"
    }
}
