// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.dsl.scripting.RuleMethods

/**
 * A rule argument model to capture DSL methods within.
 */
class DSLRuleArgument<T>(val identifier: String) {
    /**
     * The final processor function of the argument to transform the directive
     * option into the string that may be used later on.
     */
    private var processor: (T) -> String = { it.toString() }

    /**
     * Whether this argument is required in a directive.
     */
    var required: Boolean = false

    /**
     * The default value of the argument. If null and `T` is non-null you have
     * to specify a sef
     */
    var defaultValue: T? = null

    /**
     * Set the processor function to process the argument give in the directive.
     */
    fun processor(fn: RuleMethods.(T) -> String) {
        processor = { fn(RuleMethods, it) }
    }

    /**
     * Turn this DSL object into arara's core object.
     *
     * @return A [RuleArgument] resembling the user's configuration.
     */
    inline fun <reified T> toRuleArgument(): RuleArgument<T> {
        val default = if (defaultValue is T)
            defaultValue as T
        else
            throw AraraException("The default value has to match the given " +
                    "rule type.")
        return org.islandoftex.arara.dsl.rules.RuleArgument(
                identifier, isRequired = required, defaultValue = default
        )
    }
}
