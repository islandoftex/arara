/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.utils

import com.github.cereda.arara.controller.ConfigurationController
import com.github.cereda.arara.controller.LanguageController
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.model.Conditional
import com.github.cereda.arara.model.Messages
import com.github.cereda.arara.model.StopWatch
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.WordUtils
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Implements display utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
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
     * The default terminal width defined in the settings.
     */
    private val width: Int
        get() = ConfigurationController["application.width"] as Int

    /**
     * Checks if the execution is in dry-run mode.
     */
    private val isDryRunMode: Boolean
        get() = ConfigurationController["execution.dryrun"] as Boolean

    /**
     * Checks if the execution is in verbose mode.
     */
    private val isVerboseMode: Boolean
        get() = ConfigurationController["execution.verbose"] as Boolean

    /**
     * The application path.
     */
    private val applicationPath: String
        get() = try {
            ConfigurationUtils.applicationPath
        } catch (ae: AraraException) {
            "[unknown application path]"
        }

    /**
     * Displays the short version of the current entry in the terminal.
     *
     * @param name Rule name.
     * @param task Task name.
     */
    // TODO: refactor
    private fun buildShortEntry(name: String, task: String) {
        val width = width
        var result = longestMatch
        if (result >= width) {
            result = 10
        }
        val space = width - result - 1
        var entry = StringBuilder()
        entry.append("(").append(name).append(") ")
        entry.append(task).append(" ")
        val line = StringUtils.abbreviate(entry.toString(), space - 4)
        entry = StringBuilder()
        entry.append(StringUtils.rightPad(line, space, ".")).append(" ")
        print(entry)
    }

    /**
     * Displays the short version of the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    private fun buildShortResult(value: Boolean) {
        val result = longestMatch
        println(StringUtils.leftPad(getResult(value), result))
    }

    /**
     * Displays the current entry result in the terminal.
     *
     * @param value The boolean value to be displayed.
     */
    fun printEntryResult(value: Boolean) {
        ConfigurationController.put("display.line", false)
        ConfigurationController.put("display.result", true)
        ConfigurationController
                .put("execution.status", if (value) 0 else 1)
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
        println("\n" + StringUtils.leftPad(
                " " + getResult(value), width, "-"
        ))
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
        ConfigurationController.put("display.line", true)
        ConfigurationController.put("display.result", false)
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
        if (ConfigurationController.contains("display.rolling")) {
            addNewLine()
        } else {
            ConfigurationController.put("display.rolling", true)
        }
        println(displaySeparator())
        println(StringUtils.abbreviate("(" + name + ") " +
                task, width))
        println(displaySeparator())
    }

    /**
     * Displays a dry-run version of the current entry in the terminal.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    private fun buildDryRunEntry(name: String, task: String) {
        if (ConfigurationController.contains("display.rolling")) {
            addNewLine()
        } else {
            ConfigurationController.put("display.rolling", true)
        }
        println(StringUtils.abbreviate("[DR] (" + name + ") " +
                task, width))
        println(displaySeparator())
    }

    /**
     * Displays the exception in the terminal.
     *
     * @param exception The exception object.
     */
    fun printException(exception: AraraException) {
        ConfigurationController.put("display.exception", true)
        ConfigurationController.put("execution.status", 2)
        var display = false
        if (ConfigurationController.contains("display.line")) {
            display = ConfigurationController["display.line"] as Boolean
        }
        if (ConfigurationController.contains("display.result")) {
            if (ConfigurationController["display.result"] as Boolean) {
                addNewLine()
            }
        }
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
                    Messages.INFO_DISPLAY_EXCEPTION_MORE_DETAILS
            )
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
        println(StringUtils.leftPad(
                messages.getMessage(
                        Messages.INFO_LABEL_ON_ERROR
                ),
                result
        ))
    }

    /**
     * Displays the long version of an error in the terminal.
     */
    private fun buildLongError() {
        val line = StringUtils.leftPad(
                " " + messages.getMessage(Messages.INFO_LABEL_ON_ERROR),
                width, "-")
        println(line)
    }

    /**
     * Displays the provided text wrapped nicely according to the default
     * terminal width.
     *
     * @param text The text to be displayed.
     */
    fun wrapText(text: String) {
        println(WordUtils.wrap(text, width))
    }

    /**
     * Displays the rule authors in the terminal.
     *
     * @param authors The list of authors.
     */
    fun printAuthors(authors: List<String>) {
        val line = StringBuilder()
        line.append(if (authors.size == 1)
            messages.getMessage(Messages.INFO_LABEL_AUTHOR)
        else
            messages.getMessage(Messages.INFO_LABEL_AUTHORS))
        val text = if (authors.isEmpty())
            messages.getMessage(Messages.INFO_LABEL_NO_AUTHORS)
        else
            authors.map { it.trim() }.joinToString(", ")
        line.append(" ").append(text)
        wrapText(line.toString())
    }

    /**
     * Displays the current conditional in the terminal.
     *
     * @param conditional The conditional object.
     */
    fun printConditional(conditional: Conditional) {
        if (conditional.type !== Conditional.ConditionalType.NONE) {
            wrapText(messages.getMessage(Messages.INFO_LABEL_CONDITIONAL) +
                    " (" + conditional.type + ") " +
                    conditional.condition)
        }
    }

    /**
     * Displays the file information in the terminal.
     */
    fun printFileInformation() {
        val file = ConfigurationController["execution.reference"] as File
        val version = ConfigurationController["application.version"] as String
        val revision = ConfigurationController["application.revision"] as String
        val line = messages.getMessage(
                Messages.INFO_DISPLAY_FILE_INFORMATION,
                file.name,
                CommonUtils.calculateFileSize(file),
                CommonUtils.getLastModifiedInformation(file)
        )
        logger.info(messages.getMessage(
                Messages.LOG_INFO_WELCOME_MESSAGE,
                version,
                revision
        ))
        logger.info(displaySeparator())
        logger.info(String.format("::: arara @ %s",
                applicationPath
        ))
        logger.info(String.format("::: Java %s, %s",
                CommonUtils.getSystemProperty("java.version",
                        "[unknown version]"),
                CommonUtils.getSystemProperty("java.vendor",
                        "[unknown vendor]")
        ))
        logger.info(String.format("::: %s",
                CommonUtils.getSystemProperty("java.home",
                        "[unknown location]")
        ))
        logger.info(String.format("::: %s, %s, %s",
                CommonUtils.getSystemProperty("os.name",
                        "[unknown OS name]"),
                CommonUtils.getSystemProperty("os.arch",
                        "[unknown OS arch]"),
                CommonUtils.getSystemProperty("os.version",
                        "[unknown OS version]")
        ))
        logger.info(String.format("::: user.home @ %s",
                CommonUtils.getSystemProperty("user.home",
                        "[unknown user's home directory]")
        ))
        logger.info(String.format("::: user.dir @ %s",
                CommonUtils.getSystemProperty("user.dir",
                        "[unknown user's working directory]")
        ))
        logger.info(String.format("::: CF @ %s", ConfigurationController["execution.configuration.name"]
        ))
        logger.info(displaySeparator())
        logger.info(line)
        wrapText(line)
        addNewLine()
    }

    /**
     * Displays the elapsed time in the terminal.
     */
    fun printTime() {
        if (ConfigurationController.contains("display.time")) {
            if (ConfigurationController.contains("display.line") || ConfigurationController.contains("display.exception")) {
                addNewLine()
            }
            val text = messages.getMessage(
                    Messages.INFO_DISPLAY_EXECUTION_TIME, StopWatch.time)
            logger.info(text)
            wrapText(text)
        }
    }

    /**
     * Displays the application logo in the terminal.
     */
    fun printLogo() {
        println("  __ _ _ __ __ _ _ __ __ _ " + "\n" +
                " / _` | '__/ _` | '__/ _` |" + "\n" +
                "| (_| | | | (_| | | | (_| |" + "\n" +
                " \\__,_|_|  \\__,_|_|  \\__,_|")
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
        var line = messages.getMessage(
                Messages.INFO_LABEL_ON_DETAILS) + " "
        line = StringUtils.rightPad(
                StringUtils.abbreviate(line, width), width, "-")
        println(line)
    }

    /**
     * Gets the output separator with the provided text.
     *
     * @param message The provided text.
     * @return A string containing the output separator with the provided text.
     */
    fun displayOutputSeparator(message: String): String {
        return StringUtils.center(" $message ",
                width, "-")
    }

    /**
     * Gets the line separator.
     *
     * @return A string containing the line separator.
     */
    fun displaySeparator(): String {
        return StringUtils.repeat("-", width)
    }
}
