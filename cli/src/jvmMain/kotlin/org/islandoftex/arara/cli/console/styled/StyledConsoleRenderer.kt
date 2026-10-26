// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.TimeSource
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.console.FileSummary
import org.islandoftex.arara.cli.console.RunSummary
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/**
 * Terminal-cell-aware task presentation with optional color and live feedback.
 * Host streams remain open when the renderer closes.
 */
@Suppress("TooManyFunctions")
class StyledConsoleRenderer internal constructor(
    private val terminal: Terminal,
    clock: TimeSource,
    schedulerFactory: () -> ProgressScheduler
) : ConsoleRenderer {
    /** Create a renderer using a monotonic clock and the default progress scheduler. */
    constructor(terminal: Terminal) : this(terminal, TimeSource.Monotonic, { ExecutorProgressScheduler() })

    private val longestMatch: Int
        get() = maxOf(
            layout.cells(LanguageController.messages.infoLabelOnSuccess),
            layout.cells(LanguageController.messages.infoLabelOnFailure),
            layout.cells(LanguageController.messages.infoLabelOnError)
        )

    private val layout = StyledLayout(terminal)
    private val progress = LiveProgress(terminal, layout, clock, schedulerFactory)
    private var compactEntry = false
    private var displayLine = false
    private var displayResult = false
    private var displayRolling = false
    private var displayException = false
    private var outputFrameOpen = false

    private val isDryRunMode: Boolean
        get() = LinearExecutor.executionOptions.executionMode ==
            ExecutionMode.DRY_RUN

    private val isVerboseMode: Boolean
        get() = LinearExecutor.executionOptions.verbose

    private val isDecorated: Boolean =
        terminal.terminalInfo.ansiLevel != AnsiLevel.NONE

    private val canAnimate: Boolean
        get() = Session.userInterfaceOptions.enableAnimations && isDecorated &&
            terminal.terminalInfo.outputInteractive && cursorAvailable

    // Mordant 3.1's supportsAnsiCursor flag is false on ordinary terminals.
    // Its cursor API returns no movements when cursor control is unavailable.
    private val cursorAvailable: Boolean
        get() = terminal.cursor.getMoves { startOfLine() }.isNotEmpty()

    private val rule: String
        get() = if (isDecorated) StyledTheme.ruleGlyph else StyledTheme.ruleFallback

    private val leader: String
        get() = if (isDecorated) StyledTheme.leaderGlyph else StyledTheme.leaderFallback

    private val muted get() = terminal.theme.muted

    override fun printBanner() {
        suspendProgress()
        layout.print(StyledTheme.logo(DisplayUtils.logoString))
        layout.print(muted(DisplayUtils.versionLine))
        terminal.println("")
    }

    override fun printProjectInformation(name: String, directory: String) {
        suspendProgress()
        layout.print("${LanguageController.messages.infoLabelProject} $name ($directory)")
        terminal.println("")
    }

    override fun printFileInformation(file: ProjectFile) {
        suspendProgress()
        displayRolling = false
        displayLine = false
        displayResult = false
        displayException = false
        val name = file.path.fileName
        val metadata = DisplayUtils.byteSizeToString(file.sourcePath.fileSize) +
            " · " + file.sourcePath.lastModified
        val title = hyperlink(name, file.path.toJVMPath().toUri().toASCIIString())
        val fill = layout.width - layout.cells(name) - layout.cells(metadata) - HEADER_SPACING
        if (fill > 0) {
            terminal.println(title + muted(" " + rule.repeat(fill) + " " + metadata))
        } else {
            layout.print(title)
            layout.print(muted(metadata))
        }
        terminal.println("")
    }

    override fun printEntry(name: String, task: String) {
        suspendProgress()
        closeOutputFrame()
        compactEntry = false
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
        val animated = canAnimate
        val limit = layout.width - if (animated) 1 else 0
        val wordWidth = maxOf(longestMatch, if (animated) LiveProgress.CLOCK_WIDTH else 0)
        val reserved = layout.cells(successGlyph()) + 1 + wordWidth
        val headWidth = limit - reserved - ENTRY_SPACING
        compactEntry = headWidth >= MINIMUM_HEAD_WIDTH
        val head = "${runningGlyph()} $name · $task"
        if (compactEntry) {
            val fitted = layout.fit(head, headWidth)
            val fill = limit - layout.cells(fitted) - reserved - HEADER_SPACING
            terminal.print(fitted + " " + muted(leader.repeat(fill)) + " ")
            if (animated) {
                progress.start(limit - reserved, reserved, Session.userInterfaceOptions.locale.decimalSeparator)
            }
        } else {
            layout.print(head)
        }
    }

    private fun buildLongEntry(name: String, task: String) {
        if (displayRolling) {
            terminal.println("")
        } else {
            displayRolling = true
        }
        terminal.println(muted(displaySeparator()))
        layout.print("${runningGlyph()} $name · $task")
        terminal.println(muted(displaySeparator()))
    }

    private fun buildDryRunEntry(name: String, task: String) {
        if (displayRolling) {
            terminal.println("")
        } else {
            displayRolling = true
        }
        layout.print(muted("[DR] ${runningGlyph()} $name · $task"))
        terminal.println(muted(displaySeparator()))
    }

    override fun printEntryResult(value: Boolean) {
        stopProgress()
        closeOutputFrame()
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
                if (compactEntry) terminal.println(styled) else layout.print(styled)
            } else {
                val result = layout.fit(getResult(value), layout.width)
                val aligned = rule.repeat((layout.width - layout.cells(result)).coerceAtLeast(0)) + result
                terminal.println("")
                terminal.println(if (value) terminal.theme.success(aligned) else terminal.theme.danger(aligned))
            }
        }
    }

    override fun printException(text: String, details: String?) {
        stopProgress()
        closeOutputFrame()
        displayException = true

        if (displayResult)
            terminal.println("")
        if (displayLine && !isDryRunMode) {
            val error = terminal.theme.danger(failureGlyph() + " " +
                LanguageController.messages.infoLabelOnError)
            if (compactEntry && !isVerboseMode) terminal.println(error) else layout.print(error)
            terminal.println("")
        }
        displayLine = false
        compactEntry = false
        layout.print(terminal.theme.danger(text))
        if (details != null) {
            terminal.println("")
            displayDetailsLine()
            layout.print(muted(details))
        }
    }

    private fun getResult(value: Boolean): String {
        return if (value)
            LanguageController.messages.infoLabelOnSuccess
        else
            LanguageController.messages.infoLabelOnFailure
    }

    override fun printWrapped(text: String) {
        suspendProgress()
        layout.print(text)
    }

    override fun printAuthors(authors: List<String>) {
        val line = if (authors.size == 1)
            LanguageController.messages.infoLabelAuthor
        else
            LanguageController.messages.infoLabelAuthors
        val text = if (authors.isEmpty())
            LanguageController.messages.infoLabelNoAuthors
        else
            authors.joinToString(", ") { it.trim() }
        printField(line, text)
    }

    override fun printConditional(conditional: DirectiveConditional) {
        if (conditional.type !== DirectiveConditionalType.NONE) {
            printField(LanguageController.messages.infoLabelConditional,
                "(" + conditional.type + ") " + conditional.condition)
        }
    }

    override fun printTime(seconds: Double) {
        suspendProgress()
        if (displayLine || displayException)
            terminal.println("")

        val time = DisplayUtils.elapsedSeconds(seconds)
        printWrapped(LanguageController.messages.infoDisplayExecutionTime(time))
    }

    private fun displayDetailsLine() {
        val label = layout.fit(LanguageController.messages.infoLabelOnDetails + " ", layout.width)
        terminal.println(muted(label + rule.repeat((layout.width - layout.cells(label)).coerceAtLeast(0))))
    }

    override fun displayOutputSeparator(message: String): String {
        val text = layout.fit(" $message ", layout.width)
        val remaining = (layout.width - layout.cells(text)).coerceAtLeast(0)
        return rule.repeat(remaining / 2) + text + rule.repeat(remaining - remaining / 2)
    }

    override fun displaySeparator(): String = rule.repeat(layout.width)

    private fun runningGlyph(): String =
        if (isDecorated) StyledTheme.runningGlyph else StyledTheme.runningFallback

    private fun successGlyph(): String =
        if (isDecorated) StyledTheme.successGlyph else StyledTheme.successFallback

    private fun failureGlyph(): String =
        if (isDecorated) StyledTheme.failureGlyph else StyledTheme.failureFallback

    private fun hyperlink(text: String, url: String): String =
        if (terminal.terminalInfo.ansiHyperLinks) TextStyle(hyperlink = url)(text) else text

    override fun printTaskDetails(command: String, conditional: DirectiveConditional, authors: List<String>) {
        if (isVerboseMode && !isDryRunMode) {
            closeOutputFrame()
            terminal.println("")
            printField(LanguageController.messages.infoLabelSystemCommand, command)
            printConditional(conditional)
            printAuthors(authors)
            terminal.println(muted(frameRule()))
            outputFrameOpen = true
        }
    }

    override fun printDryRunCommand(command: String) =
        printField(LanguageController.messages.infoLabelSystemCommand, command)

    private fun printField(label: String, value: String) {
        suspendProgress()
        val labelWidth = layout.cells(label)
        val footprint = maxOf(labelWidth + 1, StyledTheme.fieldGutterWidth)
        if (layout.width - footprint < StyledTheme.minimumValueWidth) {
            layout.print(muted(label))
            layout.print(value)
        } else {
            layout.render(value, layout.width - footprint).lineSequence().forEachIndexed { index, line ->
                val prefix = if (index == 0) muted(label + " ".repeat(footprint - labelWidth))
                    else " ".repeat(footprint)
                terminal.println(prefix + line)
            }
        }
    }

    private fun frameRule(): String {
        val glyph = if (isDecorated) StyledTheme.frameGlyph else StyledTheme.frameFallback
        return "$glyph  ".repeat(layout.width / FRAME_SPACING + 1).take(layout.width).trimEnd()
    }

    private fun closeOutputFrame() {
        if (outputFrameOpen) {
            terminal.println("")
            terminal.println(muted(frameRule()))
            outputFrameOpen = false
        }
    }

    override fun printSkippedDirective(identifier: String) {
        printWrapped("- $identifier · ${LanguageController.messages.infoConsoleSkipped}")
    }

    override fun printCommandFailure(output: String) {
        suspendProgress()
        if (!isVerboseMode && output.isNotBlank()) {
            val lines = output.trimEnd().lineSequence().toList()
            terminal.println("")
            terminal.println(terminal.theme.danger(displaySeparator()))
            if (lines.size > FAILURE_TAIL_LINES) {
                layout.print("...")
                if (Session.loggingOptions.format != LogFormat.NONE) {
                    layout.print(muted(LanguageController.messages.infoDisplayFullOutputInLog))
                }
            }
            lines.takeLast(FAILURE_TAIL_LINES).forEach(layout::print)
            terminal.println(terminal.theme.danger(displaySeparator()))
        }
    }

    override fun printRunSummary(summary: RunSummary) {
        suspendProgress()
        closeOutputFrame()
        StyledSummaryRenderer(terminal, layout).print(summary)
    }

    override fun printRunSummary(success: Boolean, files: List<Pair<String, Int?>>) = printRunSummary(
        RunSummary(success, isDryRunMode, files.map { (path, code) ->
            val file = MPPPath(path)
            FileSummary(file, "", file.parent, exitCode = code)
        })
    )

    private fun stopProgress() {
        if (progress.stop() && displayLine && compactEntry) {
            terminal.println("")
            compactEntry = false
        }
    }

    override fun suspendProgress() {
        stopProgress()
        if (displayLine && compactEntry) {
            terminal.println("")
            compactEntry = false
        }
    }

    override fun close() {
        suspendProgress()
        closeOutputFrame()
        progress.close()
    }

    private companion object {
        const val FAILURE_TAIL_LINES = 12
        const val HEADER_SPACING = 2
        const val ENTRY_SPACING = 3
        const val MINIMUM_HEAD_WIDTH = 8
        const val FRAME_SPACING = 3
    }
}
