// SPDX-License-Identifier: BSD-3-Clause

package org.islandoftex.arara.ruleset

import org.islandoftex.arara.utils.CommonUtils
import kotlinx.serialization.Serializable

/**
 * Implements the rule command model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
@Serializable
class RuleCommand {
    // the command name
    var name: String? = null
        get() = CommonUtils.removeKeyword(field)

    // the command instruction
    var command: String? = null
        get() = CommonUtils.removeKeyword(field)

    // the exit status expression
    var exit: String? = null
        get() = CommonUtils.removeKeyword(field)

}
