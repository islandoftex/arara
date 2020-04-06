// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.configuration.ConfigurationUtils
import org.islandoftex.arara.cli.filehandling.FileHandlingUtils
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.slf4j.LoggerFactory

/**
 * Implements display utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object DisplayUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(DisplayUtils::class.java)

    /**
     * The length of the longest result match as integer.
     */
    private val longestMatch: Int = listOf(
            messages.getMessage(Messages.INFO_LABEL_ON_SUCCESS),
            messages.getMessage(Messages.INFO_LABEL_ON_FAILURE),
            messages.getMessage(Messages.INFO_LABEL_ON_ERROR))
            .map { it.length }.max()!!
    /**
     * If the longest match is longer than the width, then it will be truncated
     * to this length.
     */
    private const val shortenedLongestMatch = 10

    /**
     * The default terminal width defined in the settings.
     */
    private val width: Int
        get() = Arara.config[AraraSpec.Application.width]

    /**
     * Checks if the execution is in dry-run mode.
     */
    private val isDryRunMode: Boolean
        get() = Arara.config[AraraSpec.Execution.dryrun]

    /**
     * Checks if the execution is in verbose mode.
     */
    private val isVerboseMode: Boolean
        get() = Arara.config[AraraSpec.Execution.verbose]

    /**
     * The application path.
     */
    private val applicationPath: String
        get() = try {
            ConfigurationUtils.applicationPath.toString()
        } catch (ae: AraraException) {
            "[unknown application path]"
        }

    /**
     * Displays the short version of the current entry in the terminal.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    private fun buildShortEntry(name: String, task: String) {
        val result = if (longestMatch >= width)
            shortenedLongestMatch
        else
            longestMatch
        val space = width - result - 1
        val line = "($name) $task ".abbreviate(space - "... ".length)
        print(line.padEnd(space, '.') + " ")
    }

    /**
     * Displays the short version of the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    private fun buildShortResult(value: Boolean) {
        val result = longestMatch
        println(getResult(value).padStart(result))
    }

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    fun printEntryResult(value: Boolean) {
        Arara.config[AraraSpec.UserInteraction.displayLine] = false
        Arara.config[AraraSpec.UserInteraction.displayResult] = true
        Arara.config[AraraSpec.Execution.status] = if (value) 0 else 1
        logger.info(
                messages.getMessage(
                        Messages.LOG_INFO_TASK_RESULT
                ) + " " + getResult(value)
        )
        if (!isDryRunMode) {
            if (!isVerboseMode) {
                buildShortResult(value)
            } else {
                buildLongResult(value)
            }
        }
    }

    /**
     * Displays a long version of the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed
     */
    private fun buildLongResult(value: Boolean) {
        val width = width
        println("\n" + (" " + getResult(value)).padStart(width, '-'))
    }

    /**
     * Displays the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    fun printEntry(name: String, task: String) {
        logger.info(
                messages.getMessage(
                        Messages.LOG_INFO_INTERPRET_TASK,
                        task,
                        name
                )
        )
        Arara.config[AraraSpec.UserInteraction.displayLine] = true
        Arara.config[AraraSpec.UserInteraction.displayResult] = false
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
        if (Arara.config[AraraSpec.UserInteraction.displayRolling]) {
            addNewLine()
        } else {
            Arara.config[AraraSpec.UserInteraction.displayRolling] = true
        }
        println(displaySeparator())
        println("($name) $task".abbreviate(width))
        println(displaySeparator())
    }

    /**
     * Displays a dry-run version of the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    private fun buildDryRunEntry(name: String, task: String) {
        if (Arara.config[AraraSpec.UserInteraction.displayRolling]) {
            addNewLine()
        } else {
            Arara.config[AraraSpec.UserInteraction.displayRolling] = true
        }
        println("[DR] ($name) $task".abbreviate(width))
        println(displaySeparator())
    }

    /**
     * Displays the exception in the terminal.
     *
     * @param exception The exception object.
     */
    fun printException(exception: AraraException) {
        Arara.config[AraraSpec.UserInteraction.displayException] = true
        Arara.config[AraraSpec.Execution.status] = 2

        val display = Arara.config[AraraSpec.UserInteraction.displayLine]
        if (Arara.config[AraraSpec.UserInteraction.displayResult])
            addNewLine()
        if (display) {
            if (!isDryRunMode) {
                if (!isVerboseMode) {
                    buildShortError()
                } else {
                    buildLongError()
                }
                addNewLine()
            }
        }
        val text = (if (exception.hasException())
            exception.message + " " + messages.getMessage(
                    Messages.INFO_DISPLAY_EXCEPTION_MORE_DETAILS)
        else
            exception.message) ?: "EXCEPTION PROVIDES NO MESSAGE"
        // TODO: check null handling
        logger.error(text)
        wrapText(text)
        if (exception.hasException()) {
            addNewLine()
            displayDetailsLine()
            val details = exception.exception!!.message!!
            logger.error(details)
            wrapText(details)
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
            messages.getMessage(
                    Messages.INFO_LABEL_ON_SUCCESS
            )
        else
            messages.getMessage(Messages.INFO_LABEL_ON_FAILURE)
    }

    /**
     * Displays the short version of an error in the terminal.
     */
    private fun buildShortError() {
        val result = longestMatch
        println(messages.getMessage(Messages.INFO_LABEL_ON_ERROR)
                .padStart(result))
    }

    /**
     * Displays the long version of an error in the terminal.
     */
    private fun buildLongError() {
        println((" " + messages.getMessage(Messages.INFO_LABEL_ON_ERROR))
                .padStart(width, '-'))
    }

    /**
     * Displays the provided text wrapped nicely according to the default
     * terminal width.
     *
     * @param text The text to be displayed.
     */
    fun wrapText(text: String) = println(text.wrap(width))

    /**
     * Displays the rule authors in the terminal.
     *
     * @param authors The list of authors.
     */
    fun printAuthors(authors: List<String>) {
        val line = if (authors.size == 1)
            messages.getMessage(Messages.INFO_LABEL_AUTHOR)
        else
            messages.getMessage(Messages.INFO_LABEL_AUTHORS)
        val text = if (authors.isEmpty())
            messages.getMessage(Messages.INFO_LABEL_NO_AUTHORS)
        else
            authors.joinToString(", ") { it.trim() }
        wrapText("$line $text")
    }

    /**
     * Displays the current conditional in the terminal.
     *
     * @param conditional The conditional object.
     */
    fun printConditional(conditional: DirectiveConditional) {
        if (conditional.type !== DirectiveConditionalType.NONE) {
            wrapText(messages.getMessage(Messages.INFO_LABEL_CONDITIONAL) +
                    " (" + conditional.type + ") " +
                    conditional.condition)
        }
    }

    /**
     * Displays the file information in the terminal.
     */
    fun printFileInformation() {
        val file = Arara.config[AraraSpec.Execution.reference]
        val version = Arara.config[AraraSpec.Application.version]
        val line = messages.getMessage(
                Messages.INFO_DISPLAY_FILE_INFORMATION,
                file.name,
                CommonUtils.byteSizeToString(file.length()),
                FileHandlingUtils.getLastModifiedInformation(file)
        )
        logger.info(messages.getMessage(
                Messages.LOG_INFO_WELCOME_MESSAGE,
                version
        ))
        logger.info(displaySeparator())
        logger.debug("::: arara @ $applicationPath")
        logger.debug("::: Java %s, %s".format(
                CommonUtils.getSystemProperty("java.version",
                        "[unknown version]"),
                CommonUtils.getSystemProperty("java.vendor",
                        "[unknown vendor]")
        ))
        logger.debug("::: %s".format(
                CommonUtils.getSystemProperty("java.home",
                        "[unknown location]")
        ))
        logger.debug("::: %s, %s, %s".format(
                CommonUtils.getSystemProperty("os.name",
                        "[unknown OS name]"),
                CommonUtils.getSystemProperty("os.arch",
                        "[unknown OS arch]"),
                CommonUtils.getSystemProperty("os.version",
                        "[unknown OS version]")
        ))
        logger.debug("::: user.home @ %s".format(
                CommonUtils.getSystemProperty("user.home",
                        "[unknown user's home directory]")
        ))
        logger.debug("::: CF @ %s".format(Arara.config[AraraSpec.Execution
                .configurationName]))
        logger.debug(displaySeparator())
        logger.info(line)
        wrapText(line)
        addNewLine()
    }

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    fun printTime(seconds: Double) {
        val language = Arara.config[AraraSpec.Execution.language]

        if (Arara.config[AraraSpec.UserInteraction.displayTime]) {
            if (Arara.config[AraraSpec.UserInteraction.displayLine] ||
                    Arara.config[AraraSpec.UserInteraction.displayException])
                addNewLine()

            val text = messages.getMessage(
                    Messages.INFO_DISPLAY_EXECUTION_TIME,
                    "%1.2f".format(language.locale, seconds))
            logger.info(text)
            wrapText(text)
        }
    }

    /**
     * Displays the application logo in the terminal.
     */
    fun printLogo() {
        println("""
              __ _ _ __ __ _ _ __ __ _
             / _` | '__/ _` | '__/ _` |
            | (_| | | | (_| | | | (_| |
             \__,_|_|  \__,_|_|  \__,_|
        """.trimIndent())
        addNewLine()
    }

    /**
     * Adds a new line in the terminal.
     */
    private fun addNewLine() {
        println()
    }

    /**
     * Displays a line containing details.
     */
    private fun displayDetailsLine() {
        val line = messages.getMessage(
                Messages.INFO_LABEL_ON_DETAILS) + " "
        println(line.abbreviate(width).padEnd(width, '-'))
    }

    /**
     * Gets the output separator with the provided text.
     *
     * @param message The provided text.
     * @return A string containing the output separator with the provided text.
     */
    fun displayOutputSeparator(message: String): String {
        return " $message ".center(width, '-')
    }

    /**
     * Gets the line separator.
     *
     * @return A string containing the line separator.
     */
    fun displaySeparator(): String {
        return "-".repeat(width)
    }
}
