// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements the terminal display facade: every call site renders
 * through this object, which routes each event to two homes - the
 * run log writes the shared diagnostic artifact, and the console
 * renderer resolved at startup prints the presentation. The two are
 * kept separate on purpose: whichever console a run used, the log
 * records the same lines. The historic plain renderer is the
 * default presentation, and the facade keeps the surface the
 * interpreter, the command runner and the tests were built on.
 *
 * @author Island of TeX
 */
@Suppress("TooManyFunctions")
object Display {
    /**
     * Displays the application banner in the terminal: the greeting
     * that opens every run.
     */
    fun printBanner() = renderer.printBanner()

    /**
     * The application logo for the terminal.
     */
    val logoString: String = """
              __ _ _ __ __ _ _ __ __ _
             / _` | '__/ _` | '__/ _` |
            | (_| | | | (_| | | | (_| |
             \__,_|_|  \__,_|_|  \__,_|
    """.trimIndent()

    /**
     * The console renderer the display delegates to: the plain
     * presentation by default, replaced when the application
     * resolves another one.
     */
    internal var renderer: ConsoleRenderer = PlainConsoleRenderer

    /**
     * The file name of the configuration file in use. It is only
     * needed for logging purposes.
     */
    var configurationFileName: String = "[none]"

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    fun printEntryResult(value: Boolean) {
        RunLog.taskResult(value)
        renderer.printEntryResult(value)
    }

    /**
     * Displays the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    fun printEntry(name: String, task: String) {
        RunLog.entry(name, task)
        renderer.printEntry(name, task)
    }

    /**
     * Displays the exception in the terminal.
     *
     * @param exception The exception object.
     */
    fun printException(exception: AraraException) {
        val underlying = exception.exception
        val details = underlying?.let { it.message ?: it.toString() }
        // the log carries the bare message, as thrown; the suffix
        // pointing at the details is console presentation, so only
        // the display keeps it
        RunLog.exception(exception.message, details)
        val text = (
            if (underlying != null)
                exception.message + " " + LanguageController.messages
                    .infoDisplayExceptionMoreDetails
            else
                exception.message
            ) ?: "EXCEPTION PROVIDES NO MESSAGE"
        renderer.printException(text, details)
    }

    /**
     * Displays the provided text wrapped nicely according to the
     * default terminal width.
     *
     * @param text The text to be displayed.
     */
    fun printWrapped(text: String) = renderer.printWrapped(text)

    /**
     * Displays the rule authors in the terminal.
     *
     * @param authors The list of authors.
     */
    fun printAuthors(authors: List<String>) = renderer.printAuthors(authors)

    /**
     * Displays the current conditional in the terminal.
     *
     * @param conditional The conditional object.
     */
    fun printConditional(conditional: DirectiveConditional) =
        renderer.printConditional(conditional)

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    fun printTime(seconds: Double) {
        RunLog.executionTime(seconds)
        renderer.printTime(seconds)
    }

    /**
     * Gets the output separator with the provided text.
     *
     * @param message The provided text.
     * @return A string containing the output separator with the
     * provided text.
     */
    fun displayOutputSeparator(message: String) =
        renderer.displayOutputSeparator(message)

    /**
     * Gets the line separator.
     *
     * @return A string containing the line separator.
     */
    fun displaySeparator() = renderer.displaySeparator()

    /**
     * Displays the file information in the terminal: the welcome
     * banner and the file line, with the log carrying an English
     * twin of the localized console line.
     *
     * @param file The project file.
     */
    fun printFileInformation(file: ProjectFile) {
        RunLog.welcome()
        RunLog.environment(configurationFileName)
        RunLog.fileInformation(file)
        renderer.printFileInformation(file)
    }

    /**
     * Displays the details of the task a verbose rich run shows:
     * the command it executes, its conditional and the rule authors,
     * framed for the tool output that follows. The historic plain
     * presentation prints none of these.
     *
     * @param command The command the task executes.
     * @param conditional The conditional of the directive.
     * @param authors The authors of the rule.
     */
    fun printTaskDetails(
        command: Command,
        conditional: DirectiveConditional,
        authors: List<String>
    ) = renderer.printTaskDetails(command, conditional, authors)

    /**
     * Displays the naming of a directive its conditional skipped.
     * The historic plain presentation leaves no console trace of a
     * skip - the log carries the record.
     *
     * @param identifier The identifier of the skipped directive.
     */
    fun printSkippedDirective(identifier: String) =
        renderer.printSkippedDirective(identifier)

    /**
     * Displays the closing summary of the run: the verdict and, for
     * the presentations that keep one, the table of the processed
     * files. The historic plain ending stays as it always was.
     *
     * @param success Whether the whole run succeeded.
     */
    fun printRunSummary(success: Boolean) = renderer.printRunSummary(success)
}
