// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Command

/**
 * Implements a command model, containing a list of strings.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
data class Command(
    /**
     * The command and its arguments, as separate elements.
     */
    override val elements: List<String>,
    /**
     * The directory the command runs in, if different from the
     * working directory of the project.
     */
    override val workingDirectory: MPPPath? = null
) : Command {
    /**
     * Provides a textual representation of the current command.
     * @return A string representing the current command.
     */
    override fun toString(): String {
        return elements.joinToString(", ", "[", "]") +
            " @ $workingDirectory"
    }
}
