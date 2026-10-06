// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
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
 * Implements the styled console renderer: the modern presentation of
 * the run, built on the terminal library the help output already
 * renders through. The presentation keeps one readable measure -
 * the rules, the leaders, the alignment and the prose all hold
 * that width, so a wide terminal carries a page, not a line
 * running to the horizon; the glyphs and the colors ride the
 * detected capability, so a piped stream keeps the structure with
 * plain characters and no escapes. The renderer holds no logging:
 * the run log writes the diagnostic artifact, so the presentation
 * can never change what the log records.
 *
 * @author Island of TeX
 */
@Suppress("TooManyFunctions", "LargeClass")
class StyledConsoleRenderer(
    /**
     * The terminal every line of the presentation prints through.
     */
    private val terminal: Terminal
) : ConsoleRenderer {
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

    /**
     * Whether a compact entry left its line open for the outcome to
     * land on: the error path tells the label that lands like a
     * result from the section title by it.
     */
    private var entryLineOpen = false

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
     * Whether an exception has printed in the current file, so the
     * closing blocks know whether to separate from it.
     */
    private var displayException = false

    /**
     * Whether an outcome landed on an entry line and nothing has
     * separated it since: the quoted panel clears it when it
     * quotes, and the closing verdict consults it when the run
     * failed - so a silent failure or a halted run never glues
     * the verdict to the landed word.
     */
    private var landedOutcome = false

    /**
     * Whether the skip card printed for the current file: a file
     * whose directives were all skipped ends without a task, so
     * the closing verdict separates from the card itself.
     */
    private var skipCardPrinted = false

    /**
     * The entry of a file in the run ledger the closing summary
     * renders.
     */
    private class SummaryEntry(
        /** The file name as the summary renders it. */
        val name: String,
        /** The full path for the hyperlink. */
        val path: String,
        /** The number of tasks the file ran. */
        var tasks: Int,
        /** The wall-clock time the file took, in seconds. */
        var seconds: Double,
        /** Whether every task of the file succeeded. */
        var success: Boolean,
        /** The identifier of the project the file belongs to, or
         *  -1 when the run is single-project and no project line
         *  appeared. */
        val projectId: Int = -1
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
     * The counter that gives each project of the run a unique
     * identifier, so the summary groups by identity and never
     * merges two projects that share a display name.
     */
    private var projectCounter = 0

    /**
     * The identifier of the project whose territory the run is
     * in, or -1 when the run is single-project and no project
     * line appeared. The summary table groups by this identifier.
     */
    private var currentProjectId = -1

    /**
     * The display names of the run's projects, keyed by their
     * identifiers, so the summary titles name what the grouping
     * gathers.
     */
    private val projectDisplayNames = mutableMapOf<Int, String>()

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
     * The monotonic mark of the current task's start, taken when
     * its details print or when its compact line opens the live
     * feedback, so the frames can count what the task has been
     * running for.
     */
    private var taskStart = TimeSource.Monotonic.markNow()

    /**
     * The executor that breathes life into the pending outcome
     * zone, created only when the live feedback is first asked
     * for. Its single thread is a daemon: it dies with the run,
     * and a run that never animates never pays for it.
     */
    private val animator: ScheduledExecutorService by lazy {
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "arara-live-feedback").apply { isDaemon = true }
        }
    }

    /**
     * The live feedback of the running task: the scheduled start
     * that waits for the task to outlive the threshold, the
     * ticker cycling its frames once it did, and whether the
     * breathing cell currently hides the terminal cursor.
     */
    private var pendingSpin: ScheduledFuture<*>? = null
    private var spinTicker: ScheduledFuture<*>? = null
    private var cursorHidden = false

    /**
     * The lock serializing the live feedback against the outcomes
     * that replace it: the result landing on the open line and the
     * exception claiming it never race the frame writer.
     */
    private val spinLock = Any()

    /**
     * The column where the outcome zone of the open compact entry
     * line begins, recorded when the line prints so the frames
     * and the outcome can re-anchor to it against whatever
     * displaced the cursor while the task ran.
     */
    private var spinZone = 0

    /**
     * The width of the outcome zone of the open compact entry
     * line, recorded when the line prints so every frame paints
     * the whole zone at a constant width - the anti-flicker
     * discipline of a cell-local animation.
     */
    private var spinZoneWidth = 0

    /**
     * Whether the open entry line armed the live feedback, so its
     * landings know to re-anchor to the zone the frames breathed
     * in.
     */
    private var spinArmed = false

    /**
     * The generation of the live feedback: every stop retires the
     * dispatches of the animation it ends, so a breath or a tick
     * the executor already picked up but the landing outran finds
     * its generation retired under the lock and touches nothing -
     * no hidden cursor, no frame, no orphaned ticker fighting the
     * outcome that claimed the zone.
     */
    private var spinGeneration = 0

    /**
     * The width of the label column of the verbose task fields.
     */
    private val fieldGutterWidth = StyledTheme.fieldGutterWidth

    /**
     * The layout constants of the presentation.
     */
    private companion object {
        /**
         * The least space the dotted leader and its separator keep
         * for themselves, so an abbreviation never squeezes them
         * out of the line.
         */
        const val leaderFloor = 2
        /** The column indexes of the execution summary table. */
        const val FILE_COLUMN = 0
        const val COUNT_COLUMN = 1
        const val TIME_COLUMN = 2
        const val STATUS_COLUMN = 3

        /**
         * The number of output lines the failure panel quotes: the
         * classic TeX error window fits within the last ten lines,
         * the number the tail convention made familiar, and the
         * full record lives in the log file.
         */
        const val PANEL_TAIL_LINES = 10

        /**
         * How long a task must run before the live feedback earns
         * its seat: fast tasks stay exactly as static as they
         * always were, and only real work starts breathing.
         */
        const val ANIMATION_THRESHOLD_MS = 1000

        /**
         * The period at which the live feedback cycles its frames.
         */
        const val ANIMATION_FRAME_MS = 90

        /**
         * The frames the live feedback breathes through, in the
         * outcome zone of the open entry line: the braille dots
         * cycling their patterns, the classic working vocabulary.
         */
        val SPIN_FRAMES = listOf(
            "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"
        )

        /**
         * The seconds in a minute, the boundary where the counter
         * retires its tenths and starts telling time the clock
         * way.
         */
        const val SECONDS_PER_MINUTE = 60

        /**
         * The seconds in an hour, the boundary where the counter
         * grows to the full clock.
         */
        const val SECONDS_PER_HOUR = 3600
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
        get() = if (isDecorated) StyledTheme.ruleGlyph else StyledTheme.ruleFallback

    /**
     * The dotted leader between an entry and its result, following
     * the capability.
     */
    private val leader: String
        get() = if (isDecorated) StyledTheme.leaderGlyph else StyledTheme.leaderFallback

    /**
     * The width every text of the presentation wraps at: the
     * readable measure, never past the terminal, one width for
     * the structures and the prose alike - the whole presentation
     * is one page of that width, so a wide terminal holds a page,
     * not a line running to the horizon.
     */
    private val structureWidth: Int
        get() = terminal.size.width
            .coerceAtMost(StyledTheme.proseMaximumWidth)

    /**
     * The muted style of the chrome: the rules, the leaders and the
     * metadata.
     */
    private val muted get() = terminal.theme.muted

    /**
     * Displays the application banner in the terminal: the tinted
     * logo, then - after the art breathes - the muted line naming
     * the run: the version and the tool's own description.
     */
    override fun printBanner() {
        terminal.println(StyledTheme.logo(Display.logoString))
        terminal.println("")
        terminal.println(muted(Display.versionLine))
        terminal.println("")
    }

    /**
     * Displays the project information in the terminal: the
     * structural line that opens a project's territory, with the
     * label word and the working directory muted and the name
     * in the foreground. The display name arrives resolved -
     * the anonymous project's label or the specification's own
     * name - and the summary table groups by the project's
     * identifier, with this name as the title above the grid.
     *
     * @param name The display name of the project.
     * @param directory The working directory's short name.
     */
    override fun printProjectInformation(name: String, directory: String) {
        currentProjectId = ++projectCounter
        projectDisplayNames[currentProjectId] = name
        // the label and the directory are metadata (muted); the
        // name is the data (foreground) - the same three-level
        // contrast the file information lines carry. A name too
        // long for the measure abbreviates with the same
        // ellipsis the entry names get, so the line fits the
        // presentation's own geometry
        val label = LanguageController.messages.infoLabelProject + " "
        val suffix = " ($directory)"
        val available = structureWidth - label.length - suffix.length
        val fittedName = if (name.length > available && available > 0) {
            name.abbreviate(available)
        } else {
            name
        }
        terminal.println(
            muted(label) + fittedName + muted(suffix)
        )
    }

    /**
     * Displays the file information in the terminal: the linked
     * name of the file with the dimmed rule running flush to the
     * muted metadata at the edge of the measure.
     *
     * @param file The project file.
     */
    override fun printFileInformation(file: ProjectFile) {
        closeCurrentFile()
        // a new file clears the prior file's exception state, so
        // the closing summary separates only after an error it
        // actually follows; each file opens its own entry blocks,
        // so the rolling separator resets too - the first entry of
        // a file never carries a stale blank from the previous one;
        // and the result flag resets as well, so an error in the
        // new file separates from the header alone, not from a
        // blank the previous file's landed result left behind
        displayException = false
        displayRolling = false
        displayResult = false
        landedOutcome = false
        skipCardPrinted = false
        currentFile = SummaryEntry(
            file.path.fileName,
            file.path.toString(),
            0,
            0.0,
            true,
            currentProjectId
        )
        currentFileStart = TimeSource.Monotonic.markNow()
        val name = file.path.fileName
        val metadata = byteSizeToString(file.path.fileSize) +
            " · " + file.path.lastModified
        val title = hyperlink(name, file.path.toString())
        // the rule runs flush from the name to the muted metadata,
        // dimmed like the summary's section rule so the per-run
        // rules rhyme - and the line ends where the metadata ends,
        // with no stub to close it
        val head = "$name "
        val tail = " $metadata"
        val fill = (
            structureWidth - head.length - tail.length
            ).coerceAtLeast(1)
        terminal.println(
            title + muted(" " + rule.repeat(fill) + " ") + muted(metadata)
        )
        terminal.println("")
    }

    /**
     * Displays the current entry in the terminal: the running glyph
     * with the rule and the task, as a compact line the outcome
     * lands on, or a framed block in the verbose and dry-run
     * shapes.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    override fun printEntry(name: String, task: String) {
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
        // the leader reserves exactly the space the glyph, its
        // spacing and the longest result word need, so the aligned
        // result lands at the terminal edge; a head longer than the
        // space left abbreviates rather than breaking the layout,
        // and the leader's floor and its separator are part of the
        // reservation
        val reserved = successGlyph().length + 1 + longestMatch
        val head = "${runningGlyph()} $name · $task "
            .abbreviate(
                // the abbreviation's own minimum is two columns, so
                // the budget keeps it: a degenerate width degrades
                // to an ugly head, never a raw crash
                (structureWidth - reserved - leaderFloor).coerceAtLeast(2)
            )
        val fill = (structureWidth - head.length - reserved - 1)
            .coerceAtLeast(1)
        terminal.print(
            head + muted(leader.repeat(fill)) + muted(" ")
        )
        // the compact line stays open: the outcome - a result or an
        // error label - lands on it
        entryLineOpen = true
        // the live feedback breathes in the outcome zone this
        // arithmetic just built, gated on the user's blessing,
        // the escape vocabulary and a live terminal alike: a
        // Windows legacy console is interactive yet carries no
        // escapes, and frames written there would render as
        // bytes. It waits for the task to outlive the threshold,
        // then claims the zone until the outcome replaces it.
        // The counter the frames carry measures from the task's
        // true beginning
        spinZone = head.length + fill + 1
        spinZoneWidth = reserved
        spinArmed = Session.userInterfaceOptions.enableAnimations &&
            isDecorated &&
            terminal.terminalInfo.outputInteractive
        if (spinArmed) {
            taskStart = TimeSource.Monotonic.markNow()
            startLiveFeedback()
        }
    }

    /**
     * Starts the live feedback for the task whose entry line just
     * opened: the first frame waits for the threshold, so every
     * fast task stays exactly as static as it always was.
     */
    private fun startLiveFeedback() {
        val generation = stopLiveFeedback()
        pendingSpin = animator.schedule(
            { breathe(generation) },
            ANIMATION_THRESHOLD_MS.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * The first breath of the live feedback: the terminal cursor
     * yields the breathing zone to the frames - the library
     * restores it if the program dies while the zone breathes -
     * and the ticker writes each frame and steps back, so the
     * cursor never leaves the outcome zone and nothing outside it
     * is ever rewritten. Each frame carries the elapsed time the
     * task has been running, in the logo's tint: motion, color
     * and information in the space the outcome will claim.
     *
     * A breath whose generation the landing retired returns
     * before touching the terminal, so a dispatch the landing
     * outran cannot resurrect the animation over the outcome.
     */
    internal fun breathe(generation: Int) {
        synchronized(spinLock) {
            if (generation != spinGeneration) {
                return
            }
            terminal.cursor.hide()
            cursorHidden = true
            var frame = 0
            fun breatheOnce() {
                val counter = formatElapsed(
                    taskStart.elapsedNow().toDouble(DurationUnit.SECONDS)
                )
                // every frame re-anchors to the outcome zone and
                // paints it whole: the frame padded with trailing
                // spaces to the zone's exact, constant width, so
                // each write overwrites every column the outcome
                // claims in place - a shrinking counter, stray
                // typing, nothing lingers, and no clear escape
                // ever leaves an empty state for the terminal to
                // render between the frames
                anchorOutcomeZone()
                val width = SPIN_FRAMES[frame].length + 1 + counter.length
                terminal.rawPrint(
                    StyledTheme.logo(SPIN_FRAMES[frame]) + " " + counter +
                        " ".repeat((spinZoneWidth - width).coerceAtLeast(0))
                )
            }
            breatheOnce()
            spinTicker = animator.scheduleAtFixedRate(
                {
                    synchronized(spinLock) {
                        // the same retirement holds for the ticks: a
                        // tick the stop outran paints nothing over
                        // the landed outcome
                        if (generation == spinGeneration) {
                            frame = (frame + 1) % SPIN_FRAMES.size
                            breatheOnce()
                        }
                    }
                },
                ANIMATION_FRAME_MS.toLong(),
                ANIMATION_FRAME_MS.toLong(),
                TimeUnit.MILLISECONDS
            )
        }
    }

    /**
     * Anchors the cursor to the outcome zone of the open entry
     * line: every frame and every landing starts from the zone
     * the outcome owns, healing whatever displacement stray
     * typing left behind them. The anchor only positions - the
     * frames and the outcomes paint the zone whole themselves.
     */
    private fun anchorOutcomeZone() {
        terminal.cursor.move {
            startOfLine()
            right(spinZone)
        }
    }

    /**
     * Formats the elapsed time the live feedback carries: the
     * counter tells time the way clocks do - live tenths under a
     * minute, minutes and seconds under an hour, the full clock
     * beyond - so its width stays stable as the scale grows and
     * every rung fits the zone the outcome claims. The escalated
     * rungs are digits and colons alone, so no locale ever
     * reshapes them.
     *
     * @param seconds The elapsed seconds of the running task.
     * @return The counter the frames carry beside them.
     */
    internal fun formatElapsed(seconds: Double): String {
        val whole = seconds.toInt()
        return when {
            seconds < SECONDS_PER_MINUTE ->
                String.format(
                    Session.userInterfaceOptions.locale.toJVMLocale(),
                    "%.1f", seconds
                ) + "s"
            seconds < SECONDS_PER_HOUR ->
                String.format(
                    Locale.ROOT, "%d:%02d",
                    whole / SECONDS_PER_MINUTE,
                    whole % SECONDS_PER_MINUTE
                )
            else ->
                String.format(
                    Locale.ROOT, "%d:%02d:%02d",
                    whole / SECONDS_PER_HOUR,
                    (whole % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE,
                    whole % SECONDS_PER_MINUTE
                )
        }
    }

    /**
     * Stops the live feedback wherever it stands: the scheduled
     * start that never fired, or the breathing frames - which
     * need no erasing, as the outcome overwrites the cell they
     * breathe in - and the terminal cursor returns for the
     * landing. The stop retires the generation of the animation
     * it ends, so the dispatches the executor already holds find
     * their era closed when they run.
     *
     * @return The generation this stop created, for the animation
     *   that follows to claim as its own.
     */
    private fun stopLiveFeedback(): Int {
        synchronized(spinLock) {
            spinGeneration++
            pendingSpin?.cancel(false)
            pendingSpin = null
            spinTicker?.cancel(false)
            spinTicker = null
            if (cursorHidden) {
                terminal.cursor.show()
                cursorHidden = false
            }
            return spinGeneration
        }
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
        // a degenerate width abbreviates ugly instead of breaking
        // the layout: the budget keeps the abbreviation's own
        // minimum of two columns
        terminal.println(
            "${runningGlyph()} $name · $task"
                .abbreviate(structureWidth.coerceAtLeast(2))
        )
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
        // a degenerate width abbreviates ugly instead of breaking
        // the layout: the budget keeps the abbreviation's own
        // minimum of two columns
        terminal.println(
            muted(
                "[DR] ${runningGlyph()} $name · $task"
                    .abbreviate(structureWidth.coerceAtLeast(2))
            )
        )
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
        stopLiveFeedback()
        entryLineOpen = false
        displayResult = true
        currentFile?.let { it.success = it.success && value }

        if (!isDryRunMode) {
            if (!isVerboseMode) {
                if (spinArmed) {
                    // the landing re-anchors like the frames do,
                    // so the outcome owns the zone no matter what
                    // displaced the cursor while the task ran
                    anchorOutcomeZone()
                    spinArmed = false
                }
                // the reserved space keeps the word aligned even when
                // the label of another language runs longer, and the
                // line completes here: whatever follows starts on
                // its own line
                terminal.println(
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
            // an outcome landed on the entry line: the quoted panel
            // separates it when it quotes, and the closing verdict
            // when the run failed without one
            landedOutcome = true
        }
    }

    /**
     * Displays the exception in the terminal: an error striking an
     * open compact entry lands its label on the line like an
     * outcome, any other error titles its own section with the rule
     * every section title of the presentation carries, the message
     * speaks in the default voice beneath it, and a present cause
     * earns the muted details section.
     *
     * @param text The message to be displayed.
     * @param details The underlying cause details, if any.
     */
    override fun printException(text: String, details: String?) {
        stopLiveFeedback()
        displayException = true
        // an exception failing the run fails the file it belongs
        // to, whatever its tasks did before
        currentFile?.success = false
        // the section title ends the open output frame of a verbose
        // task itself, so no dotted close ever dangles above it
        detailsPrinted = false

        if (displayResult)
            terminal.println("")
        if (!isDryRunMode) {
            if (entryLineOpen) {
                if (spinArmed) {
                    // the error's landing re-anchors like the
                    // frames do, claiming the zone it overwrites
                    anchorOutcomeZone()
                    spinArmed = false
                }
                // the entry line is still waiting for its outcome:
                // the label lands on it like a result, padded into
                // the reserved space so the word ends flush at the
                // edge like every other outcome
                terminal.println(
                    (terminal.theme.danger + TextStyle(bold = true))(
                        (failureGlyph() + " " +
                            LanguageController.messages.infoLabelOnError)
                            .padStart(
                                successGlyph().length + 1 + longestMatch
                            )
                    )
                )
                entryLineOpen = false
                // the completed entry line separates from the
                // message the way a landed result does
                terminal.println("")
            } else {
                // the label titles its section with the rule the
                // other section titles carry, and the rule itself
                // separates: the message follows directly beneath,
                // with no blank to detach the title from its words
                val head = failureGlyph() + " " +
                    LanguageController.messages.infoLabelOnError
                val fill = (
                    structureWidth - head.length - 1
                    ).coerceAtLeast(1)
                terminal.println(
                    (terminal.theme.danger + TextStyle(bold = true))(head) +
                        muted(" " + rule.repeat(fill))
                )
            }
        }
        // the message pre-wraps at the measure and speaks in the
        // default voice - the title word carries the severity, so
        // the prose never smears the tint a shout would
        text.wrap(structureWidth).lineSequence().forEach { line ->
            terminal.println(line)
        }
        // a present cause earns a details section even when it
        // carries no message of its own
        if (details != null) {
            terminal.println("")
            displayDetailsLine()
            details.wrap(structureWidth).lineSequence().forEach { line ->
                terminal.println(muted(line))
            }
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
        skipCardPrinted = true
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
            StyledTheme.frameGlyph
        } else {
            StyledTheme.frameFallback
        }
        // the wide spacing lets the frame whisper: the boundary
        // holds the stream, the ink never competes with it
        val spaced = "$frame  "
        return spaced.repeat(structureWidth / spaced.length + 1)
            .take(structureWidth).trimEnd()
    }

    /**
     * The glyph that marks a conditionally skipped directive,
     * following the capability.
     */
    private fun skipGlyph(): String =
        if (isDecorated) StyledTheme.skipGlyph else StyledTheme.skipFallback

    /**
     * Displays the provided text wrapped at the readable measure of
     * the presentation.
     *
     * @param text The text to be displayed.
     */
    override fun printWrapped(text: String) {
        terminal.println(text.wrap(structureWidth))
    }

    /**
     * Displays the command a dry run is about to run: the field
     * grammar of the presentation, the muted label naming what is
     * about to happen and the command beside it.
     *
     * @param command The command the dry run simulates.
     */
    override fun printDryRunCommand(command: Command) {
        printField(
            LanguageController.messages.infoLabelAboutToRun,
            command.toString()
        )
    }

    /**
     * Displays the rule authors in the terminal: the field grammar
     * of the presentation, the muted label and the names beside it.
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
        printField(line, text)
    }

    /**
     * Displays the current conditional in the terminal: the field
     * grammar of the presentation, the muted label and the
     * expression beside it.
     *
     * @param conditional The conditional object.
     */
    override fun printConditional(conditional: DirectiveConditional) {
        if (conditional.type !== DirectiveConditionalType.NONE) {
            printField(
                LanguageController.messages.infoLabelConditional,
                "(" + conditional.type + ") " + conditional.condition
            )
        }
    }

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    override fun printTime(seconds: Double) {
        // the closing line separates from the summary unconditionally
        // - one blank, always, whatever came before it
        terminal.println("")

        // the label is chrome and whispers; the value is the payload
        // and speaks - the field grammar of the presentation; the
        // format follows the user's locale, its separator included,
        // and pads to the millisecond
        terminal.println(
            muted(LanguageController.messages.infoDisplayExecutionTime) +
                " " + String.format(
                    Session.userInterfaceOptions.locale.toJVMLocale(),
                    "%.4f", seconds
                ) + " " +
                LanguageController.messages.infoDisplayTimeUnit
        )
    }

    /**
     * Displays a line containing details: the muted rule that names
     * the details section.
     */
    private fun displayDetailsLine() {
        val label = LanguageController.messages.infoLabelOnDetails + " "
        val line = label.abbreviate(structureWidth.coerceAtLeast(2))
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
        if (isDecorated) StyledTheme.runningGlyph else StyledTheme.runningFallback

    /**
     * The glyph that marks a success, following the capability.
     */
    private fun successGlyph(): String =
        if (isDecorated) StyledTheme.successGlyph else StyledTheme.successFallback

    /**
     * The glyph that marks a failure, following the capability.
     */
    private fun failureGlyph(): String =
        if (isDecorated) StyledTheme.failureGlyph else StyledTheme.failureFallback

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
            TextStyle(hyperlink = url)(text)
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
     * Displays the tail of the output a failed command captured:
     * the tool's own explanation of its failure, quoted in the dim
     * bar panel beside the verdict, with the pointer to the full
     * record in the log. A verbose run already streamed the output
     * live, and a command that said nothing has nothing to quote.
     *
     * @param output The captured output of the failed command.
     */
    override fun printCommandFailure(output: String) {
        if (isVerboseMode || output.isBlank()) {
            return
        }
        val bar = if (isDecorated) "│ " else "| "
        val lines = output.trimEnd().lineSequence().toList()
        val truncated = lines.size > PANEL_TAIL_LINES
        val loggingEnabled = Session.loggingOptions.enableLogging
        // the hint titles the panel with the weight every title of
        // the presentation carries, but only when it speaks the
        // truth: the output was actually truncated, and the rest
        // actually lives in the log file the user asked for
        if (truncated && loggingEnabled) {
            terminal.println(
                (muted + TextStyle(bold = true))(
                    bar + LanguageController.messages.infoDisplayFullOutputInLog
                )
            )
        }
        if (truncated) {
            // the cut is stated by the bare ellipsis: the middle is
            // elsewhere, the tail that follows is quoted
            terminal.println(muted(bar + "…"))
        }
        // the quoted lines wrap under the bar with a hanging
        // indent, so every byte of the tool's own words survives
        // while the frame keeps its alignment; the wrapping measure
        // accounts for the bar and the indent of continuation lines
        // alike
        lines.takeLast(PANEL_TAIL_LINES).forEach { line ->
            val wrapped = line.wrap(structureWidth - bar.length * 2)
            wrapped.lineSequence().forEachIndexed { index, part ->
                val indented = if (index == 0) {
                    part
                } else {
                    " ".repeat(bar.length) + part
                }
                terminal.println(muted(bar + indented))
            }
        }
        terminal.println("")
        // the panel quoted and separated the landing itself
        landedOutcome = false
    }

    /**
     * Displays the files that were requested but never reached, when
     * an exception stopped the run before their turn: the running
     * glyph marking the note, the muted label naming what the run
     * left undone, and the file names in normal weight - with the
     * breathing of the presentation around it.
     *
     * @param files The names of the files that were never started.
     */
    override fun printUnprocessedFiles(files: List<String>) {
        terminal.println("")
        terminal.println(
            runningGlyph() + " " +
                muted(
                    LanguageController.messages
                        .infoDisplayUnprocessedFiles(files.size)
                ) +
                files.joinToString(", ")
        )
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
        // the verdict separates from an error it follows, from the
        // skip card that ended a file without a task, and from a
        // landed outcome the run then failed on - a silent failure,
        // a halted run - unless the quoted panel already separated
        // it; a file that completed normally already printed its
        // closing blank through the per-file hook
        val failedLanding = !success && landedOutcome
        if (displayException || skipCardPrinted || failedLanding) {
            terminal.println("")
        }
        skipCardPrinted = false
        landedOutcome = false
        val tasks = summary.sumOf { it.tasks }
        if (isDryRunMode) {
            terminal.println(
                muted(
                    dryGlyph() + " " +
                        LanguageController.messages.infoDisplayDryRun(tasks)
                )
            )
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
        // the project territory state clears with the ledger, so a
        // reused renderer starts every run with a clean slate
        projectCounter = 0
        currentProjectId = -1
        projectDisplayNames.clear()
    }

    /**
     * Renders the execution summary table: one row per file with
     * its task count, its time and its status, the file names
     * linked to the files themselves. Multi-project runs render
     * one grid per project with the project's name above it;
     * single-project runs keep the historic flat table.
     */
    private fun renderSummaryTable() {
        val messages = LanguageController.messages
        val head = "$rule$rule ${messages.infoDisplaySummaryTitle} "
        terminal.println(
            muted(head + rule.repeat((structureWidth - head.length).coerceAtLeast(1)))
        )
        // the section and the table breathe apart; the box belongs
        // to the dialogs, where interruption is the point
        terminal.println("")
        val projectIds = summary
            .mapNotNull { if (it.projectId >= 0) it.projectId else null }
            .distinct()
        if (projectIds.isEmpty()) {
            // single-project: the historic flat table
            terminal.println(buildSummaryTable(summary, messages))
        } else {
            projectIds.forEachIndexed { index, projectId ->
                if (index > 0) {
                    terminal.println("")
                }
                val displayName = projectDisplayNames[projectId] ?: ""
                // the container glyph marks the project's territory
                // through the themed vocabulary, degrading with the
                // stream like every other glyph does
                terminal.println("${runningGlyph()} $displayName")
                terminal.println("")
                val entries = summary.filter { it.projectId == projectId }
                terminal.println(buildSummaryTable(entries, messages))
            }
        }
    }

    /**
     * Builds one grid of the summary table for the provided
     * entries: the ruled layout with the header labels and one
     * row per file.
     */
    private fun buildSummaryTable(
        entries: List<SummaryEntry>,
        messages: org.islandoftex.arara.core.localization.AraraMessages
    ) = table {
        // the table carries the ruled layout of the presentation:
        // the underline beneath the labels, the closing rule beneath
        // the table - the rows flow between them, separated by
        // their alignment rather than by borders, matching the
        // borderless design language of everything else
        tableBorders = Borders.BOTTOM
        cellBorders = Borders.NONE
        header {
            row(
                messages.infoDisplaySummaryFile,
                messages.infoDisplaySummaryTasks,
                messages.infoDisplaySummaryTime,
                ""
            )
            cellBorders = Borders.BOTTOM
        }
        column(FILE_COLUMN) { align = TextAlign.LEFT }
        column(COUNT_COLUMN) { align = TextAlign.RIGHT }
        column(TIME_COLUMN) { align = TextAlign.RIGHT }
        column(STATUS_COLUMN) { align = TextAlign.RIGHT }
        body {
            entries.forEach { entry ->
                row(
                    hyperlink(entry.name, entry.path),
                    entry.tasks,
                    formatSeconds(entry.seconds),
                    if (entry.success) {
                        terminal.theme.success(successGlyph())
                    } else {
                        terminal.theme.danger(failureGlyph())
                    }
                )
            }
        }
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
        if (isDecorated) StyledTheme.dryGlyph else StyledTheme.dryFallback
}
