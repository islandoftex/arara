// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain
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
    override fun printBanner() {
        // the banner opens every run, so the per-run state of this
        // shared object resets with it: the tallies the closing
        // verdict reads and the flags the separations consult
        files = 0
        tasks = 0
        displayLine = true
        displayResult = false
        displayRolling = false
        displayException = false
        displaySummary = false
        entryLineOpen = false
        landedOutcome = false
        println(Display.logoString + "\n")
    }

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

    /**
     * Whether the current entry line is still open for a result to
     * land on.
     */
    private var displayLine = true

    /**
     * Whether a result has landed on the current entry line, so
     * the exception block knows whether to break the line first.
     */
    private var displayResult = false

    /**
     * Whether an entry block has already printed in this file, so
     * the first entry of a file opens without a blank line above
     * it. The flag resets with every file.
     */
    private var displayRolling = false
    /**
     * Whether an exception has printed in the current run, so the
     * closing blocks know whether to separate from it.
     */
    private var displayException = false

    /**
     * Whether a compact entry left its line open for the outcome to
     * land on: the error block tells the word that lands like a
     * result from the section title by it.
     */
    private var entryLineOpen = false

    /**
     * Whether the closing verdict printed, so the time line knows
     * to separate from it the way it separates from an error.
     */
    private var displaySummary = false

    /**
     * Whether an outcome landed on an entry line and nothing has
     * separated it since: the closing verdict consults it when the
     * run failed - a silent failure, a halted run - so the verdict
     * never glues itself to the landed word.
     */
    private var landedOutcome = false

    /**
     * The number of files the run started, so the closing verdict
     * can name them. The banner resets the tally, because this
     * presentation is a single object shared by every run of the
     * same application instance.
     */
    private var files = 0

    /**
     * The number of entries the run printed, so the closing
     * verdict can count the tasks. The banner resets the tally.
     */
    private var tasks = 0

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
        // the compact line stays open: the outcome - a result or an
        // error word - lands on it
        entryLineOpen = true
    }

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    override fun printEntryResult(value: Boolean) {
        displayLine = false
        displayResult = true
        entryLineOpen = false

        if (!isDryRunMode) {
            println(
                if (!isVerboseMode) {
                    getResult(value).padStart(longestMatch)
                } else {
                    "\n" + (" " + getResult(value))
                        .padStart(outputWidth, '-')
                }
            )
            // an outcome landed on the entry line: the per-file
            // closing blank separates it when the file completes,
            // and the closing verdict when the run failed without
            // one - a silent failure, a halted run
            landedOutcome = true
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
        tasks++
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
     * Displays the exception in the terminal: an error striking an
     * open compact entry lands its word on the line like an
     * outcome, any other error titles its own section on the
     * dashed rule, and the message follows directly beneath it.
     *
     * @param text The message to be displayed.
     * @param details The underlying cause details, if any.
     */
    override fun printException(text: String, details: String?) {
        displayException = true

        if (displayResult)
            println()
        if (!isDryRunMode) {
            if (entryLineOpen) {
                // the entry line is still waiting for its outcome:
                // the word lands on it like a result, in the column
                // the results align into, and the completed line
                // separates from the message the way a landed
                // result does
                println(
                    LanguageController.messages.infoLabelOnError
                        .padStart(longestMatch)
                )
                entryLineOpen = false
                println()
            } else {
                // the word titles its section on the dashed rule
                // the other section titles of the presentation
                // carry, and the rule separates: the message follows
                // directly beneath, with no blank to detach the
                // title from its words
                println(
                    (LanguageController.messages.infoLabelOnError + " ")
                        .abbreviate(outputWidth)
                        .padEnd(outputWidth, '-')
                )
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
     * Displays the files that were requested but never reached, when
     * an exception stopped the run before their turn: the localized
     * label and the file names, with a blank separating it from
     * whatever came before - the time line's own blank provides the
     * separator below.
     *
     * @param files The names of the files that were never started.
     */
    override fun printUnprocessedFiles(files: List<String>) {
        println()
        println(
            LanguageController.messages
                .infoDisplayUnprocessedFiles(files.size) +
                files.joinToString(", ")
        )
    }

    /**
     * Displays the closing summary of the run: the plain
     * presentation keeps no table, but the run closes with the
     * verdict word naming what happened - the counts the verdict
     * speaks come from the renderer's own tallies, reset at the
     * banner - and an empty ledger stays silent, the way a run
     * that never started a file has nothing to summarize.
     *
     * @param success Whether the whole run succeeded.
     */
    override fun printRunSummary(success: Boolean) {
        if (files == 0) {
            return
        }
        displaySummary = true
        if (displayException || (!success && landedOutcome)) {
            println()
        }
        landedOutcome = false
        println(
            if (isDryRunMode) {
                LanguageController.messages.infoDisplayDryRun(tasks)
            } else if (success) {
                LanguageController.messages
                    .infoDisplayTotalSuccess(files, tasks)
            } else {
                LanguageController.messages
                    .infoDisplayTotalFailure(files, tasks)
            }
        )
    }

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    override fun printTime(seconds: Double) {
        if (displayLine || displayException || displaySummary)
            println()

        // the format follows the user's locale, its separator
        // included, and pads to the millisecond
        val text = LanguageController.messages.infoDisplayExecutionTime +
            " " + String.format(
                Session.userInterfaceOptions.locale.toJVMLocale(),
                "%.4f", seconds
            ) + " " +
            LanguageController.messages.infoDisplayTimeUnit
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
        // each file opens its own entry blocks, so the rolling
        // separator resets with every file - the first entry of a
        // file never carries a stale blank from the previous one -
        // and the outcome flags reset with it, so an error in the
        // new file separates from the header alone, not from a
        // blank the previous file's landed result left behind
        displayRolling = false
        displayResult = false
        displayException = false
        landedOutcome = false
        files++
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
