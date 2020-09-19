// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.rules.RuleArgument

/**
 * The rule argument model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
class RuleArgument : RuleArgument<String?> {
    /**
     * The argument identifier
     */
    override var identifier: String = ""

    /**
     * Boolean indicating if the current argument is required
     */
    @SerialName("required")
    override var isRequired: Boolean = false

    /**
     * Flag to hold the argument value manipulation
     */
    var flag: String? = null
        get() = field?.trim()

    /**
     * The argument fallback if it is not defined in the directive
     */
    @SerialName("default")
    override var defaultValue: String? = null

    override fun toString(): String {
        return "RuleArgument(identifier='$identifier', isRequired=$isRequired, flag=$flag, defaultValue=$defaultValue)"
    }
}
