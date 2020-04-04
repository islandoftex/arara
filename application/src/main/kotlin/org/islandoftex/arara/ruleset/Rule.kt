// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

import kotlinx.serialization.Serializable

/**
 * Implements the rule model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
class Rule {
    /**
     * The rule identifier
     */
    var identifier: String = INVALID_RULE_IDENTIFIER

    /**
     * The rule name
     */
    var name: String = INVALID_RULE_NAME

    /**
     * The list of authors
     */
    var authors: List<String> = listOf()
        get() = field.map { it.trim() }

    /**
     * The list of commands
     */
    var commands: List<RuleCommand> = listOf()

    /**
     * The list of arguments
     */
    var arguments: List<Argument> = listOf()

    companion object {
        const val INVALID_RULE_IDENTIFIER = "INVALID_RULE"
        const val INVALID_RULE_NAME = "INVALID_RULE"
    }
}
