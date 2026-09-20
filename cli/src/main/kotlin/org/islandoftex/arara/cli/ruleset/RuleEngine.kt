// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

/**
 * The rule engines the user may choose from on the command line.
 *
 * arara defaults to the (deprecated) YAML rules; the Lua engine will
 * become the default once the YAML format is removed.
 */
enum class RuleEngine(val identifier: String) {
    /**
     * The YAML/MVEL rule engine (deprecated).
     */
    YAML("yaml"),

    /**
     * The Lua rule engine.
     */
    LUA("lua");

    /**
     * The rule formats this engine resolves, in lookup order.
     */
    @Suppress("DEPRECATION")
    val formats: List<RuleFormat>
        get() = when (this) {
            YAML -> listOf(RuleFormat.MVEL, RuleFormat.KOTLIN_DSL)
            LUA -> listOf(RuleFormat.LUA)
        }

    companion object {
        /**
         * The engine chosen for the current run (see the `--engine`
         * command line option).
         */
        @JvmStatic
        var current: RuleEngine = YAML
    }
}
