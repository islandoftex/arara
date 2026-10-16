// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.math.ln
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.console.RunPresentation
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/** The generated version object belongs to the platform source set. */
internal expect fun applicationVersion(): String

/** Stable display entry points with presentation-independent logging. */
@Suppress("TooManyFunctions")
object DisplayUtils {
    private const val MILLISECOND_DIGITS = 3

    /** Active terminal renderer; log output remains independent of this choice. */
    var renderer: ConsoleRenderer = PlainConsoleRenderer()
    internal var presentation = RunPresentation()

    /** Selected configuration filename shown in diagnostics. */
    var configurationFileName: String = "[none]"
    /** Classic ASCII application logo. */
    val logoString: String = """
              __ _ _ __ __ _ _ __ __ _
             / _` | '__/ _` | '__/ _` |
            | (_| | | | (_| | | | (_| |
             \__,_|_|  \__,_|_|  \__,_|
    """.trimIndent()

    /** Version greeting based on the generated application version. */
    val versionLine: String = "arara ${applicationVersion()}, the cool TeX automation tool."

    internal fun elapsedSeconds(value: Double): String {
        val duration = value.seconds
        val whole = duration.inWholeSeconds
        val milliseconds = (duration - whole.seconds).inWholeMilliseconds.toString().padStart(MILLISECOND_DIGITS, '0')
        return "$whole${Session.userInterfaceOptions.locale.decimalSeparator}$milliseconds"
    }

    /** Finish the current file in the run ledger; the final summary renders its status. */
    fun printFileResult(exitCode: Int) = presentation.finish(exitCode)

    /** Present the retained invocation ledger with the aggregate success flag. */
    fun printRunSummary(success: Boolean) {
        renderer.printRunSummary(presentation.summary(
            success, LinearExecutor.executionOptions.executionMode == ExecutionMode.DRY_RUN
        ))
        if (presentation.deprecatedYamlUsed) renderer.printWrapped(LanguageController.messages.warningDeprecatedYaml)
    }

    /** Retain compatibility warnings across files and project configuration changes. */
    fun deprecatedYamlUsed() = presentation.deprecatedYaml()

    /** Count a conditional skip and present it through the active renderer. */
    fun printSkippedDirective(identifier: String) {
        presentation.skip()
        renderer.printSkippedDirective(identifier)
    }

    @Suppress("MagicNumber")
    internal fun byteSizeToString(size: Long): String {
        val conversionFactor = 1000.0
        return if (size < conversionFactor) "$size B"
        else
            (ln(size.toDouble()) / ln(conversionFactor)).toInt().let { exp ->
                val baseSize = size / conversionFactor.pow(exp.toDouble())
                val ones = baseSize.toInt()
                val tenths = ((baseSize - ones) * 10).toInt()
                val separator = Session.userInterfaceOptions.locale.decimalSeparator
                val prefix = "kMGTPE"[exp - 1]
                "$ones$separator$tenths ${prefix}B"
            }
    }

    /** Print the application greeting through the active renderer. */
    fun printBanner() = renderer.printBanner()

    /** Start file accounting and present its metadata. */
    fun printFileInformation(file: ProjectFile) {
        presentation.start(file, LinearExecutor.executionOptions.executionMode == ExecutionMode.DRY_RUN)
        renderer.printFileInformation(file)
    }

    /** Print wrapped prose through the active renderer. */
    fun printWrapped(text: String) = renderer.printWrapped(text)
    /** Present rule authors through the active renderer. */
    fun printAuthors(authors: List<String>) = renderer.printAuthors(authors)
    /** Present a directive conditional through the active renderer. */
    fun printConditional(conditional: DirectiveConditional) = renderer.printConditional(conditional)

    /** Count and log a task, then start its terminal presentation. */
    fun printEntry(name: String, task: String) {
        presentation.task()
        RunLog.entry(name, task)
        renderer.printEntry(name, task)
    }

    /** Log a task result and finish its terminal presentation. */
    fun printEntryResult(value: Boolean) {
        RunLog.taskResult(value)
        renderer.printEntryResult(value)
    }

    /** Record an application error, finish file accounting with status 2 and present cause details. */
    fun printException(exception: AraraException) {
        presentation.finish(2)
        val text = (exception.message ?: "EXCEPTION PROVIDES NO MESSAGE") +
            if (exception.hasException()) " " + LanguageController.messages.infoDisplayExceptionMoreDetails else ""
        val details = exception.exception?.let { it.message ?: it.toString() }
        RunLog.exception(exception.message, details, exception.exception ?: exception)
        renderer.printException(text, details)
    }

    /** Log and present elapsed time in seconds. */
    fun printTime(seconds: Double) {
        RunLog.executionTime(seconds)
        renderer.printTime(seconds)
    }
}
