// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.model.Interpreter

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
    override val lineNumbers: List<Int>
) : Directive {
    override fun execute(): Int {
        Interpreter.execute(this)
        return Arara.config[AraraSpec.Execution.exitCode]
    }
}
