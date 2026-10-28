// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for reporting messages. */
public interface ReportingMessages {
    /** Project heading in styled output. */
    public val infoLabelProject: String

    /** Heading above the per-project file summaries. */
    public val infoDisplaySummaryTitle: String

    /** File column heading. */
    public val infoDisplaySummaryFile: String

    /** Evaluated task count column heading. */
    public val infoDisplaySummaryTasks: String

    /** Elapsed file processing time column heading. */
    public val infoDisplaySummaryTime: String

    /** Hint shown only when a truncated output excerpt has a complete log. */
    public val infoDisplayFullOutputInLog: String

    /** Overall verdict and counts for reached files and evaluated tasks. */
    public fun infoDisplayTotalSuccess(files: Int, tasks: Int): String

    /** Overall failure and counts for reached files and evaluated tasks. */
    public fun infoDisplayTotalFailure(files: Int, tasks: Int): String

    /** Simulation label, without claiming that commands succeeded. */
    public fun infoDisplayDryRun(tasks: Int): String

    /** Summary label for files intentionally skipped by execution. */
    public val infoConsoleSkipped: String

    /** Summary label for files left unreached after execution stopped. */
    public val infoConsoleUnprocessed: String

    /**
     * The label shown when an exception carries details in the log.
     */
    public val infoDisplayExceptionMoreDetails: String

    /**
     * The message shown with the total execution time.
     */
    public fun infoDisplayExecutionTime(time: String): String

    /**
     * The message shown while a file is being processed.
     */
    public fun infoDisplayFileInformation(name: String, size: String, lastModified: String): String

    /** Heading of a processed file's tool diagnostics. */
    public val infoDisplayDiagnosticsTitle: String

    /** Diagnostic table severity column. */
    public val infoDisplayDiagnosticLevel: String

    /** Diagnostic table executable column. */
    public val infoDisplayDiagnosticTool: String

    /** Diagnostic table message column. */
    public val infoDisplayDiagnosticMessage: String

    /** Diagnostic table occurrence-count column. */
    public val infoDisplayDiagnosticCount: String
}
