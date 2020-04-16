// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.Arara
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements common utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object CommonUtils {
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
    val ruleErrorHeader: String
        get() {
            return ruleId?.let { id ->
                rulePath?.let { path ->
                    LanguageController.messages.ERROR_RULE_IDENTIFIER_AND_PATH
                            .format(id, path) + " "
                }
            } ?: ""
        }

    /**
     * Gets the list of file types as string, in order.
     *
     * @return A string representation of the list of file types, in order.
     */
    val fileTypesList: String
        get() = Arara.config[AraraSpec.executionOptions].fileTypes
                .joinToString(" | ", "[ ", " ]")
}
