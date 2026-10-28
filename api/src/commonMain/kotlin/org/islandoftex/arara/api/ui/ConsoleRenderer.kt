// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.ui

import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.reporting.RunSummary
import org.islandoftex.arara.api.reporting.WatchSummary
import org.islandoftex.arara.api.rules.DirectiveConditional

/** Terminal presentation composed from focused output, task, summary and progress capabilities. */
public interface ConsoleRenderer : ConsoleText, ConsoleTasks, ConsoleTaskDetails, ConsoleProgress, ConsoleSummaries

/** Output capability of a terminal presentation. */
public interface ConsoleOutput {
    /** Print prose wrapped to the available terminal width. */
    public fun printWrapped(text: String)
}

/** Separators capability of a terminal presentation. */
public interface ConsoleSeparators {
    /** Build a separator for the current output width. */
    public fun displaySeparator(): String

    /** Build an output-buffer separator containing the supplied message. */
    public fun displayOutputSeparator(message: String): String
}

/** Text capability of a terminal presentation. */
public interface ConsoleText : ConsoleOutput, ConsoleSeparators {
    /** Print the application logo and version. */
    public fun printBanner()

    /** Present an application error and optional cause details. */
    public fun printException(text: String, details: String?)

    /** Present elapsed time in seconds. */
    public fun printTime(seconds: Double)

    /** Present the input path, size and modification time. */
    public fun printFileInformation(file: ProjectFile)
}

/** Tasks capability of a terminal presentation. */
public interface ConsoleTasks {
    /** Begin presentation of a task within its rule. */
    public fun printEntry(name: String, task: String)

    /** Finish the current task with a success or failure result. */
    public fun printEntryResult(value: Boolean)
}

/** TaskDetails capability of a terminal presentation. */
public interface ConsoleTaskDetails : ConsoleOutput {
    /** Present the rule authors. */
    public fun printAuthors(authors: List<String>)

    /** Present the directive conditional, including its expression. */
    public fun printConditional(conditional: DirectiveConditional)

    /** Present command, conditional and author fields; the default emits nothing. */
    public fun printTaskDetails(command: String, conditional: DirectiveConditional, authors: List<String>) { }

    /** Present a simulated command. */
    public fun printDryRunCommand(command: String)

    /** Present a directive skipped by its conditional; the default emits nothing. */
    public fun printSkippedDirective(identifier: String) { }

    /** Present a process launch or execution failure; the default emits nothing. */
    public fun printCommandFailure(output: String) { }
}

/** Progress capability of a terminal presentation. */
public interface ConsoleProgress : ConsoleOutput {
    /** Present a workspace operation before validation, staging or cleanup starts. */
    public fun startBuildDirectory(message: String): Unit = printWrapped(message)

    /** Retire workspace feedback and present its localized outcome. */
    public fun finishBuildDirectory(message: String): Unit = printWrapped(message)

    /** Suspend live feedback before another component prints or asks for input. */
    public fun suspendProgress() { }

    /** Release presentation-owned resources; host streams remain open. */
    public fun close() { }
}

/** Summaries capability of a terminal presentation. */
public interface ConsoleSummaries : ConsoleOutput {
    /** Legacy project presentation hook; the default emits nothing. */
    public fun printProjectInformation(name: String, directory: String) { }

    /** Present project context, identifying multi-project runs; the default delegates to the legacy hook. */
    public fun printProjectInformation(name: String, directory: String, multipleProjects: Boolean): Unit =
        printProjectInformation(name, directory)

    /** Present complete run metadata; the default delegates to the legacy file-status summary. */
    public fun printRunSummary(summary: RunSummary): Unit =
        printRunSummary(summary.success, summary.files.map { it.path.toString() to it.exitCode })

    /** Present completed watch cycles and the final graph result. */
    public fun printWatchSummary(summary: WatchSummary)

    /** Legacy run summary hook; null file statuses identify files never reached. The default emits nothing. */
    public fun printRunSummary(success: Boolean, files: List<Pair<String, Int?>>) { }
}
