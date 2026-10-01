// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.rich

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraAPI
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
 * Implements the rich console renderer: the modern presentation of
 * the run, built on the terminal library the help output already
 * renders through. The structural elements - the rules, the leaders,
 * the alignment - extend to the live terminal width, while prose
 * wraps at a readable measure; the glyphs and the colors ride the
 * detected capability, so a piped stream keeps the structure with
 * plain characters and no escapes. The renderer holds no logging:
 * the run log writes the diagnostic artifact, so the presentation
 * can never change what the log records.
 *
 * @author Island of TeX
 */
@Suppress("TooManyFunctions")
class RichConsoleRenderer(private val terminal: Terminal) : ConsoleRenderer {
    /**
     * The length of the longest result match as integer, reserving
     * the space the aligned results need.
     */
    private val longestMatch: Int by lazy {
        // lazy computation to wait for the language to be initialized
        listOf(
            LanguageController.messages.infoLabelOnSuccess,
            LanguageController.messages.infoLabelOnFailure,
            LanguageController.messages.infoLabelOnError
        ).map { it.length }.maxOrNull()!!
    }

    private var displayLine = true
    private var displayResult = false
    private var displayRolling = false
    private var displayException = false

    /**
     * The entry of a file in the run ledger the closing summary
     * renders.
     */
    private class SummaryEntry(
        val name: String,
        val path: String,
        var tasks: Int,
        var seconds: Double,
        var success: Boolean
    )

    /**
     * The files the run processed, in order.
     */
    private val summary = mutableListOf<SummaryEntry>()

    /**
     * The file whose entries are currently printing.
     */
    private var currentFile: SummaryEntry? = null

    /**
     * The monotonic mark of the current file's start.
     */
    private var currentFileStart = TimeSource.Monotonic.markNow()

    /**
     * Whether the details of the current task printed, so the result
     * knows to close the output frame and carry the duration.
     */
    private var detailsPrinted = false

    /**
     * The monotonic mark of the current task's start, taken when its
     * details print and the command is about to run.
     */
    private var taskStart = TimeSource.Monotonic.markNow()

    /**
     * The width of the label column of the verbose task fields.
     */
    private val fieldGutterWidth = RichTheme.fieldGutterWidth

    private companion object {
        /** The column indexes of the execution summary table. */
        const val COUNT_COLUMN = 1
        const val TIME_COLUMN = 2
        const val STATUS_COLUMN = 3
    }

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
     * Whether the terminal carries the colors and the Unicode
     * glyphs; a plain stream keeps every character in the ASCII
     * fallbacks.
     */
    private val isDecorated: Boolean =
        terminal.terminalInfo.ansiLevel != AnsiLevel.NONE

    /**
     * The rule character of the section frames, following the
     * capability.
     */
    private val rule: String
        get() = if (isDecorated) RichTheme.ruleGlyph else RichTheme.ruleFallback

    /**
     * The dotted leader between an entry and its result, following
     * the capability.
     */
    private val leader: String
        get() = if (isDecorated) RichTheme.leaderGlyph else RichTheme.leaderFallback

    /**
     * The width every structural element extends to: the live
     * terminal width.
     */
    private val structureWidth: Int
        get() = terminal.size.width

    /**
     * The width prose wraps at: the readable measure, never past
     * the terminal.
     */
    private val proseWidth: Int
        get() = structureWidth.coerceAtMost(RichTheme.proseMaximumWidth)

    /**
     * The muted style of the chrome: the rules, the leaders and the
     * metadata.
     */
    private val muted get() = terminal.theme.muted

    /**
     * Displays the application banner in the terminal: the tinted
     * logo and the version line that names the run.
     */
    override fun printBanner() {
        terminal.println(RichTheme.logo(Display.logoString))
        terminal.println(muted("arara ${AraraAPI.version}"))
        terminal.println("")
    }

    /**
     * Displays the file information in the terminal: the section
     * rule that names the file, with its name linked to the file
     * itself and the size and modification date as the metadata.
     *
     * @param file The project file.
     */
    override fun printFileInformation(file: ProjectFile) {
        closeCurrentFile()
        currentFile = SummaryEntry(
            file.path.fileName,
            file.path.toString(),
            0,
            0.0,
            true
        )
        currentFileStart = TimeSource.Monotonic.markNow()
        val name = file.path.fileName
        val metadata = byteSizeToString(file.path.fileSize) +
            " · " + file.path.lastModified
        val title = hyperlink(name, file.path.toString())
        val head = "$rule$rule $name "
        val tail = " $metadata $rule$rule"
        val fill = (
            structureWidth - head.length - tail.length
            ).coerceAtLeast(1)
        terminal.println(
            muted("$rule$rule ") + title + muted(
                " " + rule.repeat(fill) + " "
            ) + muted(metadata) + muted(" $rule$rule")
        )
        terminal.println("")
    }

