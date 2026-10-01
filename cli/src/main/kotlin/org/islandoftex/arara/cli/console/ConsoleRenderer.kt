// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional

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
