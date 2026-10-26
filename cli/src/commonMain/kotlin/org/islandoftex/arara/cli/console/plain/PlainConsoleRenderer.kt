// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.console.RunSummary
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.abbreviate
import org.islandoftex.arara.cli.utils.center
import org.islandoftex.arara.cli.utils.wrap
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/**
 * Preserves the classic terminal layout.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Suppress("TooManyFunctions")
class PlainConsoleRenderer : ConsoleRenderer {

    /**
     * The length of the longest result match as integer.
     */
    private val longestMatch: Int
        get() = maxOf(
            LanguageController.messages.infoLabelOnSuccess.length,
            LanguageController.messages.infoLabelOnFailure.length,
            LanguageController.messages.infoLabelOnError.length
        )

    private val shortenedLongestMatch = SHORT_LABEL_WIDTH

    private var displayLine = false
    private var displayResult = false
    private var displayRolling = false
    private var displayException = false
    private var displaySummary = false

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
        val space = outputWidth - result - 1
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
        println(
            "($name) $task"
                .abbreviate(outputWidth)
        )
        println(displaySeparator())
    }

    /**
     * Displays a dry-run version of the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
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
     * @param text The localized message.
     * @param details The underlying failure, if present.
     */
    override fun printException(text: String, details: String?) {
        displayException = true

        if (displayResult)
            println()
        if (displayLine) {
            if (!isDryRunMode) {
                println(
                    if (!isVerboseMode) {
                        LanguageController.messages.infoLabelOnError.padStart(longestMatch)
                    } else {
                        (" " + LanguageController.messages.infoLabelOnError)
                            .padStart(outputWidth, '-')
                    }
                )
                println()
            }
        }
        printWrapped(text)
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
     * Displays the provided text wrapped nicely according to the default
     * terminal width.
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
        if (displayLine || displayException || displaySummary)
            println()

        val text = LanguageController.messages.infoDisplayExecutionTime(DisplayUtils.elapsedSeconds(seconds))
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
     * @return A string containing the output separator with the provided text.
     */
    override fun displayOutputSeparator(message: String): String =
        " $message ".center(outputWidth, '-')

    /**
     * Gets the line separator.
     *
     * @return A string containing the line separator.
     */
    override fun displaySeparator(): String = "-".repeat(outputWidth)
    override fun printBanner() {
        displayLine = false
        displayResult = false
        displayRolling = false
        displayException = false
        displaySummary = false
        println("${DisplayUtils.logoString}\n")
        printWrapped(DisplayUtils.versionLine)
        println()
    }

    override fun printRunSummary(summary: RunSummary) {
        if (summary.files.isEmpty()) return
        val messages = LanguageController.messages
        val tasks = summary.files.sumOf { it.tasks }
        val reached = summary.files.count { it.exitCode != null }
        if (displayException) println()
        printWrapped(when {
            summary.dryRun -> messages.infoDisplayDryRun(tasks)
            summary.success -> messages.infoDisplayTotalSuccess(reached, tasks)
            else -> messages.infoDisplayTotalFailure(reached, tasks)
        })
        if (summary.dryRun && !summary.success) printWrapped(messages.infoLabelOnFailure)
        val pending = summary.files.filter { it.exitCode == null }
        if (pending.isNotEmpty()) {
            printWrapped("${messages.infoConsoleUnprocessed}: ${pending.joinToString(", ") { it.path.toString() }}")
        }
        displaySummary = true
    }

    override fun printFileInformation(file: ProjectFile) {
        printWrapped(LanguageController.messages.infoDisplayFileInformation(
            file.path.fileName, DisplayUtils.byteSizeToString(file.sourcePath.fileSize),
            file.sourcePath.lastModified.toString()
        ))
        println()
    }

    override fun printProjectInformation(name: String, directory: String, multipleProjects: Boolean) {
        if (multipleProjects) {
            val project = if (name.isBlank()) directory else "$name ($directory)"
            printWrapped("${LanguageController.messages.infoLabelProject} $project")
            println()
        }
    }

    private companion object {
        const val SHORT_LABEL_WIDTH = 10
    }
}
