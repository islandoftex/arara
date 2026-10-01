// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.utils.Display
import org.islandoftex.arara.cli.utils.abbreviate
import org.islandoftex.arara.cli.utils.byteSizeToString
import org.islandoftex.arara.cli.utils.center
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.utils.wrap

/**
 * Implements the historic console renderer: the layout arara has
 * printed since its first release, kept deliberately frozen behind
 * the renderer contract. The entries, the dotted leaders, the
 * labels, the separators and the wrapping are the exact output
 * every existing workflow and expectation was built on, so this
 * presentation is moved, not modernized. It holds no logging: the
 * log entries of a run live in the run log, so no presentation can
 * ever change what the log records.
 *
 * @author Island of TeX
 */
@Suppress("TooManyFunctions")
object PlainConsoleRenderer : ConsoleRenderer {
    /**
     * Displays the application banner in the terminal: the logo and
     * the blank line that has opened every run.
     */
    override fun printBanner() = println(Display.logoString + "\n")

    /**
     * The length of the longest result match as integer.
     */
    private val longestMatch: Int by lazy {
        // lazy computation to wait for the language to be initialized
        listOf(
            LanguageController.messages.infoLabelOnSuccess,
            LanguageController.messages.infoLabelOnFailure,
            LanguageController.messages.infoLabelOnError
        ).map { it.length }.maxOrNull()!!
    }

    /**
     * If the longest match is longer than the width, then it will be
     * truncated to this length.
     */
    private const val shortenedLongestMatch = 10

    /**
     * The minimum entry width a degenerate terminal width degrades
     * to, so the abbreviation never runs out of space and the render
     * degrades instead of crashing.
     */
    private const val minimumEntryWidth = 6

    private var displayLine = true
    private var displayResult = false
    private var displayRolling = false
    private var displayException = false

    /**
     * Checks if the execution is in dry-run mode.
     */
    private val isDryRunMode: Boolean
        get() = LinearExecutor.executionOptions.executionMode ==
            ExecutionMode.DRY_RUN

    /**
     * Checks if the execution is in verbose mode.
     */
    private val isVerboseMode: Boolean
        get() = LinearExecutor.executionOptions.verbose

    /**
     * The number of columns available for output.
     */
    private val outputWidth: Int
        get() = Session.userInterfaceOptions.terminalOutputWidth

    /**
     * Displays the short version of the current entry in the terminal.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildShortEntry(name: String, task: String) {
        val result = if (longestMatch >= outputWidth)
            shortenedLongestMatch
        else
            longestMatch
        // a degenerate width has to degrade to an ugly entry, never
        // crash the render: the abbreviation keeps its minimum and
        // the padding simply wraps
        val space = (outputWidth - result - 1)
            .coerceAtLeast(minimumEntryWidth)
        val line = "($name) $task ".abbreviate(space - "... ".length)
        print(line.padEnd(space, '.') + " ")
    }

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    override fun printEntryResult(value: Boolean) {
        displayLine = false
        displayResult = true

        if (!isDryRunMode) {
            println(
                if (!isVerboseMode) {
                    getResult(value).padStart(longestMatch)
                } else {
                    "\n" + (" " + getResult(value))
                        .padStart(outputWidth, '-')
                }
            )
        }
    }

    /**
     * Displays the command a dry run is about to run: the
     * localized sentence, exactly as the plain console has always
     * rendered it.
     *
     * @param command The command the dry run simulates.
     */
    override fun printDryRunCommand(command: Command) =
        printWrapped(
            LanguageController.messages
                .infoInterpreterDryRunModeSystemCommand(
                    command.toString()
                )
        )