    /**
     * Displays the current entry in the terminal: the running glyph,
     * the rule and the task, with the leader reserving the space the
     * result aligns into.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    override fun printEntry(name: String, task: String) {
        displayLine = true
        displayResult = false
        currentFile?.let { it.tasks++ }
        if (isDryRunMode) {
            buildDryRunEntry(name, task)
        } else if (isVerboseMode) {
            buildLongEntry(name, task)
        } else {
            buildShortEntry(name, task)
        }
    }

    /**
     * Displays the short version of the current entry in the
     * terminal.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildShortEntry(name: String, task: String) {
        val head = "${runningGlyph()} $name · $task "
        // the leader reserves exactly the space the glyph, its
        // spacing and the longest result word need, so the aligned
        // result lands at the terminal edge
        val reserved = successGlyph().length + 1 + longestMatch
        val fill = (structureWidth - head.length - reserved)
            .coerceAtLeast(1)
        terminal.print(head + muted(leader.repeat(fill)) + " ")
    }

    /**
     * Displays a long version of the current entry in the terminal:
     * the framed block of the verbose run.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildLongEntry(name: String, task: String) {
        if (displayRolling) {
            terminal.println("")
        } else {
            displayRolling = true
        }
        terminal.println(muted(displaySeparator()))
        terminal.println("${runningGlyph()} $name · $task")
        terminal.println(muted(displaySeparator()))
    }

    /**
     * Displays a dry-run version of the current entry in the
     * terminal: the marked block of the simulated run.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildDryRunEntry(name: String, task: String) {
        if (displayRolling) {
            terminal.println("")
        } else {
            displayRolling = true
        }
        terminal.println(muted("[DR] ${runningGlyph()} $name · $task"))
        terminal.println(muted(displaySeparator()))
    }

    /**
     * Displays the current entry result in the terminal: the glyph
     * and the word, aligned into the reserved space and colored by
     * the outcome.
     *
     * @param value The boolean value to be displayed.
     */
    override fun printEntryResult(value: Boolean) {
        displayLine = false
        displayResult = true
        currentFile?.let { it.success = it.success && value }

        if (!isDryRunMode) {
            if (!isVerboseMode) {
                // the reserved space keeps the word aligned even when
                // the label of another language runs longer
                terminal.print(
                    resultWord(value)
                        .padStart(successGlyph().length + 1 + longestMatch)
                )
            } else {
                terminal.println("")
                if (detailsPrinted) {
                    // the framed tool output ends where the result
                    // lands, and the duration of the task rides the
                    // word
                    terminal.println(muted(frameRule()))
                    val elapsed = taskStart.elapsedNow()
                        .toDouble(DurationUnit.SECONDS)
                    terminal.println(
                        resultWord(value) +
                            muted(" · " + formatSeconds(elapsed))
                    )
                } else {
                    val aligned = (
                        " " + getResult(value)
                        ).padStart(structureWidth, rule.first())
                    terminal.println(muted(aligned))
                }
                detailsPrinted = false
            }
        }
    }

