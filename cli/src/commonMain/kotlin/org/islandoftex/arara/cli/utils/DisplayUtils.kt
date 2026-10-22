// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.cli.console.ConsoleRenderer
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session
import kotlin.math.ln
import kotlin.math.pow

/** Stable display entry points with presentation-independent logging. */
@Suppress("TooManyFunctions")
object DisplayUtils {
    var renderer: ConsoleRenderer = PlainConsoleRenderer()
    internal var outcomes = linkedMapOf<String, Int?>()
    internal var currentFile: String? = null

    var configurationFileName: String = "[none]"
    val logoString: String = """
              __ _ _ __ __ _ _ __ __ _
             / _` | '__/ _` | '__/ _` |
            | (_| | | | (_| | | | (_| |
             \__,_|_|  \__,_|_|  \__,_|
    """.trimIndent()

    internal fun prepareFiles(files: List<ProjectFile>) {
        outcomes = files.associate { it.path.toString() to null }.toMap(LinkedHashMap())
        currentFile = null
    }

    fun printFileResult(exitCode: Int) {
        currentFile?.let { outcomes[it] = exitCode }
        currentFile = null
    }

    fun printRunSummary(success: Boolean) = renderer.printRunSummary(success, outcomes.toList())

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

    fun printBanner() = renderer.printBanner()

    fun printFileInformation(file: ProjectFile) {
        currentFile = file.path.toString()
        renderer.printFileInformation(file)
    }

    fun printWrapped(text: String) = renderer.printWrapped(text)
    fun printAuthors(authors: List<String>) = renderer.printAuthors(authors)
    fun printConditional(conditional: DirectiveConditional) = renderer.printConditional(conditional)

    fun printEntry(name: String, task: String) {
        RunLog.entry(name, task)
        renderer.printEntry(name, task)
    }

    fun printEntryResult(value: Boolean) {
        RunLog.taskResult(value)
        renderer.printEntryResult(value)
    }

    fun printException(exception: AraraException) {
        currentFile?.let { outcomes[it] = 2 }
        val text = (exception.message ?: "EXCEPTION PROVIDES NO MESSAGE") +
            if (exception.hasException()) " " + LanguageController.messages.infoDisplayExceptionMoreDetails else ""
        val details = exception.exception?.let { it.message ?: it.toString() }
        RunLog.exception(exception.message, details, exception.exception ?: exception)
        renderer.printException(text, details)
    }

    fun printTime(seconds: Double) {
        RunLog.executionTime(seconds)
        renderer.printTime(seconds)
    }
}
