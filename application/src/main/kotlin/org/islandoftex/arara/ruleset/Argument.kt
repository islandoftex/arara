// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.utils.CommonUtils

/**
 * The rule argument model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
class Argument {
    /**
     * The argument identifier
     */
    var identifier: String? = null
        get() = CommonUtils.removeKeyword(field)

    /**
     * Boolean indicating if the current argument is required
     */
    @SerialName("required")
    var isRequired: Boolean = false

    /**
     * Flag to hold the argument value manipulation
     */
    var flag: String? = null
        get() = CommonUtils.removeKeyword(field)

    /**
     * The argument fallback if it is not defined in the directive
     */
    var default: String? = null
        get() = CommonUtils.removeKeyword(field)
}
