// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/**
 * This interface describes an argument to a rule. arara will look for
 * arguments in the source file (directives).
 *
 * In the directive
 * ```
 * % arara: pdflatex: { shell: yes }
 * ```
 * `shell` is an argument.
 */
interface RuleArgument<T> {
    /**
     * The rule's identifier is its key to arara. This will be used in
     * directives. The identifier must not be blank.
     */
    val identifier: String

    /**
     * Whether an argument is required.
     */
    val isRequired: Boolean

    /**
     * The argument's default value. It will be used when the argument's
     * identifier is given without value.
     */
    val defaultValue: T
}
