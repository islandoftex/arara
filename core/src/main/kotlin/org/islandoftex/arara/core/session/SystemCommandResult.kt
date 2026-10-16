// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

/**
 * The result of a system command execution: either the command ran to
 * completion and holds its exit status and output, or the execution
 * failed and holds the underlying exception.
 */
sealed class SystemCommandResult {
    /**
     * The command ran to completion.
     *
     * @property exitCode The exit status of the command.
     * @property output The captured output of the command.
     */
    data class Completed(
        val exitCode: Int,
        val output: String
    ) : SystemCommandResult()

    /**
     * The command could not be executed.
     *
     * @property exception The underlying exception of the failure.
     * @property output The output captured up to the failure.
     */
    data class Failed(
        val exception: Exception,
        val output: String = ""
    ) : SystemCommandResult()
}
