// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.rules

import org.islandoftex.arara.api.rules.RuleCommand

data class RuleCommand(
    override val name: String?,
    override val command: () -> Int
) : RuleCommand