    /**
     * Displays the exception in the terminal: the failure glyph with
     * the error label, the message and the details of the cause.
     *
     * @param text The message to be displayed.
     * @param details The underlying cause details, if any.
     */
    override fun printException(text: String, details: String?) {
        displayException = true

        if (displayResult)
            terminal.println("")
        if (displayLine && !isDryRunMode) {
            terminal.println(
                terminal.theme.danger(
                    failureGlyph() + " " +
                        LanguageController.messages.infoLabelOnError
                )
            )
            terminal.println("")
        }
        terminal.println(terminal.theme.danger(text), width = proseWidth)
        // a present cause earns a details section even when it
        // carries no message of its own
        if (details != null) {
            terminal.println("")
            displayDetailsLine()
            terminal.println(muted(details), width = proseWidth)
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
     * Gets the styled result word of an outcome: the glyph and the
     * localized word, colored by the theme's semantics.
     *
     * @param value The outcome of the task.
     * @return The styled result word.
     */
    private fun resultWord(value: Boolean): String =
        if (value) {
            terminal.theme.success(
                successGlyph() + " " + getResult(value)
            )
        } else {
            terminal.theme.danger(
                failureGlyph() + " " + getResult(value)
            )
        }

    /**
     * Displays the details of the task a verbose run shows: the
     * command it executes, its conditional and the rule authors,
     * with the dotted frame opening for the raw tool output that
     * follows. The stream itself flows untouched - the frame
     * carries the structure, the tee carries the bytes.
     *
     * @param command The command the task executes.
     * @param conditional The conditional of the directive.
     * @param authors The authors of the rule.
     */
    override fun printTaskDetails(
        command: Command,
        conditional: DirectiveConditional,
        authors: List<String>
    ) {
        // the details are the verbose microscope, and the dry run
        // already prints its own trio through the historic calls
        if (!isVerboseMode || isDryRunMode) {
            return
        }
        detailsPrinted = true
        taskStart = TimeSource.Monotonic.markNow()
        printField(
            LanguageController.messages.infoLabelSystemCommand,
            command.toString()
        )
        if (conditional.type != DirectiveConditionalType.NONE) {
            printField(
                LanguageController.messages.infoLabelConditional,
                "(" + conditional.type + ") " + conditional.condition
            )
        }
        if (authors.isNotEmpty()) {
            printField(
                if (authors.size == 1) {
                    LanguageController.messages.infoLabelAuthor
                } else {
                    LanguageController.messages.infoLabelAuthors
                },
                authors.joinToString(", ") { it.trim() }
            )
        }
        terminal.println(muted(frameRule()))
    }

    /**
     * Displays the naming of a directive its conditional skipped:
     * the muted card of the verbose microscope, answering the
     * question a silent skip never could.
     *
     * @param identifier The identifier of the skipped directive.
     */
    override fun printSkippedDirective(identifier: String) {
        if (!isVerboseMode) {
            return
        }
        terminal.println(
            muted(
                skipGlyph() + " " +
                    LanguageController.messages
                        .infoDisplaySkippedConditional(identifier)
            )
        )
    }

    /**
     * Prints a verbose task field: the muted label aligned in the
     * gutter, the value wrapped under itself.
     *
     * @param label The field label.
     * @param value The field value.
     */
    private fun printField(label: String, value: String) {
        val gutter = " ".repeat(fieldGutterWidth)
        value.wrap(structureWidth - fieldGutterWidth).lineSequence()
            .forEachIndexed { index, line ->
                val prefix = if (index == 0) {
                    muted(label.padEnd(fieldGutterWidth))
                } else {
                    gutter
                }
                terminal.println(prefix + line)
            }
    }

    /**
     * The dotted rule that frames the raw tool output of a verbose
     * run.
     */
    private fun frameRule(): String {
        val frame = if (isDecorated) {
            RichTheme.frameGlyph
        } else {
            RichTheme.frameFallback
        }
        val spaced = "$frame "
        return spaced.repeat(structureWidth / spaced.length + 1)
            .take(structureWidth).trimEnd()
    }

    /**
     * The glyph that marks a conditionally skipped directive,
     * following the capability.
     */
    private fun skipGlyph(): String =
        if (isDecorated) RichTheme.skipGlyph else RichTheme.skipFallback

    /**
     * Displays the provided text wrapped at the readable measure of
     * the presentation.
     *
     * @param text The text to be displayed.
     */
    override fun printWrapped(text: String) {
        terminal.println(text.wrap(proseWidth))
    }

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
            terminal.println("")

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
     * Displays a line containing details: the muted rule that names
     * the details section.
     */
    private fun displayDetailsLine() {
        val label = LanguageController.messages.infoLabelOnDetails + " "
        val line = label.abbreviate(structureWidth)
        terminal.println(muted(line.padEnd(structureWidth, rule.first())))
    }

    /**
     * Gets the output separator with the provided text, framed in
     * the rule of the presentation; the styling happens at the
     * print sites, so the returned geometry is plain.
     *
     * @param message The provided text.
     * @return A string containing the output separator with the
     * provided text.
     */
    override fun displayOutputSeparator(message: String): String =
        " $message ".center(structureWidth, rule.first())

    /**
     * Gets the line separator of the presentation; the styling
     * happens at the print sites, so the returned geometry is
     * plain.
     *
     * @return A string containing the line separator.
     */
    override fun displaySeparator(): String = rule.repeat(structureWidth)

    /**
     * The glyph that marks a running entry, following the
     * capability.
     */
    private fun runningGlyph(): String =
        if (isDecorated) RichTheme.runningGlyph else RichTheme.runningFallback

    /**
     * The glyph that marks a success, following the capability.
     */
    private fun successGlyph(): String =
        if (isDecorated) RichTheme.successGlyph else RichTheme.successFallback

    /**
     * The glyph that marks a failure, following the capability.
     */
    private fun failureGlyph(): String =
        if (isDecorated) RichTheme.failureGlyph else RichTheme.failureFallback

    /**
     * Wraps the text in the terminal hyperlink escape, gated by the
     * terminal's own detection: a stream that cannot carry the link
     * keeps the plain text.
     *
     * @param text The visible text of the link.
     * @param url The target of the link.
     * @return The linked text, or the plain text.
     */
    private fun hyperlink(text: String, url: String): String =
        if (terminal.terminalInfo.ansiHyperLinks) {
            "\u001B]8;;$url\u001B\\$text\u001B]8;;\u001B\\"
        } else {
            text
        }

    /**
     * Seals the ledger entry of the file whose printing just ended.
     */
    private fun closeCurrentFile() {
        currentFile?.let {
            it.seconds = currentFileStart.elapsedNow()
                .toDouble(DurationUnit.SECONDS)
            summary += it
        }
        currentFile = null
    }

    /**
     * Displays the closing summary of the run: the verdict line and
     * the table of the processed files. A dry run validates nothing,
     * so its verdict makes no success claim and carries no table -
     * the run already printed everything it simulated.
     *
     * @param success Whether the whole run succeeded.
     */
    override fun printRunSummary(success: Boolean) {
        closeCurrentFile()
        if (summary.isEmpty()) {
            return
        }
        val tasks = summary.sumOf { it.tasks }
        if (isDryRunMode) {
            terminal.println(
                muted(
                    dryGlyph() + " " +
                        LanguageController.messages.infoDisplayDryRun(tasks)
                )
            )
            terminal.println("")
        } else {
            val verdict = if (success) {
                terminal.theme.success(
                    successGlyph() + " " +
                        LanguageController.messages
                            .infoDisplayTotalSuccess(summary.size, tasks)
                )
            } else {
                terminal.theme.danger(
                    failureGlyph() + " " +
                        LanguageController.messages
                            .infoDisplayTotalFailure(summary.size, tasks)
                )
            }
            terminal.println(verdict)
            terminal.println("")
            renderSummaryTable()
        }
        summary.clear()
    }

    /**
     * Renders the execution summary table: one row per file with
     * its task count, its time and its status, the file names
     * linked to the files themselves.
     */
    private fun renderSummaryTable() {
        val messages = LanguageController.messages
        val table = table {
            tableBorders = Borders.NONE
            header {
                cellBorders = Borders.BOTTOM
                row(
                    messages.infoDisplaySummaryFile,
                    messages.infoDisplaySummaryTasks,
                    messages.infoDisplaySummaryTime,
                    ""
                )
            }
            column(COUNT_COLUMN) { align = TextAlign.RIGHT }
            column(TIME_COLUMN) { align = TextAlign.RIGHT }
            column(STATUS_COLUMN) { align = TextAlign.RIGHT }
            body {
                summary.forEach { entry ->
                    row(
                        hyperlink(entry.name, entry.path),
                        entry.tasks,
                        formatSeconds(entry.seconds),
                        if (entry.success) {
                            successGlyph()
                        } else {
                            failureGlyph()
                        }
                    )
                }
            }
        }
        // the title rides the section rule, the table keeps the
        // borderless layout of the presentation: rules, not boxes -
        // the box belongs to the dialogs, where interruption is the
        // point
        val head = "$rule$rule ${messages.infoDisplaySummaryTitle} "
        terminal.println(
            muted(head + rule.repeat((structureWidth - head.length).coerceAtLeast(1)))
        )
        terminal.println(table)
        terminal.println("")
    }

    /**
     * Formats a duration as the summary renders it: one decimal
     * digit with the separator of the session locale.
     *
     * @param seconds The elapsed seconds.
     * @return The formatted duration with its unit.
     */
    private fun formatSeconds(seconds: Double): String =
        String.format(Locale.ROOT, "%.1f", seconds)
            .replace(
                '.',
                Session.userInterfaceOptions.locale.decimalSeparator
            ) + " s"

    /**
     * The glyph that marks a simulated dry run, following the
     * capability.
     */
    private fun dryGlyph(): String =
        if (isDecorated) RichTheme.dryGlyph else RichTheme.dryFallback
}
