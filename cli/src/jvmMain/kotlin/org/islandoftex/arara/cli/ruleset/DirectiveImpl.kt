// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.RuleEngine
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
    /** Rule identifier requested by the directive. */
    override val identifier: String,
    /** Raw directive parameters supplied to the resolved rule engine. */
    override val parameters: Map<String, Any>,
    /** Condition controlling directive scheduling. */
    override val conditional: DirectiveConditional,
    /** One-based source lines occupied by this directive. */
    override val lineNumbers: List<Int>,
    /** Preferred rule engine; lookup can resolve the other format. */
    val engine: RuleEngine = RuleEngine.YAML
) : Directive {
    /** Execute using the current linear-executor file, project and options, returning the final status. */
    override fun execute(): Int = Interpreter(
        LinearExecutor.executionOptions, LinearExecutor.currentFile!!,
        LinearExecutor.currentProject!!.executionDirectory, engine
    ).execute(this).exitCode

    /** Describe the directive and its source metadata for diagnostics. */
    override fun toString(): String {
        return "Directive(identifier='$identifier', parameters=$parameters," +
            "conditional=$conditional, lineNumbers=$lineNumbers)"
    }
}
