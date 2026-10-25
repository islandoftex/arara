// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

/**
 * Rule file suffixes.
 * @property extension Filename extension without its leading dot.
 */
enum class RuleFormat(val extension: String) {
    /** Lua rule tables evaluated with Luak. */
    LUA("lua"),

    /**
     * The traditional MVEL rules from version 1 on.
     */
    MVEL("yaml")
}
