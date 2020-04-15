// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.rules

import org.islandoftex.arara.api.rules.RuleArgument

class RuleArgument<T>(
    override val identifier: String,
    override val isRequired: Boolean,
    override val defaultValue: T
) : RuleArgument<T>
