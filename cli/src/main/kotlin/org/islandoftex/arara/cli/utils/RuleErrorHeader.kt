// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements a singleton holding the current rule for error output
 * generation.
 *
 * TODO: remove this object
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object RuleErrorHeader {
    /**
     * The rule identifier to display in the rule error header.
     */
    var ruleId: String? = null

    /**
     * The rule path to display in the rule error header.
     */
    var rulePath: String? = null

    /**
     * Gets the rule error header, containing the identifier and the path, if
     * any.
     *
     * @return A string representation of the rule error header, containing the
     * identifier and the path, if any.
     */
    @JvmStatic
    val ruleErrorHeader: String
        get() {
            return ruleId?.let { id ->
                rulePath?.let { path ->
                    LanguageController.messages.ERROR_RULE_IDENTIFIER_AND_PATH
                            .format(id, path) + " "
                }
            } ?: ""
        }
}
