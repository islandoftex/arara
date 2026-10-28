// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/**
 * Rule file suffixes.
 * @property extension Filename extension without its leading dot.
 */
public enum class RuleFormat(public val extension: String) {
    /** Lua rule tables. */
    LUA("lua"),

    /**
     * The traditional MVEL rules from version 1 on.
     */
    MVEL("yaml")
}
