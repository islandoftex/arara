// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.ui

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.ProjectFile

/** File accounting, invocation summaries and application diagnostics. */
public interface DisplayRunActions {
    /** Finish the current file in the run ledger; the final summary renders its status. */
    public fun printFileResult(exitCode: Int)

    /** Present the retained invocation ledger with the aggregate success flag. */
    public fun printRunSummary(success: Boolean)

    /** Retain compatibility warnings across files and project configuration changes. */
    public fun deprecatedYamlUsed()

    /** Count a conditional skip and present it through the active renderer. */
    public fun printSkippedDirective(identifier: String)

    /** Start file accounting and present its metadata. */
    public fun printFileInformation(file: ProjectFile)

    /** Record an application error, finish file accounting with status 2 and present cause details. */
    public fun printException(exception: AraraException)

    /** Log and present elapsed time in seconds. */
    public fun printTime(seconds: Double)
}
