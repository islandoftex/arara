// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.Rule
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

    fun toRuleCommand(
        rule: Rule,
        parameters: Map<String, Any>
    ): RuleCommand = RuleCommandImpl(name) {
        TODO("implement this command")
    }
}

data class RuleCommandImpl(
    override val name: String?,
    override val command: () -> Int
) : RuleCommand
