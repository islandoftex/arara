// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

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
    private var processor: (T, Map<String, Any>) -> List<String> = { it, _ ->
        listOf(it.toString())
    }

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
    // TODO: only accept String and List<String>
    @JvmName("processorStringList")
    fun processor(fn: RuleMethods.(T) -> Any) {
        processor = { it, _ ->
            val result = fn(RuleMethods, it)
            if (result is List<*>)
                result.map { it.toString() }
            else
                listOf(it.toString())
        }
    }

    /**
     * Turn this DSL object into arara's core object.
     *
     * @return A [RuleArgument] resembling the user's configuration.
     */
    fun toRuleArgument(): RuleArgument<T> {
        return org.islandoftex.arara.dsl.rules.RuleArgument(
                identifier, isRequired = required, defaultValue = defaultValue,
                processor = processor
        )
    }
}
