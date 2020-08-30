// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Path
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.Arara
import org.islandoftex.arara.core.ui.InputHandling

/**
 * Implements a command model, containing a list of strings.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class CommandImpl(values: List<Any>) : Command {
    override val elements: List<String> = InputHandling.flatten(values.toList())
            .map { it.toString() }.filter { it.isNotEmpty() }
    override var workingDirectory: Path = Arara.currentProject.workingDirectory

    /**
     * Provides a textual representation of the current command.
     * @return A string representing the current command.
     */
    override fun toString(): String {
        return elements.joinToString(", ", "[", "]") +
                " @ $workingDirectory"
    }
}
