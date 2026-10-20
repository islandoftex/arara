// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.center
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.cli.utils.wrap
import org.islandoftex.arara.core.utils.formatString

@Suppress("TooManyFunctions")
class StyledConsoleRenderer(private val terminal: Terminal) : ConsoleRenderer {
    private val longestMatch: Int by lazy {
        listOf(
            LanguageController.messages.INFO_LABEL_ON_SUCCESS,
            LanguageController.messages.INFO_LABEL_ON_FAILURE,
            LanguageController.messages.INFO_LABEL_ON_ERROR
        ).map { it.length }.maxOrNull()!!
    }

    private var displayLine = true
    private var displayResult = false
    private var displayRolling = false
    private var displayException = false

    private val isDryRunMode: Boolean
        get() = LinearExecutor.executionOptions.executionMode ==
            ExecutionMode.DRY_RUN

    private val isVerboseMode: Boolean
        get() = LinearExecutor.executionOptions.verbose

    private val isDecorated: Boolean =
        terminal.terminalInfo.ansiLevel != AnsiLevel.NONE

    private val rule: String
        get() = if (isDecorated) StyledTheme.ruleGlyph else StyledTheme.ruleFallback

    private val leader: String
        get() = if (isDecorated) StyledTheme.leaderGlyph else StyledTheme.leaderFallback

    private val structureWidth: Int
        get() = terminal.size.width.coerceAtLeast(1)

    private val proseWidth: Int
        get() = structureWidth.coerceAtMost(StyledTheme.proseMaximumWidth)

    private val muted get() = terminal.theme.muted

    override fun printBanner() {
        terminal.println(StyledTheme.logo(DisplayUtils.logoString))
        terminal.println(muted("arara ${AraraAPI.version}"))
        terminal.println("")
    }

    override fun printFileInformation(file: ProjectFile) {
        val name = file.path.fileName
        val metadata = DisplayUtils.byteSizeToString(file.path.fileSize) +
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

    private fun buildShortEntry(name: String, task: String) {
        val head = "${runningGlyph()} $name · $task "
        val reserved = successGlyph().length + 1 + longestMatch
        val fill = (structureWidth - head.length - reserved)
            .coerceAtLeast(1)
        terminal.print(head + muted(leader.repeat(fill)) + " ")
    }

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

    private fun buildDryRunEntry(name: String, task: String) {
        if (displayRolling) {
            terminal.println("")
        } else {
            displayRolling = true
        }
        terminal.println(muted("[DR] ${runningGlyph()} $name · $task"))
        terminal.println(muted(displaySeparator()))
    }

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
                terminal.println(styled)
            } else {
                val aligned = (
                    " " + getResult(value)
                    ).padStart(structureWidth, rule.first())
                terminal.println("")
                terminal.println(muted(aligned))
            }
        }
    }

    override fun printException(text: String, details: String?) {
        displayException = true

        if (displayResult)
            terminal.println("")
        if (displayLine && !isDryRunMode) {
            terminal.println(
                terminal.theme.danger(
                    failureGlyph() + " " +
                        LanguageController.messages.INFO_LABEL_ON_ERROR
                )
            )
            terminal.println("")
        }
        terminal.println(terminal.theme.danger(text), width = proseWidth)
        if (details != null) {
            terminal.println("")
            displayDetailsLine()
            terminal.println(muted(details), width = proseWidth)
        }
    }

    private fun getResult(value: Boolean): String {
        return if (value)
            LanguageController.messages.INFO_LABEL_ON_SUCCESS
        else
            LanguageController.messages.INFO_LABEL_ON_FAILURE
    }

    override fun printWrapped(text: String) {
        terminal.println(text.wrap(proseWidth))
    }

    override fun printAuthors(authors: List<String>) {
        val line = if (authors.size == 1)
            LanguageController.messages.INFO_LABEL_AUTHOR
        else
            LanguageController.messages.INFO_LABEL_AUTHORS
        val text = if (authors.isEmpty())
            LanguageController.messages.INFO_LABEL_NO_AUTHORS
        else
            authors.joinToString(", ") { it.trim() }
        printWrapped("$line $text")
    }

    override fun printConditional(conditional: DirectiveConditional) {
        if (conditional.type !== DirectiveConditionalType.NONE) {
            printWrapped(
                LanguageController.messages.INFO_LABEL_CONDITIONAL +
                    " (" + conditional.type + ") " +
                    conditional.condition
            )
        }
    }

    override fun printTime(seconds: Double) {
        if (displayLine || displayException)
            terminal.println("")

        val secondDuration = seconds.seconds
        val separator = Session.userInterfaceOptions.locale
            .decimalSeparator.toString()
        val time = secondDuration.inWholeSeconds.toString() + separator +
            (secondDuration - secondDuration.inWholeSeconds.seconds)
                .inWholeMilliseconds.toString()
        val text = LanguageController.messages.INFO_DISPLAY_EXECUTION_TIME.formatString(time)
        printWrapped(text)
    }

    private fun displayDetailsLine() {
        val label = LanguageController.messages.INFO_LABEL_ON_DETAILS + " "
        val line = label.take(structureWidth)
        terminal.println(muted(line.padEnd(structureWidth, rule.first())))
    }

    override fun displayOutputSeparator(message: String): String =
        " $message ".center(structureWidth, rule.first())

    override fun displaySeparator(): String = rule.repeat(structureWidth)

    private fun runningGlyph(): String =
        if (isDecorated) StyledTheme.runningGlyph else StyledTheme.runningFallback

    private fun successGlyph(): String =
        if (isDecorated) StyledTheme.successGlyph else StyledTheme.successFallback

    private fun failureGlyph(): String =
        if (isDecorated) StyledTheme.failureGlyph else StyledTheme.failureFallback

    private fun hyperlink(text: String, @Suppress("UNUSED_PARAMETER") url: String): String = text
}
