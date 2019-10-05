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
package com.github.cereda.arara.model

import com.github.cereda.arara.configuration.ConfigurationController
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.utils.LoggingUtils
import com.github.cereda.arara.localization.Language
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.utils.CommonUtils
import com.github.cereda.arara.utils.DisplayUtils
import org.apache.commons.cli.*

/**
 * Implements the command line parser.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class Parser(
        // command line arguments to be
        // processed by this parser
        private val arguments: Array<String> = arrayOf()) {

    // command line options, it will
    // group each option available
    // in arara
    private var options: Options = Options()

    // each option available in
    // arara
    private var version: Option = Option("V", "version", false, "")
    private var help: Option = Option("h", "help", false, "")
    private var log: Option = Option("l", "log", false, "")
    private var verbose: Option = Option("v", "verbose", false, "")
    private var silent: Option = Option("s", "silent", false, "")
    private var dryrun: Option = Option("n", "dry-run", false, "")
    private var timeout: Option = Option("t", "timeout", true, "").apply {
        argName = "number"
    }
    private var language: Option = Option("L", "language", true, "").apply {
        argName = "code"
    }
    private var loops: Option = Option("m", "max-loops", true, "").apply {
        argName = "number"
    }
    private var preamble: Option = Option("p", "preamble", true, "").apply {
        argName = "name"
    }
    private var onlyheader: Option = Option("H", "header", false, "")

    /**
     * Parses the command line arguments.
     *
     * @return A boolean value indicating if the parsing should allow the
     * application to look for directives in the provided main file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun parse(): Boolean {
        // add all options to the options
        // group, so they are recognized
        // by the command line parser
        options.addOption(version)
        options.addOption(help)
        options.addOption(log)
        options.addOption(verbose)
        options.addOption(silent)
        options.addOption(dryrun)
        options.addOption(timeout)
        options.addOption(language)
        options.addOption(loops)
        options.addOption(preamble)
        options.addOption(onlyheader)

        // update all descriptions based
        // on the localized messages
        updateDescriptions()

        // a new default command line
        // parser is created and the
        // arguments are parsed
        val parser = DefaultParser()

        try {
            val line = parser.parse(options, arguments)

            val reference: String
            if (line.hasOption("language")) {
                ConfigurationController.put("execution.language",
                    Language(line.getOptionValue("language"))
                )
                val locale = (ConfigurationController["execution.language"] as Language).locale
                messages.setLocale(locale)
                updateDescriptions()
            }

            if (line.hasOption("help")) {
                printVersion()
                printUsage()
                return false
            }

            if (line.hasOption("version")) {
                printVersion()
                printNotes()
                return false
            }

            if (line.args.size != 1) {
                printVersion()
                printUsage()
                return false
            } else {
                reference = line.args[0]
            }

            if (line.hasOption("timeout")) {
                try {
                    val value = java.lang.Long.parseLong(line.getOptionValue("timeout"))
                    if (value <= 0) {
                        throw AraraException(
                                messages.getMessage(
                                        Messages.ERROR_PARSER_TIMEOUT_INVALID_RANGE
                                )
                        )
                    } else {
                        ConfigurationController.put("execution.timeout", true)
                        ConfigurationController.put("execution.timeout.value", value)
                    }
                } catch (nfexception: NumberFormatException) {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_PARSER_TIMEOUT_NAN
                            )
                    )
                }

            }

            if (line.hasOption("max-loops")) {
                try {
                    val value = java.lang.Long.parseLong(
                            line.getOptionValue("max-loops")
                    )
                    if (value <= 0) {
                        throw AraraException(
                                messages.getMessage(
                                        Messages.ERROR_PARSER_LOOPS_INVALID_RANGE
                                )
                        )
                    } else {
                        ConfigurationController.put("execution.loops", value)
                    }
                } catch (nfexception: NumberFormatException) {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_PARSER_LOOPS_NAN
                            )
                    )
                }

            }

            if (line.hasOption("verbose")) {
                ConfigurationController.put("execution.verbose", true)
            }

            if (line.hasOption("silent")) {
                ConfigurationController.put("execution.verbose", false)
            }

            if (line.hasOption("dry-run")) {
                ConfigurationController.put("execution.dryrun", true)
                ConfigurationController.put("execution.errors.halt", false)
            }

            if (line.hasOption("log")) {
                ConfigurationController.put("execution.logging", true)
            }

            if (line.hasOption("preamble")) {
                val preambles = ConfigurationController["execution.preambles"] as Map<String, String>
                if (preambles.containsKey(line.getOptionValue("preamble"))) {
                    ConfigurationController.put("execution.preamble.active", true)
                    ConfigurationController.put("execution.preamble.content",
                            // will never throw (see check above)
                            preambles.getValue(line.getOptionValue("preamble"))
                    )
                } else {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_PARSER_INVALID_PREAMBLE,
                                    line.getOptionValue("preamble")
                            )
                    )
                }
            }

            if (line.hasOption("header")) {
                ConfigurationController.put("execution.header", true)
            }

            CommonUtils.discoverFile(reference)
            LoggingUtils.enableLogging(ConfigurationController["execution.logging"] as Boolean)
            ConfigurationController.put("display.time", true)

            return true

        } catch (_: ParseException) {
            printVersion()
            printUsage()
            return false
        }

    }

    /**
     * Prints the application usage.
     */
    private fun printUsage() {
        val formatter = HelpFormatter()
        formatter.printHelp("arara [file [--dry-run] [--log] " +
                "[--verbose | --silent] [--timeout N] " +
                "[--max-loops N] [--language L] " +
                "[ --preamble P ] [--header] | --help | --version]", options)
    }

    /**
     * Prints the application version.
     */
    private fun printVersion() {
        val year = ConfigurationController["application.copyright.year"] as String
        val number = ConfigurationController["application.version"] as String
        val revision = ConfigurationController["application.revision"] as String
        println("arara " + number + " (revision " + revision + ")\n" +
                "Copyright (c) " + year + ", " + "Paulo Roberto Massa Cereda\n" +
                messages.getMessage(Messages
                        .INFO_PARSER_ALL_RIGHTS_RESERVED) + "\n")
    }

    /**
     * Print the application notes.
     */
    private fun printNotes() {
        DisplayUtils.wrapText(messages.getMessage(Messages.INFO_PARSER_NOTES))
    }

    /**
     * Updates all the descriptions in order to make them reflect the current
     * language setting.
     */
    private fun updateDescriptions() {
        version.description = messages.getMessage(
                Messages.INFO_PARSER_VERSION_DESCRIPTION
        )
        help.description = messages.getMessage(
                Messages.INFO_PARSER_HELP_DESCRIPTION
        )
        log.description = messages.getMessage(
                Messages.INFO_PARSER_LOG_DESCRIPTION
        )
        verbose.description = messages.getMessage(
                Messages.INFO_PARSER_VERBOSE_MODE_DESCRIPTION
        )
        silent.description = messages.getMessage(
                Messages.INFO_PARSER_SILENT_MODE_DESCRIPTION
        )
        dryrun.description = messages.getMessage(
                Messages.INFO_PARSER_DRYRUN_MODE_DESCRIPTION
        )
        timeout.description = messages.getMessage(
                Messages.INFO_PARSER_TIMEOUT_DESCRIPTION
        )
        language.description = messages.getMessage(
                Messages.INFO_PARSER_LANGUAGE_DESCRIPTION
        )
        loops.description = messages.getMessage(
                Messages.INFO_PARSER_LOOPS_DESCRIPTION
        )
        preamble.description = messages.getMessage(
                Messages.INFO_PARSER_PREAMBLE_DESCRIPTION
        )
        onlyheader.description = messages.getMessage(
                Messages.INFO_PARSER_ONLY_HEADER
        )
    }

    companion object {

        // the application messages obtained from the
        // language controller
        private val messages = LanguageController
    }

}
