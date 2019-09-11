/**
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
package com.github.cereda.arara.model;

import com.github.cereda.arara.controller.ConfigurationController;
import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.controller.LoggingController;
import com.github.cereda.arara.utils.CommonUtils;
import com.github.cereda.arara.utils.DisplayUtils;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Implements the command line parser.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Parser {

    // command line arguments to be
    // processed by this parser
    private final String[] arguments;

    // command line options, it will
    // group each option available
    // in arara
    private Options options;

    // each option available in
    // arara
    private Option version;
    private Option help;
    private Option log;
    private Option verbose;
    private Option silent;
    private Option dryrun;
    private Option timeout;
    private Option language;
    private Option loops;
    private Option preamble;
    private Option onlyheader;

    public Parser() {
        this.arguments = null;
    }

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Constructor.
     * @param arguments Array of strings representing the command line
     * arguments.
     */
    public Parser(String[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Parses the command line arguments.
     * @return A boolean value indicating if the parsing should allow the
     * application to look for directives in the provided main file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public boolean parse() throws AraraException {

        // create new instances of the
        // command line options, including
        // the ones that require arguments
        version = new Option("V", "version", false, "");
        help = new Option("h", "help", false, "");
        log = new Option("l", "log", false, "");
        verbose = new Option("v", "verbose", false, "");
        silent = new Option("s", "silent", false, "");
        dryrun = new Option("n", "dry-run", false, "");
        timeout = new Option("t", "timeout", true, "");
        timeout.setArgName("number");
        language = new Option("L", "language", true, "");
        language.setArgName("code");
        loops = new Option("m", "max-loops", true, "");
        loops.setArgName("number");
        preamble = new Option("p", "preamble", true, "");
        preamble.setArgName("name");
        onlyheader = new Option("H", "header", false, "");

        // add all options to the options
        // group, so they are recognized
        // by the command line parser
        options = new Options();
        options.addOption(version);
        options.addOption(help);
        options.addOption(log);
        options.addOption(verbose);
        options.addOption(silent);
        options.addOption(dryrun);
        options.addOption(timeout);
        options.addOption(language);
        options.addOption(loops);
        options.addOption(preamble);
        options.addOption(onlyheader);

        // update all descriptions based
        // on the localized messages
        updateDescriptions();

        // a new default command line
        // parser is created and the
        // arguments are parsed
        CommandLineParser parser = new DefaultParser();

        try {

            CommandLine line = parser.parse(options, arguments);

            String reference;
            if (line.hasOption("language")) {
                ConfigurationController.getInstance().
                        put("execution.language",
                                new Language(line.getOptionValue("language"))
                        );
                Locale locale = ((Language) ConfigurationController.
                        getInstance().get("execution.language")).getLocale();
                messages.setLocale(locale);
                updateDescriptions();
            }

            if (line.hasOption("help")) {
                printVersion();
                printUsage();
                return false;
            }

            if (line.hasOption("version")) {
                printVersion();
                printNotes();
                return false;
            }

            if (line.getArgs().length != 1) {
                printVersion();
                printUsage();
                return false;
            } else {
                reference = line.getArgs()[0];
            }

            if (line.hasOption("timeout")) {
                try {
                    long value = Long.parseLong(line.getOptionValue("timeout"));
                    if (value <= 0) {
                        throw new AraraException(
                                messages.getMessage(
                                        Messages.ERROR_PARSER_TIMEOUT_INVALID_RANGE
                                )
                        );
                    } else {
                        ConfigurationController.getInstance().
                                put("execution.timeout", true);
                        ConfigurationController.getInstance().
                                put("execution.timeout.value", value);
                    }
                } catch (NumberFormatException nfexception) {
                    throw new AraraException(
                            messages.getMessage(
                                    Messages.ERROR_PARSER_TIMEOUT_NAN
                            )
                    );
                }
            }

            if (line.hasOption("max-loops")) {
                try {
                    long value = Long.parseLong(
                            line.getOptionValue("max-loops")
                    );
                    if (value <= 0) {
                        throw new AraraException(
                                messages.getMessage(
                                        Messages.ERROR_PARSER_LOOPS_INVALID_RANGE
                                )
                        );
                    } else {
                        ConfigurationController.getInstance().
                                put("execution.loops", value);
                    }
                } catch (NumberFormatException nfexception) {
                    throw new AraraException(
                            messages.getMessage(
                                    Messages.ERROR_PARSER_LOOPS_NAN
                            )
                    );
                }
            }

            if (line.hasOption("verbose")) {
                ConfigurationController.getInstance().
                        put("execution.verbose", true);
            }
            
            if (line.hasOption("silent")) {
                ConfigurationController.getInstance().
                        put("execution.verbose", false);
            }

            if (line.hasOption("dry-run")) {
                ConfigurationController.getInstance().
                        put("execution.dryrun", true);
                ConfigurationController.getInstance().
                        put("execution.errors.halt", false);
            }

            if (line.hasOption("log")) {
                ConfigurationController.getInstance().
                        put("execution.logging", true);
            }
            
            if (line.hasOption("preamble")) {
                @SuppressWarnings("unchecked")
                Map<String, String> preambles = (Map<String, String>) 
                        ConfigurationController.getInstance().
                        get("execution.preambles");
                if (preambles.containsKey(line.getOptionValue("preamble"))) {
                    ConfigurationController.getInstance().
                        put("execution.preamble.active", true);
                    ConfigurationController.getInstance().
                        put("execution.preamble.content",
                                preambles.get(line.getOptionValue("preamble"))
                        );
                }
                else {
                    throw new AraraException(
                            messages.getMessage(
                                    Messages.ERROR_PARSER_INVALID_PREAMBLE,
                                    line.getOptionValue("preamble")
                            )
                    );
                }
            }
            
            if (line.hasOption("header")) {
                ConfigurationController.getInstance().
                        put("execution.header", true);
            }

            CommonUtils.discoverFile(reference);
            LoggingController.enableLogging((Boolean) ConfigurationController.
                    getInstance().get("execution.logging"));
            ConfigurationController.getInstance().put("display.time", true);

            return true;

        } catch (ParseException pexception) {
            printVersion();
            printUsage();
            return false;
        }
    }

    /**
     * Prints the application usage.
     */
    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuilder builder = new StringBuilder();
        builder.append("arara [file [--dry-run] [--log] ");
        builder.append("[--verbose | --silent] [--timeout N] ");
        builder.append("[--max-loops N] [--language L] ");
        builder.append("[ --preamble P ] [--header] | --help | --version]");
        formatter.printHelp(builder.toString(), options);
    }

    /**
     * Prints the application version.
     */
    private void printVersion() {
        String year = (String) ConfigurationController.getInstance().
                get("application.copyright.year");
        String number = (String) ConfigurationController.getInstance().
                get("application.version");
        String revision = (String) ConfigurationController.getInstance().
                get("application.revision");
        StringBuilder builder = new StringBuilder();
        builder.append("arara ");
        builder.append(number);
        builder.append(" (revision ");
        builder.append(revision);
        builder.append(")");
        builder.append("\n");
        builder.append("Copyright (c) ").append(year).append(", ");
        builder.append("Paulo Roberto Massa Cereda");
        builder.append("\n");
        builder.append(messages.getMessage(
                Messages.INFO_PARSER_ALL_RIGHTS_RESERVED)
        );
        builder.append("\n");
        System.out.println(builder.toString());
    }

    /**
     * Print the application notes.
     */
    private void printNotes() {
        DisplayUtils.wrapText(messages.getMessage(Messages.INFO_PARSER_NOTES));
    }

    /**
     * Updates all the descriptions in order to make them reflect the current
     * language setting.
     */
    private void updateDescriptions() {
        version.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_VERSION_DESCRIPTION
                )
        );
        help.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_HELP_DESCRIPTION
                )
        );
        log.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_LOG_DESCRIPTION
                )
        );
        verbose.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_VERBOSE_MODE_DESCRIPTION
                )
        );
        silent.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_SILENT_MODE_DESCRIPTION
                )
        );
        dryrun.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_DRYRUN_MODE_DESCRIPTION
                )
        );
        timeout.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_TIMEOUT_DESCRIPTION
                )
        );
        language.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_LANGUAGE_DESCRIPTION
                )
        );
        loops.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_LOOPS_DESCRIPTION
                )
        );
        preamble.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_PREAMBLE_DESCRIPTION
                )
        );
        onlyheader.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_ONLY_HEADER
                )
        );
    }

}
