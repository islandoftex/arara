// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

/**
 * Describe the rule formats supported by this CLI implementation.
 */
enum class RuleFormat(val extension: String) {
    /**
     * The traditional MVEL rules from version 1 on.
     */
    MVEL("yaml"),

    /**
     * The new and experimental Kotlin DSL format from version 6 on.
     */
    KOTLIN_DSL("kts")
}
