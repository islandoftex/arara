// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

/**
 * Enumeration to indicate the current execution's status. This can be
 * used to signal in which state arara is, whether it is doing its work
 * or encountered an error and is cleaning up. See the enumeration values
 * for details.
 *
 * As arara's execution status correlates with what is considered a successful
 * execution, this enumeration also determines arara's exit code.
 */
public sealed class ExecutionStatus(
    /**
     * The exit code arara will use when in the given state.
     */
    public val exitCode: Int,
) {
    /**
     * Everything went just fine (note that the
     * [org.islandoftex.arara.api.configuration.ExecutionMode.DRY_RUN] mode
     * always makes arara exit with 0, unless it is an error in the directive
     * builder itself).
     */
    public class Processing : ExecutionStatus(0)

    /**
     * One of the tasks failed, so the execution ended abruptly. This means the
     * error relies on the command line call, not with arara.
     */
    public class ExternalCallFailed : ExecutionStatus(1)

    /**
     * arara just handled an exception, meaning that something bad just
     * happened and might require user intervention.
     */
    public class CaughtException : ExecutionStatus(2)

    /**
     * This represents any case where a user defined code has to be handled
     * which shall not be represented by one of arara's default categories.
     */
    public class FinishedWithCode(
        exitCode: Int,
    ) : ExecutionStatus(exitCode)
}
