// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.RuleCommand

/**
 * Implements the rule command model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
class SerialRuleCommand : RuleCommand {
    override var name: String? = null
    override val command: () -> Int
        get() = throw AraraException("Operation not supported for SerialRuleCommand")

    // TODO: why is trimming needed?
    @SerialName("command")
    var commandString: String? = null
        get() = field?.trim()

    /**
     * The exit status expression
     */
    var exit: String? = null
}
