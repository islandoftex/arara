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
public interface RuleArgument<T> {
    /**
     * The rule's identifier is its key to arara. This will be used in
     * directives. The identifier must not be blank.
     */
    public val identifier: String

    /**
     * Whether an argument is required.
     */
    public val isRequired: Boolean

    /**
     * The argument's default value. It will be used when the argument's
     * identifier is given without value.
     */
    public val defaultValue: T

    /**
     * Process the argument value of type [T] into a list of string. If
     * returned `emptyList()` this argument is either invalid or should not
     * contribute to the command. The `Map<String, Any>` argument represents
     * the parameter values as passed by the user (unprocessed).
     *
     * We map everything to `List<String>` to avoid issues when passing a
     * single string to the system's process management.
     */
    public val processor: (T, Map<String, Any>) -> List<String>
}
