// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.RuleCommand

/**
 * Implements the rule command model.
 */
@Serializable
class SerialRuleCommand : RuleCommand {
    /**
     * The name the command entry displays itself with, if any.
     */
    override var name: String? = null
    /**
     * Always unsupported: the command entry evaluates through the MVEL
     * template machinery instead of a closure.
     */
    override val command: () -> Int
        get() = throw AraraException("Operation not supported for SerialRuleCommand")

    // the template arrives with the surrounding whitespace of its
    // block in the rule file; the trimmed form is what the
    // evaluator sees and the log shows
    /**
     * The MVEL expression the command entry evaluates against the
     * resolved arguments.
     */
    @SerialName("command")
    var commandString: String? = null
        get() = field?.trim()

    /**
     * The exit status expression
     */
    var exit: String? = null

    /**
     * A human-readable representation of the command entry, for logs
     * and error reports.
     */
    override fun toString(): String {
        return "RuleCommand(name=$name, commandString=$commandString)"
    }
}
