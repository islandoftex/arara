// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.rules

import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.rules.RuleCommand

data class Rule(
    override val identifier: String,
    override val displayName: String?,
    override val authors: List<String>,
    override val commands: List<RuleCommand>,
    override val arguments: List<RuleArgument<*>>
) : Rule
