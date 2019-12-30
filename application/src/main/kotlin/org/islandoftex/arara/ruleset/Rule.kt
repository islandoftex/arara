// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

import kotlinx.serialization.Serializable
import org.islandoftex.arara.utils.CommonUtils

/**
 * Implements the rule model.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
@Serializable
class Rule {
    // the rule identifier
    var identifier: String = INVALID_RULE_IDENTIFIER
        get() = CommonUtils.removeKeywordNotNull(field)

    // the rule name
    var name: String = INVALID_RULE_NAME
        get() = CommonUtils.removeKeywordNotNull(field)

    // the list of authors
    var authors: List<String> = listOf()
        get() = field.mapNotNull { CommonUtils.removeKeyword(it) }

    // the list of commands
    var commands: List<RuleCommand> = listOf()

    // the list of arguments
    var arguments: List<Argument> = listOf()

    companion object {
        const val INVALID_RULE_IDENTIFIER = "INVALID_RULE"
        const val INVALID_RULE_NAME = "INVALID_RULE"
    }
}
