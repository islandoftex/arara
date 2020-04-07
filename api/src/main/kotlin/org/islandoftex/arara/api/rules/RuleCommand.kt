// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/**
 * A command is the essence of a rule. It is the unit of execution and
 * represents a system call.
 */
interface RuleCommand {
    /**
     * The command's name might be used to display a more descriptive string to
     * the user in the terminal.
     */
    val name: String?

    /**
     * The command itself is implemented using a lambda which returns an
     * integer. This integer is treated as exit code. Use it like you would use
     * an exit code on the command line (i.e. we treat only 0 as success).
     */
    val command: () -> Int
}
