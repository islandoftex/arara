// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/** Selects rule files, conditional expressions and option interpolation. */
public enum class RuleEngine {
    /** Prefer Lua rules, falling back to YAML within each search directory. */
    LUA,
    /** Prefer legacy YAML/MVEL rules, falling back to Lua within each search directory. */
    YAML
}
