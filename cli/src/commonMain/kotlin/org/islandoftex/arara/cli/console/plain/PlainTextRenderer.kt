// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.cli.console.ConsoleText
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.abbreviate
import org.islandoftex.arara.core.localization.LanguageController

/** Focused plain presentation component. */
internal class PlainTextRenderer(state: PlainRenderState) : PlainComponent(state), ConsoleText {
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

    override fun printFileInformation(file: ProjectFile) {
        printWrapped(LanguageController.messages.infoDisplayFileInformation(
            file.path.fileName, DisplayUtils.byteSizeToString(file.sourcePath.fileSize),
            file.sourcePath.lastModified.toString()
        ))
        println()
    }
}
