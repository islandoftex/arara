// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.session.Command

/**
 * Defines the contract of a console renderer: the whole run
 * lifecycle a presentation prints - the entries and their results,
 * the file information, the messages, the errors and the closing
 * blocks. The display facade keeps every call site stable and owns
 * the log entries, so a renderer is pure presentation: it prints
 * and holds no logging, and the application resolves the renderer
 * once at startup without touching the interpreter, the command
 * runner or the command line.
 *
 * @author Island of TeX
 */
@Suppress("TooManyFunctions")
interface ConsoleRenderer {
    /**
     * Displays the application banner in the terminal: the greeting
     * that opens every run.
     */
    fun printBanner()

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    fun printEntryResult(value: Boolean)

    /**
     * Displays the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task Task name.
     */
    fun printEntry(name: String, task: String)

    /**
     * Displays the exception in the terminal.
     *
     * @param text The message to be displayed.
     * @param details The underlying cause details, if any.
     */
    fun printException(text: String, details: String?)

    /**
     * Displays the provided text wrapped nicely according to the
     * default terminal width.
     *
     * @param text The text to be displayed.
     */
    fun printWrapped(text: String)

    /**
     * Displays the rule authors in the terminal.
     *
     * @param authors The list of authors.
     */
    fun printAuthors(authors: List<String>)

    /**
     * Displays the current conditional in the terminal.
     *
     * @param conditional The conditional object.
     */
    fun printConditional(conditional: DirectiveConditional)

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    fun printTime(seconds: Double)

    /**
     * Gets the output separator with the provided text.
     *
     * @param message The provided text.
     * @return A string containing the output separator with the
     * provided text.
     */
    fun displayOutputSeparator(message: String): String

    /**
     * Gets the line separator.
     *
     * @return A string containing the line separator.
     */
    fun displaySeparator(): String

    /**
     * Displays the file information in the terminal: the localized
     * file line of the run.
     *
     * @param file The project file.
     */
    fun printFileInformation(file: ProjectFile)

    /**
     * Displays the project information in the terminal: the
     * structural line that opens a project's territory in a
     * multi-project run. Single-project runs never call it, so
     * the historic silhouette keeps its shape.
     *
     * @param project The project.
     */
    fun printProjectInformation(project: Project)

    /**
     * Displays the command a dry run is about to run. The historic
     * plain presentation renders the localized sentence, so its
     * override carries the bytes and the default here is a
     * uniformity guarantee for presentations that own no words of
     * their own, rather than a load-bearing silence.
     *
     * @param command The command the dry run simulates.
     */
    fun printDryRunCommand(command: Command) {
    }

    /**
     * Displays the details of the task a verbose styled run shows:
     * the command it executes, its conditional and the rule authors,
     * framed for the tool output that follows. The historic plain
     * presentation prints none of these, so the default says
     * nothing.
     *
     * @param command The command the task executes.
     * @param conditional The conditional of the directive.
     * @param authors The authors of the rule.
     */
    fun printTaskDetails(
        command: Command,
        conditional: DirectiveConditional,
        authors: List<String>
    ) {
    }

    /**
     * Displays the naming of a directive its conditional skipped.
     * The historic plain presentation leaves no console trace of a
     * skip - the log carries the record - so the default says
     * nothing.
     *
     * @param identifier The identifier of the skipped directive.
     */
    fun printSkippedDirective(identifier: String) {
    }

    /**
     * Displays the tail of the output a failed command captured, so
     * the tool's own explanation of its failure sits beside the
     * verdict. A verbose run already streamed the output live, and
     * the historic plain presentation keeps its silence, so the
     * default says nothing.
     *
     * @param output The captured output of the failed command.
     */
    fun printCommandFailure(output: String) {
    }

    /**
     * Displays the files that were requested but never reached,
     * when an exception stopped the run before their turn. The
     * line follows the closing summary, naming what the run left
     * undone.
     *
     * @param files The names of the files that were never started.
     */
    fun printUnprocessedFiles(files: List<String>)

    /**
     * Displays the closing summary of the run: the verdict and, for
     * the presentations that keep one, the table of the processed
     * files. The historic plain ending stays as it always was, so
     * the default says nothing.
     *
     * @param success Whether the whole run succeeded.
     */
    fun printRunSummary(success: Boolean) {
    }
}
