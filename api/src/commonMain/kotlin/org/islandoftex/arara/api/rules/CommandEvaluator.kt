// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

/** An expression engine supplies command results and interprets process exit codes. */
public interface CommandEvaluator {
    /** Return a boolean, a system command, or a nested list of these values. */
    public fun evaluate(parameters: Map<String, Any>): Any?

    /** Called only after a system command runs, never in dry-run mode. */
    public fun isSuccessful(exitCode: Int): Boolean
}
