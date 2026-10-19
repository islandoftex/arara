// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

/** An expression engine supplies command results and interprets process exit codes. */
interface CommandEvaluator {
    /** Return a boolean, a system command, or a nested list of these values. */
    fun evaluate(parameters: Map<String, Any>): Any?

    /** Called only after a system command runs, never in dry-run mode. */
    fun isSuccessful(exitCode: Int): Boolean
}
