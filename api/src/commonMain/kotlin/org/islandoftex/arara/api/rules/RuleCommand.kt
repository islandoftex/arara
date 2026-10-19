// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/**
 * A command is the essence of a rule. It is the unit of execution and
 * represents a system call.
 */
public interface RuleCommand {
    /**
     * The command's name might be used to display a more descriptive string to
     * the user in the terminal.
     */
    public val name: String?

}

/** A command implemented directly by a Kotlin function. */
public interface ExecutableRuleCommand : RuleCommand {
    /** Executes the command and returns its exit status. */
    public val command: () -> Int
}
