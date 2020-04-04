// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.session

import java.nio.file.Path

/**
 * A command is a representation of a call to an external program.
 */
interface Command {
    /**
     * A list of elements which are components of a command. The command-line
     * call will join all these elements with spaces.
     */
    val elements: List<String>

    /**
     * Commands are intended to be executed within the project's working
     * directory. If this property is non-null, the working directory will be
     * overwritten. Use with care.
     */
    val workingDirectory: Path?
}
