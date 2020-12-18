// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/**
 * A directive is a pattern within a source file that tells arara which rules
 * to apply in order to process the source.
 */
public interface Directive {
    /**
     * The directive identifier is a unique identifier that must correspond to
     * a [Rule.identifier].
     */
    public val identifier: String

    /**
     * The parameters of a directive are specified in YAML format within the
     * source file. This map collects all parameters, validation happens later
     * on.
     */
    public val parameters: Map<String, Any>

    /**
     * If the directive is subject to conditional execution, the conditions
     * have to be stored.
     */
    public val conditional: DirectiveConditional

    /**
     * A list contained all line numbers from the source file which built the
     * current directive.
     */
    public val lineNumbers: List<Int>

    /**
     * Execute the current directive, i.e. transform it into rules and
     * conditionals, resolve parameters and run the rule's command.
     *
     * @return The exit code of the rule's command.
     */
    public fun execute(): Int
}
