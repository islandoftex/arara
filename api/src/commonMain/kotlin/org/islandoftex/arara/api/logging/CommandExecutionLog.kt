// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.logging

import org.islandoftex.arara.api.session.Command

/** Diagnostic events for tool output, completion and process failures. */
public interface CommandExecutionLog {
    /** Record an explicitly supplied raw output buffer at trace level. */
    public fun commandOutput(output: String)

    /** Record the command exit code and duration in milliseconds. */
    public fun commandCompleted(command: Command, exitCode: Int, durationMs: Long)

    /** Record a backend failure category, duration in milliseconds and original cause. */
    public fun commandFailed(command: Command, kind: String, durationMs: Long, cause: Throwable)
}
