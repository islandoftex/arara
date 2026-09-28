// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.cli.interpreter.Interpreter
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Implements the directive model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
data class DirectiveImpl(
    /**
     * The identifier of the rule this directive asks for.
     */
    override val identifier: String,
    /**
     * The resolved parameters of the directive.
     */
    override val parameters: Map<String, Any>,
    /**
     * The conditional attached to the directive, if any.
     */
    override val conditional: DirectiveConditional,
    /**
     * The lines the directive was extracted from.
     */
    override val lineNumbers: List<Int>
) : Directive {
    /**
     * Interprets the directive through the interpreter and reports its
     * exit code.
     */
    override fun execute(): Int = Interpreter(
        LinearExecutor.executionOptions, LinearExecutor.currentFile!!,
        LinearExecutor.currentProject!!.workingDirectory
    ).execute(this).exitCode

    /**
     * A human-readable representation of the directive, for logs and
     * error reports.
     */
    override fun toString(): String {
        return "Directive(identifier='$identifier', parameters=$parameters," +
            "conditional=$conditional, lineNumbers=$lineNumbers)"
    }
}
