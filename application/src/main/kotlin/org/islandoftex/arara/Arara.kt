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
package org.islandoftex.arara

import com.github.ajalt.clikt.parameters.options.versionOption
import org.islandoftex.arara.configuration.AraraSpec
import org.islandoftex.arara.configuration.Configuration
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.model.AraraException
import org.islandoftex.arara.model.Extractor
import org.islandoftex.arara.model.Interpreter
import org.islandoftex.arara.ruleset.DirectiveUtils
import org.islandoftex.arara.utils.DisplayUtils
import com.uchuhimo.konf.Config
import kotlin.time.ExperimentalTime

object Arara {
    // TODO: watch config files
    val baseconfig = Config { addSpec(AraraSpec) }
            .from.env()
            .from.systemProperties()
    var config = baseconfig.withLayer("initial")

    /**
     * Main method. This is the application entry point.
     * @param args A string array containing all command line arguments.
     */
    @ExperimentalTime
    @JvmStatic
    fun main(args: Array<String>) {
        val year = config[AraraSpec.Application.copyrightYear]
        val version = config[AraraSpec.Application.version]
        CLI().versionOption(version, names = setOf("-V", "--version"),
                message = {
                    "arara $version\n" +
                            "Copyright (c) $year, Paulo Roberto Massa Cereda\n" +
                            LanguageController.getMessage(Messages
                                    .INFO_PARSER_ALL_RIGHTS_RESERVED) + "\n\n" +
                            LanguageController.getMessage(Messages
                                    .INFO_PARSER_NOTES)
                })
                .main(args)
    }

    @ExperimentalTime
    fun run() {
        try {
            // first of all, let's try to load a potential
            // configuration file located at the current
            // user's home directory; if there's a bad
            // configuration file, arara will panic and
            // end the execution
            Configuration.load()

            // let's print the current file information; it is a
            // basic display, just the file name, the size properly
            // formatted as a human readable format, and the last
            // modification date; also, in this point, the logging
            // feature starts to collect data (of course, if enabled
            // either through the configuration file or manually
            // in the command line)
            DisplayUtils.printFileInformation()

            // time to read the file and try to extract the directives;
            // extract() brings us a list of directives properly parsed
            // and almost ready to be handled; note that no directives
            // in the provided file will raise an exception; this is
            // by design and I opted to not include a default fallback
            // (although it wouldn't be so difficult to write one,
            // I decided not to take the risk)
            val extracted = Extractor.extract(config[AraraSpec.Execution
                    .reference])

            // it is time to validate the directives (for example, we have
            // a couple of keywords that cannot be used as directive
            // parameters); another interesting feature of the validate()
            // method is to replicate a directive that has the 'files'
            // keyword on it, since it's the whole point of having 'files'
            // in the first place; if you check the log file, you will see
            // that the list of extracted directives might differ from
            // the final list of directives to be effectively processed
            // by arara
            val directives = DirectiveUtils.process(extracted)

            // time to shine, now the interpreter class will interpret
            // one directive at a time, get the corresponding rule,
            // set the parameters, evaluate it, get the tasks, run them,
            // evaluate the result and print the status; note that
            // arara, from this version on, will try to evaluate things
            // progressively, so in case of an error, the previous tasks
            // were already processed and potentially executed
            Interpreter(directives).execute()
        } catch (exception: AraraException) {
            // something bad just happened, so arara will print the proper
            // exception and provide details on it, if available; the idea
            // here is to propagate an exception throughout the whole
            // application and catch it here instead of a local treatment
            DisplayUtils.printException(exception)
        }
    }
}
