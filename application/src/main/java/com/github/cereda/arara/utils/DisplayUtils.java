/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda 
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
package com.github.cereda.arara.utils;

import com.github.cereda.arara.controller.ConfigurationController;
import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.model.AraraException;
import com.github.cereda.arara.model.Conditional;
import com.github.cereda.arara.model.Messages;
import com.github.cereda.arara.model.StopWatch;
import java.io.File;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements display utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class DisplayUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();
    
    // get the logger context from a factory
    private static final Logger logger =
            LoggerFactory.getLogger(DisplayUtils.class);

    /**
     * Displays the short version of the current entry in the terminal.
     * @param name Rule name.
     * @param task Task name.
     */
    private static void buildShortEntry(String name, String task) {
        int width = getWidth();
        int result = getLongestMatch();
        if (result >= width) {
            result = 10;
        }
        int space = width - result - 1;
        StringBuilder entry = new StringBuilder();
        entry.append("(").append(name).append(") ");
        entry.append(task).append(" ");
        String line = StringUtils.abbreviate(entry.toString(), space - 4);
        entry = new StringBuilder();
        entry.append(StringUtils.rightPad(line, space, ".")).append(" ");
        System.out.print(entry);
    }

    /**
     * Displays the short version of the current entry result in the terminal.
     * @param value The boolean value to be displayed.
     */
    private static void buildShortResult(boolean value) {
        int result = getLongestMatch();
        System.out.println(StringUtils.leftPad(getResult(value), result));
    }

    /**
     * Displays the current entry result in the terminal.
     * @param value The boolean value to be displayed.
     */
    public static void printEntryResult(boolean value) {
        ConfigurationController.getInstance().put("display.line", false);
        ConfigurationController.getInstance().put("display.result", true);
        ConfigurationController.getInstance()
                .put("execution.status", value ? 0 : 1);
        logger.info(
                messages.getMessage(
                        Messages.LOG_INFO_TASK_RESULT
                ).concat(" ").concat(getResult(value))
        );
        if (!isDryRunMode()) {
            if (!isVerboseMode()) {
                buildShortResult(value);
            } else {
                buildLongResult(value);
            }
        }
    }

    /**
     * Displays a long version of the current entry result in the terminal.
     * @param value The boolean value to be displayed
     */
    private static void buildLongResult(boolean value) {
        int width = getWidth();
        System.out.println(StringUtils.leftPad(
                " ".concat(getResult(value)), width, "-"
        ));
    }

    /**
     * Displays the current entry in the terminal.
     * @param name The rule name.
     * @param task The task name.
     */
    public static void printEntry(String name, String task) {
        logger.info(
                messages.getMessage(
                        Messages.LOG_INFO_INTERPRET_TASK,
                        task,
                        name
                )
        );
        ConfigurationController.getInstance().put("display.line", true);
        ConfigurationController.getInstance().put("display.result", false);
        if (!isDryRunMode()) {
            if (!isVerboseMode()) {
                buildShortEntry(name, task);
            } else {
                buildLongEntry(name, task);
            }
        } else {
            buildDryRunEntry(name, task);
        }
    }

    /**
     * Gets the length of the longest result match.
     * @return An integer value representing the longest result match.
     */
    private static int getLongestMatch() {
        String[] values = new String[]{
            messages.getMessage(Messages.INFO_LABEL_ON_SUCCESS),
            messages.getMessage(Messages.INFO_LABEL_ON_FAILURE),
            messages.getMessage(Messages.INFO_LABEL_ON_ERROR)
        };
        int max = values[0].length();
        for (String value : values) {
            if (max < value.length()) {
                max = value.length();
            }
        }
        return max;
    }

    /**
     * Displays a long version of the current entry in the terminal.
     * @param name Rule name.
     * @param task Task name.
     */
    private static void buildLongEntry(String name, String task) {
        if (ConfigurationController.getInstance().contains("display.rolling")) {
            addNewLine();
        } else {
            ConfigurationController.getInstance().put("display.rolling", true);
        }
        StringBuilder line = new StringBuilder();
        line.append("(").append(name).append(") ");
        line.append(task);
        System.out.println(displaySeparator());
        System.out.println(StringUtils.abbreviate(line.toString(), getWidth()));
        System.out.println(displaySeparator());
    }

    /**
     * Displays a dry-run version of the current entry in the terminal.
     * @param name The rule name.
     * @param task The task name.
     */
    private static void buildDryRunEntry(String name, String task) {
        if (ConfigurationController.getInstance().contains("display.rolling")) {
            addNewLine();
        } else {
            ConfigurationController.getInstance().put("display.rolling", true);
        }
        StringBuilder line = new StringBuilder();
        line.append("[DR] (").append(name).append(") ");
        line.append(task);
        System.out.println(StringUtils.abbreviate(line.toString(), getWidth()));
        System.out.println(displaySeparator());
    }

    /**
     * Displays the exception in the terminal.
     * @param exception The exception object.
     */
    public static void printException(AraraException exception) {
        ConfigurationController.getInstance().put("display.exception", true);
        ConfigurationController.getInstance().put("execution.status", 2);
        boolean display = false;
        if (ConfigurationController.getInstance().contains("display.line")) {
            display = (Boolean) ConfigurationController.
                    getInstance().get("display.line");
        }
        if (ConfigurationController.getInstance().contains("display.result")) {
            if (((Boolean) ConfigurationController.
                    getInstance().get("display.result")) == true) {
                addNewLine();
            }
        }
        if (display) {
            if (!isDryRunMode()) {
                if (!isVerboseMode()) {
                    buildShortError();
                } else {
                    buildLongError();
                }
                addNewLine();
            }
        }
        String text = exception.hasException() ?
                exception.getMessage().concat(" ").concat(
                        messages.getMessage(
                                Messages.INFO_DISPLAY_EXCEPTION_MORE_DETAILS
                        )
                )
                : exception.getMessage();
        logger.error(text);
        wrapText(text);
        if (exception.hasException()) {
            addNewLine();
            displayDetailsLine();
            String details = exception.getException().getMessage();
            logger.error(details);
            wrapText(details);
        }
    }

    /**
     * Gets the string representation of the provided boolean value.
     * @param value The boolean value.
     * @return The string representation.
     */
    private static String getResult(boolean value) {
        return (value == true ?
                messages.getMessage(
                        Messages.INFO_LABEL_ON_SUCCESS
                )
                : messages.getMessage(Messages.INFO_LABEL_ON_FAILURE));
    }

    /**
     * Displays the short version of an error in the terminal.
     */
    private static void buildShortError() {
        int result = getLongestMatch();
        System.out.println(StringUtils.leftPad(
                messages.getMessage(
                        Messages.INFO_LABEL_ON_ERROR
                ),
                result
        ));
    }

    /**
     * Displays the long version of an error in the terminal.
     */
    private static void buildLongError() {
        String line = StringUtils.leftPad(
                " ".concat(messages.getMessage(Messages.INFO_LABEL_ON_ERROR)),
                getWidth(), "-");
        System.out.println(line);
    }

    /**
     * Gets the default terminal width defined in the settings.
     * @return An integer representing the terminal width.
     */
    private static int getWidth() {
        return (Integer) ConfigurationController.
                getInstance().get("application.width");
    }

    /**
     * Displays the provided text wrapped nicely according to the default
     * terminal width.
     * @param text The text to be displayed.
     */
    public static void wrapText(String text) {
        System.out.println(WordUtils.wrap(text, getWidth()));
    }

    /**
     * Checks if the execution is in dry-run mode.
     * @return A boolean value indicating if the execution is in dry-run mode.
     */
    private static boolean isDryRunMode() {
        return (Boolean) ConfigurationController.
                getInstance().get("execution.dryrun");
    }

    /**
     * Checks if the execution is in verbose mode.
     * @return A boolean value indicating if the execution is in verbose mode.
     */
    private static boolean isVerboseMode() {
        return (Boolean) ConfigurationController.
                getInstance().get("execution.verbose");
    }

    /**
     * Displays the rule authors in the terminal.
     * @param authors The list of authors.
     */
    public static void printAuthors(List<String> authors) {
        StringBuilder line = new StringBuilder();
        line.append(authors.size() == 1 ?
                messages.getMessage(Messages.INFO_LABEL_AUTHOR)
                : messages.getMessage(Messages.INFO_LABEL_AUTHORS));
        String text = authors.isEmpty() ?
                messages.getMessage(Messages.INFO_LABEL_NO_AUTHORS)
                : CommonUtils.getCollectionElements(
                        CommonUtils.trimSpaces(authors), "", "", ", ");
        line.append(" ").append(text);
        wrapText(line.toString());
    }

    /**
     * Displays the current conditional in the terminal.
     * @param conditional The conditional object.
     */
    public static void printConditional(Conditional conditional) {
        if (conditional.getType() != Conditional.ConditionalType.NONE) {
            StringBuilder line = new StringBuilder();
            line.append(messages.getMessage(Messages.INFO_LABEL_CONDITIONAL));
            line.append(" (");
            line.append(String.valueOf(conditional.getType()));
            line.append(") ").append(conditional.getCondition());
            wrapText(line.toString());
        }
    }

    /**
     * Displays the file information in the terminal.
     */
    public static void printFileInformation() {
        File file = (File) ConfigurationController.
                getInstance().get("execution.reference");
        String version = (String) ConfigurationController.
                getInstance().get("application.version");
        String line = messages.getMessage(
                Messages.INFO_DISPLAY_FILE_INFORMATION,
                file.getName(),
                CommonUtils.calculateFileSize(file),
                CommonUtils.getLastModifiedInformation(file)
        );
        logger.info(messages.getMessage(Messages.LOG_INFO_WELCOME_MESSAGE, version));
        logger.info(line);
        wrapText(line);
        addNewLine();
    }

    /**
     * Displays the elapsed time in the terminal.
     */
    public static void printTime() {
        if (ConfigurationController.getInstance().contains("display.time")) {
            if ((ConfigurationController.getInstance().contains("display.line"))
                    || (ConfigurationController.getInstance().
                            contains("display.exception"))) {
                addNewLine();
            }
            String text = messages.getMessage(
                    Messages.INFO_DISPLAY_EXECUTION_TIME, StopWatch.getTime());
            logger.info(text);
            wrapText(text);
        }
    }

    /**
     * Displays the application logo in the terminal.
     */
    public static void printLogo() {
        StringBuilder builder = new StringBuilder();
        builder.append("  __ _ _ __ __ _ _ __ __ _ ").append("\n");
        builder.append(" / _` | '__/ _` | '__/ _` |").append("\n");
        builder.append("| (_| | | | (_| | | | (_| |").append("\n");
        builder.append(" \\__,_|_|  \\__,_|_|  \\__,_|");
        System.out.println(builder.toString());
        addNewLine();
    }

    /**
     * Adds a new line in the terminal.
     */
    private static void addNewLine() {
        System.out.println();
    }

    /**
     * Displays a line containing details.
     */
    private static void displayDetailsLine() {
        String line = messages.getMessage(
                Messages.INFO_LABEL_ON_DETAILS).concat(" ");
        line = StringUtils.rightPad(
                StringUtils.abbreviate(line, getWidth()), getWidth(), "-");
        System.out.println(line);
    }

    /**
     * Gets the output separator with the provided text.
     * @param message The provided text.
     * @return A string containing the output separator with the provided text.
     */
    public static String displayOutputSeparator(String message) {
        return StringUtils.center("".concat(message).concat(" "),
                getWidth(), "-");
    }

     /**
     * Gets the line separator.
     * @return A string containing the line separator.
     */
    public static String displaySeparator() {
        return StringUtils.repeat("-", getWidth());
    }

}
