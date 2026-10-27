// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import kotlin.time.Duration
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.diagnostics.ToolDiagnostic

/** Presentation data retained across project configuration and renderer changes. */
data class FileSummary(
    /** Source path retained for the final summary. */
    val path: MPPPath,
    /** Owning project name. */
    val project: String,
    /** Working directory of the owning project. */
    val directory: MPPPath,
    /** Null for files never reached; otherwise the executor or application error status. */
    val exitCode: Int? = null,
    /** File execution duration, or null if no duration was recorded. */
    val elapsed: Duration? = null,
    /** Evaluated command or boolean entries, including simulations. */
    val tasks: Int = 0,
    /** Directives skipped because their conditions did not hold. */
    val skipped: Int = 0,
    /** Whether this file was evaluated without executing system commands. */
    val dryRun: Boolean = false,
    /** Warnings and errors parsed from this file's tools, independently of diagnostic log filtering. */
    val diagnostics: List<ToolDiagnostic> = emptyList()
)

/**
 * A dry run describes evaluation, not successful execution of the commands.
 * @property success Whether the invocation completed without an error status.
 * @property dryRun Whether the invocation simulated system commands.
 * @property files Files in invocation order, including those never reached.
 */
data class RunSummary(val success: Boolean, val dryRun: Boolean, val files: List<FileSummary>)
