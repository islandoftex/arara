// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

import java.nio.file.Path
import org.islandoftex.arara.Arara
import org.islandoftex.arara.configuration.AraraSpec
import org.islandoftex.arara.session.Command
import org.islandoftex.arara.utils.CommonUtils

/**
 * Implements a command model, containing a list of strings.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class CommandImpl : Command {
    override val elements: List<String>
    override var workingDirectory: Path = Arara.config[AraraSpec.Execution
            .workingDirectory]

    /**
     * Constructor.
     * @param values An array of objects.
     */
    constructor(vararg values: Any) {
        elements = mutableListOf()
        val result = CommonUtils.flatten(values.toList())
        result.map { it.toString() }.filter { it.isNotEmpty() }
                .forEach { elements.add(it) }
    }

    /**
     * Constructor.
     * @param elements A list of strings.
     */
    constructor(elements: List<String>) {
        this.elements = elements
    }

    /**
     * Provides a textual representation of the current command.
     * @return A string representing the current command.
     */
    override fun toString(): String {
        return "[ " + elements.joinToString(", ") + " ]" +
                " @ $workingDirectory"
    }
}
