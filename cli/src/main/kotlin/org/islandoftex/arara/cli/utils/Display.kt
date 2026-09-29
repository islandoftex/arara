// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.ln
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.configuration.DefaultConfiguration
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.utils.wrap

/**
 * Implements display utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Suppress("TooManyFunctions")
object Display {
    private val logger = KotlinLogging.logger { }

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
            LanguageController.messages.infoLabelOnSuccess,
            LanguageController.messages.infoLabelOnFailure,
            LanguageController.messages.infoLabelOnError
        ).map { it.length }.maxOrNull()!!
    }

    /**
     * If the longest match is longer than the width, then it will be truncated
     * to this length.
     */
    private const val shortenedLongestMatch = 10

    /**
     * The minimum entry width a degenerate terminal width degrades
     * to, so the abbreviation never runs out of space and the
     * render degrades instead of crashing.
     */
    private const val minimumEntryWidth = 6

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
    internal val applicationPath: String
        get() = try {
            DefaultConfiguration.applicationPath.toString()
        } catch (_: AraraException) {
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
        // a degenerate width has to degrade to an ugly entry, never
        // crash the render: the abbreviation keeps its minimum and
        // the padding simply wraps
        val space = (outputWidth - result - 1)
            .coerceAtLeast(minimumEntryWidth)
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

        logger.info {
            // a fixed English token keeps the outcome greppable across
            // every language and every future rewording of the labels
            "Task result: " + if (value) "SUCCESS" else "FAILURE"
        }
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
        logger.info {
            "I am ready to interpret task \"$task\" from rule \"$name\"."
        }
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
        // the abbreviation keeps its minimum so a degenerate width
        // wraps ugly instead of crashing the render
        println(
            "($name) $task"
                .abbreviate(outputWidth.coerceAtLeast(minimumEntryWidth))
        )
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
                        LanguageController.messages.infoLabelOnError.padStart(longestMatch)
                    } else {
                        (" " + LanguageController.messages.infoLabelOnError)
                            .padStart(outputWidth, '-')
                    }
                )
                println()
            }
        }
        val underlying = exception.exception
        val text = (
            if (underlying != null)
                exception.message + " " + LanguageController.messages
                    .infoDisplayExceptionMoreDetails
            else
                exception.message
            ) ?: "EXCEPTION PROVIDES NO MESSAGE"
        logger.error { text }
        printWrapped(text)
        // a present cause earns a details section even when it carries no
        // message of its own
        if (underlying != null) {
            println()
            displayDetailsLine()
            val details = underlying.message ?: underlying.toString()
            logger.error { details }
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
            LanguageController.messages.infoLabelOnSuccess
        else
            LanguageController.messages.infoLabelOnFailure
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
    fun printConditional(conditional: DirectiveConditional) {
        if (conditional.type !== DirectiveConditionalType.NONE) {
            printWrapped(
                LanguageController.messages.infoLabelConditional +
                    " (" + conditional.type + ") " +
                    conditional.condition
            )
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
                val baseSize = size / conversionFactor.pow(exp.toDouble())
                val ones = baseSize.toInt()
                val tenths = ((baseSize - ones) * 10).toInt()
                val separator = Session.userInterfaceOptions
                    .locale.decimalSeparator.toString()
                "$ones$separator$tenths " +
                    "${"kMGTPE"[exp - 1]}B"
            }
    }

    /**
     * Displays the elapsed time in the terminal.
     *
     * @param seconds The elapsed seconds.
     */
    fun printTime(seconds: Double) {
        if (displayLine || displayException)
            println()

        val secondDuration = seconds.seconds
        val separator = Session.userInterfaceOptions.locale
            .decimalSeparator.toString()
        val time = secondDuration.inWholeSeconds.toString() + separator +
            (secondDuration - secondDuration.inWholeSeconds.seconds)
                .inWholeMilliseconds.toString()
        val text = LanguageController.messages.infoDisplayExecutionTime(time)
        // the console line stays localized; the log carries an English
        // twin so the file reads the same in every language
        logger.info {
            "Total execution time: $seconds seconds"
        }
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

    /**
     * Displays the file information in the terminal: the welcome banner
     * and the file line, with the log carrying an English twin of the
     * localized console line.
     */
    fun printFileInformation(file: ProjectFile) {
        logger.info {
            "\n" + "Welcome to arara ${AraraAPI.version}!" + "\n" +
                displaySeparator()
        }
        logEnvironmentDetails()
        LanguageController.messages
            .infoDisplayFileInformation(
                file.path.fileName,
                byteSizeToString(file.path.fileSize),
                file.path.lastModified.toString()
            ).let {
                logger.info {
                    "Processing \"${file.path.fileName}\" (size: " +
                        "${byteSizeToString(file.path.fileSize)}, " +
                        "last modified: ${file.path.lastModified}), " +
                        "please wait."
                }
                printWrapped(it)
                println()
            }
    }

    /**
     * Writes the environment block of the run to the log at debug level:
     * the Java runtime, the operating system, the user home and the
     * configuration file name.
     */
    private fun logEnvironmentDetails() {
        val javaVersion = Environment.getSystemProperty(
            "java.version",
            "[unknown version]"
        )
        val javaVendor = Environment.getSystemProperty(
            "java.vendor",
            "[unknown vendor]"
        )
        val javaHome = Environment.getSystemProperty(
            "java.home",
            "[unknown location]"
        )
        val osName = Environment.getSystemProperty(
            "os.name",
            "[unknown OS name]"
        )
        val osArch = Environment.getSystemProperty(
            "os.arch",
            "[unknown OS arch]"
        )
        val osVersion = Environment.getSystemProperty(
            "os.version",
            "[unknown OS version]"
        )
        val userHome = Environment.getSystemProperty(
            "user.home",
            "[unknown user's home directory]"
        )
        logger.debug {
            """

                ::: arara @ ${DefaultConfiguration.applicationPath}
                ::: Java $javaVersion, $javaVendor
                ::: $javaHome
                ::: $osName, $osArch, $osVersion
                ::: user.home @ $userHome
                ::: CF @ ${configurationFileName}
                ${displaySeparator()}
            """.trimIndent()
        }
    }
}
