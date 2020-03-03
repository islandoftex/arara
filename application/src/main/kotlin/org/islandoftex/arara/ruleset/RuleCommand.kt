// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

import kotlinx.serialization.Serializable
import org.islandoftex.arara.utils.CommonUtils

/**
 * Implements the rule command model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
class RuleCommand {
    /**
     * The command name
     */
    var name: String? = null
        get() = CommonUtils.removeKeyword(field)

    /**
     * The command instruction
     */
    var command: String? = null
        get() = CommonUtils.removeKeyword(field)

    /**
     * The exit status expression
     */
    var exit: String? = null
        get() = CommonUtils.removeKeyword(field)
}
