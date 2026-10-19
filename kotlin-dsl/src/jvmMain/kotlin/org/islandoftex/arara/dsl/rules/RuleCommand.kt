// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.rules

import org.islandoftex.arara.api.rules.ExecutableRuleCommand

data class RuleCommand(
    override val name: String?,
    override val command: () -> Int
) : ExecutableRuleCommand
