// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.rules.Rule

/**
 * Implements the rule model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
data class RuleImpl(
    override val identifier: String,
    @SerialName("name")
    override val displayName: String? = null,
    override val authors: List<String> = listOf(),
    override val commands: List<RuleCommandImpl> = listOf(),
    override val arguments: List<RuleArgumentImpl> = listOf()
) : Rule
