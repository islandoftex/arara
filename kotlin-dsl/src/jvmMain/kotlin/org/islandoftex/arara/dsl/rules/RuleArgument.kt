// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.rules

import org.islandoftex.arara.api.rules.RuleArgument

data class RuleArgument<T>(
    override val identifier: String,
    override val isRequired: Boolean,
    override val defaultValue: T,
    override val processor: (T, Map<String, Any>) -> List<String> = { _, _ -> listOf() }
) : RuleArgument<T>
