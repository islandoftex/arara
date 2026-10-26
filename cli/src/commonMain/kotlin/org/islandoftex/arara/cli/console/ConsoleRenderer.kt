// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.core.localization.LanguageController

/** Terminal presentation only; logging belongs to the display facade. */
@Suppress("TooManyFunctions")
interface ConsoleRenderer {
    /** Print the application logo and version. */
    fun printBanner()
    /** Begin presentation of a task within its rule. */
    fun printEntry(name: String, task: String)
    /** Finish the current task with a success or failure result. */
    fun printEntryResult(value: Boolean)
    /** Present an application error and optional cause details. */
    fun printException(text: String, details: String?)
    /** Print prose wrapped to the available terminal width. */
    fun printWrapped(text: String)
    /** Present the rule authors. */
    fun printAuthors(authors: List<String>)
    /** Present the directive conditional, including its expression. */
    fun printConditional(conditional: DirectiveConditional)
    /** Present elapsed time in seconds. */
    fun printTime(seconds: Double)
    /** Present the input path, size and modification time. */
    fun printFileInformation(file: ProjectFile)
    /** Build a separator for the current output width. */
    fun displaySeparator(): String
    /** Build an output-buffer separator containing the supplied message. */
    fun displayOutputSeparator(message: String): String

    /** Present a workspace operation before validation, staging or cleanup starts. */
    fun startBuildDirectory(message: String) = printWrapped(message)

    /** Retire workspace feedback and present its localized outcome. */
    fun finishBuildDirectory(message: String) = printWrapped(message)

    /** Suspend live feedback before another component prints or asks for input. */
    fun suspendProgress() { }

    /** Release presentation-owned resources; host streams remain open. */
    fun close() { }

    /** Legacy project presentation hook; the default emits nothing. */
    fun printProjectInformation(name: String, directory: String) { }

    /** Present project context, identifying multi-project runs; the default delegates to the legacy hook. */
    fun printProjectInformation(name: String, directory: String, multipleProjects: Boolean) =
        printProjectInformation(name, directory)

    /** Present complete run metadata; the default delegates to the legacy file-status summary. */
    fun printRunSummary(summary: RunSummary) =
        printRunSummary(summary.success, summary.files.map { it.path.toString() to it.exitCode })

    /** Present command, conditional and author fields; the default emits nothing. */
    fun printTaskDetails(command: String, conditional: DirectiveConditional, authors: List<String>) { }

    /** Present a simulated command; the default prints the localized dry-run message. */
    fun printDryRunCommand(command: String) =
        printWrapped(LanguageController.messages.infoInterpreterDryRunModeSystemCommand(command))

    /** Present a directive skipped by its conditional; the default emits nothing. */
    fun printSkippedDirective(identifier: String) { }

    /** Present a process launch or execution failure; the default emits nothing. */
    fun printCommandFailure(output: String) { }

    /** Legacy run summary hook; null file statuses identify files never reached. The default emits nothing. */
    fun printRunSummary(success: Boolean, files: List<Pair<String, Int?>>) { }
}
