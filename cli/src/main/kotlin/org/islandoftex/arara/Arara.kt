// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara

import com.github.ajalt.clikt.parameters.options.versionOption
import com.uchuhimo.konf.Config
import java.time.LocalDate
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.configuration.Configuration
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.session.Executor
import org.islandoftex.arara.core.session.ExecutorHooks

/**
 * arara's main entry point
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
object Arara {
    // TODO: watch config files
    val baseconfig = Config { addSpec(AraraSpec) }
            .from.env()
            .from.systemProperties()
    var config = baseconfig.withLayer("initial")
    var updateConfigurationFromCommandLine: () -> Unit = { }

    /**
     * Main method. This is the application entry point.
     * @param args A string array containing all command line arguments.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        // print the arara logo in the terminal; I just
        // hope people use this tool in a good terminal with
        // fixed-width fonts, otherwise the logo will be messed
        DisplayUtils.printLogo()

        val version = config[AraraSpec.Application.version]
        CLI().versionOption(version, names = setOf("-V", "--version"),
                message = {
                    "arara $version\n" +
                            "Copyright (c) ${LocalDate.now().year}, Island of TeX\n" +
                            LanguageController.getMessage(Messages
                                    .INFO_PARSER_NOTES) + "\n\n" +
                            "New features in version $version:\n" +
                            Arara::class.java
                                    .getResource("/org/islandoftex/arara/cli/configuration/release-notes")
                                    .readText()
                })
                .main(args)
    }

    fun run() {
        try {
            Executor.hooks = ExecutorHooks(
                    executeBeforeProject = {
                        // first of all, let's try to load a potential
                        // configuration file located at the current
                        // user's home directory; if there's a bad
                        // configuration file, arara will panic and
                        // end the execution
                        Configuration.load()
                    },
                    executeBeforeFile = {
                        // TODO: do we have to reset some more file-specific config?
                        // especially the working directory will have to be set and
                        // changed
                        config = baseconfig.withLayer(it.toString())
                        updateConfigurationFromCommandLine()
                        config[AraraSpec.Execution.reference] = it
                        // let's print the current file information; it is a
                        // basic display, just the file name, the size properly
                        // formatted as a human readable format, and the last
                        // modification date; also, in this point, the logging
                        // feature starts to collect data (of course, if enabled
                        // either through the configuration file or manually
                        // in the command line)
                        DisplayUtils.printFileInformation()
                    },
                    executeAfterFile = {
                        // add an empty line between file executions
                        println()
                    },
                    processDirectives = {
                        // it is time to validate the directives (for example, we have
                        // a couple of keywords that cannot be used as directive
                        // parameters); another interesting feature of the validate()
                        // method is to replicate a directive that has the 'files'
                        // keyword on it, since it's the whole point of having 'files'
                        // in the first place; if you check the log file, you will see
                        // that the list of extracted directives might differ from
                        // the final list of directives to be effectively processed
                        // by arara
                        DirectiveUtils.process(it)
                    }
            )
            Executor.execute(
                    config[AraraSpec.Execution.projects],
                    config[AraraSpec.Execution.executionOptions]
            )
        } catch (exception: AraraException) {
            // something bad just happened, so arara will print the proper
            // exception and provide details on it, if available; the idea
            // here is to propagate an exception throughout the whole
            // application and catch it here instead of a local treatment
            DisplayUtils.printException(exception)
        }
    }
}
