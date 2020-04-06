// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/**
 * A rule is the core of arara's execution. It describes how to execute
 * commands that will compile TeX files or run auxiliary tools.
 *
 * In the directive
 * ```
 * % arara: pdflatex: { shell: yes }
 * ```
 * `pdflatex` is a rule.
 */
interface Rule {
    /**
     * The rule's identifier is used to identify a rule within a directive. It
     * is unique within the ruleset arara has to load during a single run.
     */
    val identifier: String

    /**
     * The rule's display name is used to identify a rule in the terminal. A
     * display name is not mandatory.
     */
    val displayName: String?

    /**
     * The list of authors represented by the string representation of their
     * names.
     */
    val authors: List<String>

    /**
     * The list of commands this rule will execute.
     */
    val commands: List<RuleCommand>

    /**
     * The list of arguments which may be used to configure the runtime
     * behavior of the rule.
     */
    val arguments: List<RuleArgument<*>>
}
