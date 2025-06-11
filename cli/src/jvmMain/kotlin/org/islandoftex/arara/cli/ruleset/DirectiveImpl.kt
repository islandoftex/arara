// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.cli.interpreter.Interpreter
import org.islandoftex.arara.core.session.LinearExecutor
import kotlin.time.ExperimentalTime

/**
 * Implements the directive model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
data class DirectiveImpl(
    override val identifier: String,
    override val parameters: Map<String, Any>,
    override val conditional: DirectiveConditional,
    override val lineNumbers: List<Int>,
) : Directive {
    @OptIn(ExperimentalTime::class)
    override fun execute(): Int =
        Interpreter(
            LinearExecutor.executionOptions,
            LinearExecutor.currentFile!!,
            LinearExecutor.currentProject!!.workingDirectory,
        ).execute(this).exitCode

    override fun toString(): String =
        "Directive(identifier='$identifier', parameters=$parameters," +
            "conditional=$conditional, lineNumbers=$lineNumbers)"
}
