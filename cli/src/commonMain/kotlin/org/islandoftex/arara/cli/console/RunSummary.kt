// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import org.islandoftex.arara.api.files.MPPPath
import kotlin.time.Duration

/** Presentation data retained across project configuration and renderer changes. */
data class FileSummary(
    val path: MPPPath,
    val project: String,
    val directory: MPPPath,
    /** Null for files never reached; otherwise the executor or application error status. */
    val exitCode: Int? = null,
    val elapsed: Duration? = null,
    /** Evaluated command or boolean entries, including simulations. */
    val tasks: Int = 0,
    val skipped: Int = 0,
    val dryRun: Boolean = false
)

/** A dry run describes evaluation, not successful execution of the commands. */
data class RunSummary(val success: Boolean, val dryRun: Boolean, val files: List<FileSummary>)
