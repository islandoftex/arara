// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

import org.islandoftex.arara.rules.DirectiveConditional

/**
 * Implements the directive model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
data class Directive(
    /**
     * The directive identifier, it is resolved to the rule identifier
     * later on.
     */
    val identifier: String,
    /**
     * A map containing the parameters; they are validated later on in
     * order to ensure they are valid.
     */
    val parameters: Map<String, Any>,
    /**
     * A conditional containing the type and the expression to be evaluated
     * later on.
     */
    val conditional: DirectiveConditional,
    /**
     * A list contained all line numbers from the main file which built the
     * current directive.
     */
    val lineNumbers: List<Int>
)
