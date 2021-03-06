// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.text.SimpleDateFormat
import kotlin.math.ln
import kotlin.math.pow
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.slf4j.LoggerFactory

/**
 * Implements display utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object DisplayUtils {
    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(DisplayUtils::class.java)

    /**
     * The application logo for the terminal.
     */
    val logoString: String = """
              __ _ _ __ __ _ _ __ __ _
             / _` | '__/ _` | '__/ _` |
            | (_| | | | (_| | | | (_| |
             \__,_|_|  \__,_|_|  \__,_|
    """.trimIndent()

    /**
     * The file name of the configuration file in use. It is only needed for
     * logging purposes.
     */
    var configurationFileName: String = "[none]"

    /**
     * The length of the longest result match as integer.
     */
    private val longestMatch: Int by lazy {
        // lazy computation to wait for the language to be initialized
        listOf(
            LanguageController.messages.INFO_LABEL_ON_SUCCESS,
            LanguageController.messages.INFO_LABEL_ON_FAILURE,
            LanguageController.messages.INFO_LABEL_ON_ERROR
        ).map { it.length }.maxOrNull()!!
    }

    /**
     * If the longest match is longer than the width, then it will be truncated
     * to this length.
     */
    private const val shortenedLongestMatch = 10

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
     * The application path.
     */
    private val applicationPath: String
        get() = try {
            ConfigurationUtils.applicationPath.toString()
        } catch (ae: AraraException) {
            "[unknown application path]"
        }

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
        val space = outputWidth - result - 1
        val line = "($name) $task ".abbreviate(space - "... ".length)
        print(line.padEnd(space, '.') + " ")
    }

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    fun printEntryResult(value: Boolean) {
        displayLine = false
        displayResult = true

        logger.info(
                LanguageController.messages.LOG_INFO_TASK_RESULT + " " +
                        getResult(value)
        )
        if (!isDryRunMode) {
            println(
                    if (!isVerboseMode) {
                        getResult(value).padStart(longestMatch)
                    } else {
                        "\n" + (" " + getResult(value))
                                .padStart(outputWidth, '-')
                    }
            )
        }
    }

    /**
     * Displays the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    fun printEntry(name: String, task: String) {
        logger.info(
                LanguageController.messages.LOG_INFO_INTERPRET_TASK
                        .format(task, name)
        )
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
        println("($name) $task"
                .abbreviate(outputWidth))
        println(displaySeparator())
    }

    /**
     * Displays a dry-run version of the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
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
     * Displays the exception in the terminal.
     *
     * @param exception The exception object.
     */
    fun printException(exception: AraraException) {
        displayException = true

        if (displayResult)
            println()
        if (displayLine) {
            if (!isDryRunMode) {
                println(
                        if (!isVerboseMode) {
                            LanguageController.messages.INFO_LABEL_ON_ERROR.padStart(longestMatch)
                        } else {
                            (" " + LanguageController.messages.INFO_LABEL_ON_ERROR)
                                    .padStart(outputWidth, '-')
                        }
                )
                println()
            }
        }
        val text = (if (exception.hasException())
            exception.message + " " + LanguageController.messages
                    .INFO_DISPLAY_EXCEPTION_MORE_DETAILS
        else
            exception.message) ?: "EXCEPTION PROVIDES NO MESSAGE"
        // TODO: check null handling
        logger.error(text)
        printWrapped(text)
        if (exception.hasException()) {
            println()
            displayDetailsLine()
            val details = exception.exception!!.message!!
            logger.error(details)
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
            LanguageController.messages.INFO_LABEL_ON_SUCCESS
        else
            LanguageController.messages.INFO_LABEL_ON_FAILURE
    }

    /**
     * Displays the provided text wrapped nicely according to the default
     * terminal width.
     *
     * @param text The text to be displayed.
     */
    fun printWrapped(text: String) = println(text.wrap(outputWidth))

    /**
     * Displays the rule authors in the terminal.
     *
     * @param authors The list of authors.
     */
    fun printAuthors(authors: List<String>) {
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

    /**
     * Displays the current conditional in the terminal.
     *
     * @param conditional The conditional object.
     */
    fun printConditional(conditional: DirectiveConditional) {
        if (conditional.type !== DirectiveConditionalType.NONE) {
            printWrapped(LanguageController.messages.INFO_LABEL_CONDITIONAL +
                    " (" + conditional.type + ") " +
                    conditional.condition)
        }
    }

    /**
     * Gets a human readable representation of a size.
     *
     * @param size The byte size to be converted.
     * @return A string representation of the size.
     */
    @Suppress("MagicNumber")
    internal fun byteSizeToString(size: Long): String {
        val conversionFactor = 1000.0
        return if (size < conversionFactor) "$size B"
        else
            (ln(size.toDouble()) / ln(conversionFactor)).toInt().let { exp ->
                "%.1f %sB".format(
                        Session.userInterfaceOptions.locale,
                        size / conversionFactor.pow(exp.toDouble()),
                        "kMGTPE"[exp - 1]
                )
            }
    }

    /**
     * Displays the file information in the terminal.
     */
    fun printFileInformation(projectFile: ProjectFile) {
        val file = projectFile.path.toFile()
        val line = LanguageController.messages.INFO_DISPLAY_FILE_INFORMATION
                .format(
                        file.name,
                        byteSizeToString(file.length()),
                        SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                                .format(file.lastModified())
                )
        logger.info(LanguageController.messages.LOG_INFO_WELCOME_MESSAGE
                .format(AraraAPI.version))
        logger.info(displaySeparator())
        logger.debug("::: arara @ $applicationPath")
        logger.debug("::: Java %s, %s".format(
                Environment.getSystemProperty("java.version",
                        "[unknown version]"),
                Environment.getSystemProperty("java.vendor",
                        "[unknown vendor]")
        ))
        logger.debug("::: %s".format(
                Environment.getSystemProperty("java.home",
                        "[unknown location]")
        ))
        logger.debug("::: %s, %s, %s".format(
                Environment.getSystemProperty("os.name",
                        "[unknown OS name]"),
                Environment.getSystemProperty("os.arch",
                        "[unknown OS arch]"),
                Environment.getSystemProperty("os.version",
                        "[unknown OS version]")
        ))
        logger.debug("::: user.home @ %s".format(
                Environment.getSystemProperty("user.home",
                        "[unknown user's home directory]")
        ))
        logger.debug("::: CF @ %s".format(configurationFileName))
        logger.debug(displaySeparator())
        logger.info(line)
        printWrapped(line)
        println()
    }

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    fun printTime(seconds: Double) {
        if (displayLine || displayException)
            println()

        val text = LanguageController.messages.INFO_DISPLAY_EXECUTION_TIME
                .format(
                        "%1.2f".format(
                                Session.userInterfaceOptions.locale,
                                seconds
                        )
                )
        logger.info(text)
        printWrapped(text)
    }

    /**
     * Displays a line containing details.
     */
    private fun displayDetailsLine() = println(
            (LanguageController.messages.INFO_LABEL_ON_DETAILS + " ")
                    .abbreviate(outputWidth)
                    .padEnd(outputWidth, '-')
    )

    /**
     * Gets the output separator with the provided text.
     *
     * @param message The provided text.
     * @return A string containing the output separator with the provided text.
     */
    fun displayOutputSeparator(message: String): String =
        " $message ".center(outputWidth, '-')

    /**
     * Gets the line separator.
     *
     * @return A string containing the line separator.
     */
    fun displaySeparator(): String = "-".repeat(outputWidth)
}
