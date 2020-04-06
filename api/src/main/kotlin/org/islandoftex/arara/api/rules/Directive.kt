// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/**
 * A directive is a pattern within a source file that tells arara which rules
 * to apply in order to process the source.
 */
interface Directive {
    /**
     * The directive identifier is a unique identifier that must correspond to
     * a [Rule.identifier].
     */
    val identifier: String
    /**
     * The parameters of a directive are specified in YAML format within the
     * source file. This map collects all parameters, validation happens later
     * on.
     */
    val parameters: Map<String, Any>
    /**
     * If the directive is subject to conditional execution, the conditions
     * have to be stored.
     */
    val conditional: DirectiveConditional
    /**
     * A list contained all line numbers from the source file which built the
     * current directive.
     */
    val lineNumbers: List<Int>
}
