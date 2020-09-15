// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Path
import org.islandoftex.arara.api.session.Command

/**
 * Implements a command model, containing a list of strings.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class CommandImpl(
    override val elements: List<String>
) : Command {
    override var workingDirectory: Path? = null

    /**
     * Provides a textual representation of the current command.
     * @return A string representing the current command.
     */
    override fun toString(): String {
        return elements.joinToString(", ", "[", "]") +
                " @ $workingDirectory"
    }
}