    /**
     * Displays the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    override fun printEntry(name: String, task: String) {
        displayLine = true
        displayResult = false
        if (!isDryRunMode) {
            if (!isVerboseMode) {
                buildShortEntry(name, task)
            } else {
                buildLongEntry(name, task)
            }
        } else {
            buildDryRunEntry(name, task)
        }
    }

    /**
     * Displays a long version of the current entry in the terminal.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildLongEntry(name: String, task: String) {
        if (displayRolling) {
            println()
        } else {
            displayRolling = true
        }
        println(displaySeparator())
        // the abbreviation keeps its minimum so a degenerate width
        // wraps ugly instead of crashing the render
        println(
            "($name) $task"
                .abbreviate(outputWidth.coerceAtLeast(minimumEntryWidth))
        )
        println(displaySeparator())
    }

    /**
     * Displays a dry-run version of the current entry in the terminal.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildDryRunEntry(name: String, task: String) {
        if (displayRolling) {
            println()
        } else {
            displayRolling = true
        }
        println("[DR] ($name) $task".abbreviate(outputWidth))
        println(displaySeparator())
    }

    /**
     * Displays the exception in the terminal.
     *
     * @param text The message to be displayed.
     * @param details The underlying cause details, if any.
     */
    override fun printException(text: String, details: String?) {
        displayException = true

        if (displayResult)
            println()
        if (displayLine) {
            if (!isDryRunMode) {
                println(
                    if (!isVerboseMode) {
                        LanguageController.messages.infoLabelOnError
                            .padStart(longestMatch)
                    } else {
                        (" " + LanguageController.messages.infoLabelOnError)
                            .padStart(outputWidth, '-')
                    }
                )
                println()
            }
        }
        printWrapped(text)
        // a present cause earns a details section even when it carries no
        // message of its own
        if (details != null) {
            println()
            displayDetailsLine()
            printWrapped(details)
        }
    }

    /**
     * Gets the string representation of the provided boolean value.
     *
     * @param value The boolean value.
     * @return The string representation.
     */
    private fun getResult(value: Boolean): String {
        return if (value)
            LanguageController.messages.infoLabelOnSuccess
        else
            LanguageController.messages.infoLabelOnFailure
    }

    /**
     * Displays the provided text wrapped nicely according to the
     * default terminal width.
     *
     * @param text The text to be displayed.
     */
    override fun printWrapped(text: String) = println(text.wrap(outputWidth))

    /**
     * Displays the rule authors in the terminal.
     *
     * @param authors The list of authors.
     */
    override fun printAuthors(authors: List<String>) {
        val line = if (authors.size == 1)
            LanguageController.messages.infoLabelAuthor
        else
            LanguageController.messages.infoLabelAuthors
        val text = if (authors.isEmpty())
            LanguageController.messages.infoLabelNoAuthors
        else
            authors.joinToString(", ") { it.trim() }
        printWrapped("$line $text")
    }

    /**
     * Displays the current conditional in the terminal.
     *
     * @param conditional The conditional object.
     */
    override fun printConditional(conditional: DirectiveConditional) {
        if (conditional.type !== DirectiveConditionalType.NONE) {
            printWrapped(
                LanguageController.messages.infoLabelConditional +
                    " (" + conditional.type + ") " +
                    conditional.condition
            )
        }
    }

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    override fun printTime(seconds: Double) {
        if (displayLine || displayException)
            println()

        val secondDuration = seconds.seconds
        val separator = Session.userInterfaceOptions.locale
            .decimalSeparator.toString()
        val time = secondDuration.inWholeSeconds.toString() + separator +
            (secondDuration - secondDuration.inWholeSeconds.seconds)
                .inWholeMilliseconds.toString()
        val text = LanguageController.messages.infoDisplayExecutionTime(time)
        printWrapped(text)
    }

    /**
     * Displays a line containing details.
     */
    private fun displayDetailsLine() = println(
        (LanguageController.messages.infoLabelOnDetails + " ")
            .abbreviate(outputWidth)
            .padEnd(outputWidth, '-')
    )

    /**
     * Gets the output separator with the provided text.
     *
     * @param message The provided text.
     * @return A string containing the output separator with the
     * provided text.
     */
    override fun displayOutputSeparator(message: String): String =
        " $message ".center(outputWidth, '-')

    /**
     * Gets the line separator.
     *
     * @return A string containing the line separator.
     */
    override fun displaySeparator(): String = "-".repeat(outputWidth)

    /**
     * Displays the file information in the terminal: the localized
     * file line of the run.
     *
     * @param file The project file.
     */
    override fun printFileInformation(file: ProjectFile) {
        LanguageController.messages
            .infoDisplayFileInformation(
                file.path.fileName,
                byteSizeToString(file.path.fileSize),
                file.path.lastModified.toString()
            ).let {
                printWrapped(it)
                println()
            }
    }
}
