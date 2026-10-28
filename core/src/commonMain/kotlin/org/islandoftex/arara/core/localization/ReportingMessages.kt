// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for reporting messages. */
interface ReportingMessages {
    /** Project heading in styled output. */
    val infoLabelProject: String

    /** Heading above the per-project file summaries. */
    val infoDisplaySummaryTitle: String

    /** File column heading. */
    val infoDisplaySummaryFile: String

    /** Evaluated task count column heading. */
    val infoDisplaySummaryTasks: String

    /** Elapsed file processing time column heading. */
    val infoDisplaySummaryTime: String

    /** Hint shown only when a truncated output excerpt has a complete log. */
    val infoDisplayFullOutputInLog: String

    /** Overall verdict and counts for reached files and evaluated tasks. */
    fun infoDisplayTotalSuccess(files: Int, tasks: Int): String

    /** Overall failure and counts for reached files and evaluated tasks. */
    fun infoDisplayTotalFailure(files: Int, tasks: Int): String

    /** Simulation label, without claiming that commands succeeded. */
    fun infoDisplayDryRun(tasks: Int): String

    /** Summary label for files intentionally skipped by execution. */
    val infoConsoleSkipped: String

    /** Summary label for files left unreached after execution stopped. */
    val infoConsoleUnprocessed: String

    /**
     * The label shown when an exception carries details in the log.
     */
    val infoDisplayExceptionMoreDetails: String

    /**
     * The message shown with the total execution time.
     */
    fun infoDisplayExecutionTime(time: String): String

    /**
     * The message shown while a file is being processed.
     */
    fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String

    /** Heading of a processed file's tool diagnostics. */
    val infoDisplayDiagnosticsTitle: String

    /** Diagnostic table severity column. */
    val infoDisplayDiagnosticLevel: String

    /** Diagnostic table executable column. */
    val infoDisplayDiagnosticTool: String

    /** Diagnostic table message column. */
    val infoDisplayDiagnosticMessage: String

    /** Diagnostic table occurrence-count column. */
    val infoDisplayDiagnosticCount: String
}
