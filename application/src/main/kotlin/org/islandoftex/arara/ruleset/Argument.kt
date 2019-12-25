// SPDX-License-Identifier: BSD-3-Clause

package org.islandoftex.arara.ruleset

import org.islandoftex.arara.utils.CommonUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The rule argument model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
@Serializable
class Argument {
    // the argument identifier
    var identifier: String? = null
        get() = CommonUtils.removeKeyword(field)

    // a boolean indicating if the
    // current argument is required
    @SerialName("required")
    var isRequired: Boolean = false

    // the flag to hold the argument
    // value manipulation
    var flag: String? = null
        get() = CommonUtils.removeKeyword(field)

    // the argument fallback if it is
    // not defined in the directive
    var default: String? = null
        get() = CommonUtils.removeKeyword(field)
}
