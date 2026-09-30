// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer

/**
 * Implements the terminal display facade: every call site renders
 * through this object, which delegates to the console renderer the
 * application resolved at startup. The historic plain renderer is
 * the default presentation, and the facade keeps the surface the
 * interpreter, the command runner and the tests were built on.
 *
 * @author Island of TeX
 */
@Suppress("TooManyFunctions")
object Display {
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
    var configurationFileName: String
        get() = renderer.configurationFileName
        set(value) {
            renderer.configurationFileName = value
        }

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    fun printEntryResult(value: Boolean) = renderer.printEntryResult(value)

    /**
     * Displays the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    fun printEntry(name: String, task: String) =
        renderer.printEntry(name, task)

    /**
     * Displays the exception in the terminal.
     *
     * @param exception The exception object.
     */
    fun printException(exception: AraraException) =
        renderer.printException(exception)

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
     * Gets a human readable representation of a size.
     *
     * @param size The byte size to be converted.
     * @return A string representation of the size.
     */
    internal fun byteSizeToString(size: Long) =
        renderer.byteSizeToString(size)

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    fun printTime(seconds: Double) = renderer.printTime(seconds)

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
    fun printFileInformation(file: ProjectFile) =
        renderer.printFileInformation(file)
}
