// SPDX-License-Identifier: BSD-3-Clause

package org.islandoftex.arara.ruleset

/**
 * Implements the directive model.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
data class Directive(
        // the directive identifier, it is resolved 
        // to the rule identifier later on
        val identifier: String,
        // a map containing the parameters; they
        // are validated later on in order to
        // ensure they are valid
        val parameters: Map<String, Any>,
        // a conditional containing the type and
        // the expression to be evaluated later on
        val conditional: Conditional,
        // a list contained all line numbers from
        // the main file which built the current
        // directive
        val lineNumbers: List<Int>)
