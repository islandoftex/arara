// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    /** Optional human-readable task name. */
    override var name: String? = null
    /** MVEL command template, with outer whitespace trimmed on access. */
    @SerialName("command")
    var commandString: String? = null
        get() = field?.trim()

    /**
     * Boolean MVEL expression with the exit code available as `value`; null selects `value == 0`.
     */
    var exit: String? = null

    /** Describe the task name and command template for diagnostics. */
    override fun toString(): String {
        return "RuleCommand(name=$name, commandString=$commandString)"
    }
}
