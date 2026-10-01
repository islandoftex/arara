// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.rich

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
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

        if (!isDryRunMode) {
            if (!isVerboseMode) {
                val styled = if (value) {
                    terminal.theme.success(
                        successGlyph() + " " + getResult(value)
                    )
                } else {
                    terminal.theme.danger(
                        failureGlyph() + " " + getResult(value)
                    )
                }
                // the reserved space keeps the word aligned even when
                // the label of another language runs longer
                terminal.print(styled.padStart(successGlyph().length + 1 + longestMatch))
            } else {
                val aligned = (
                    " " + getResult(value)
                    ).padStart(structureWidth, rule.first())
                terminal.println("")
                terminal.println(muted(aligned))
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
}
