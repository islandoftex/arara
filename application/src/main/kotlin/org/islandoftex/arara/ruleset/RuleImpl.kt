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
    override var identifier: String = Rule.INVALID_RULE_IDENTIFIER,
    @SerialName("name")
    override var displayName: String? = null,
    override var authors: List<String> = listOf(),
    override var commands: List<RuleCommandImpl> = listOf(),
    override var arguments: List<RuleArgumentImpl> = listOf()
) : Rule
