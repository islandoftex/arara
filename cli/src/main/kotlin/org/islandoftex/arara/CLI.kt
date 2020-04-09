// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlin.system.exitProcess
import kotlin.time.TimeSource
import kotlin.time.milliseconds
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.session.ExecutionMode
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.configuration.Configuration
import org.islandoftex.arara.cli.filehandling.FileSearchingUtils
import org.islandoftex.arara.cli.localization.Language
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.cli.model.ProjectFile
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.ExecutionOptions
import org.islandoftex.arara.core.session.Session

/**
 * arara's command line interface
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
class CLI : CliktCommand(name = "arara", printHelpOnEmptyArgs = true) {
    private val log by option("-l", "--log",
            help = "Generate a log output")
            .flag()
    private val verbose by option("-v", "--verbose",
            help = "Print the command output")
            .flag("-s", "--silent")
    private val dryrun by option("-n", "--dry-run",
            help = "Go through all the motions of running a command, but " +
                    "with no actual calls")
            .flag()
    private val onlyheader by option("-H", "--header",
            help = "Extract directives only in the file header")
            .flag()
    private val timeout by option("-t", "--timeout",
            help = "Set the execution timeout (in milliseconds)")
            .int().restrictTo(min = 1)
    private val language by option("-L", "--language",
            help = "Set the application language")
    private val maxLoops by option("-m", "--max-loops",
            help = "Set the maximum number of loops (> 0)")
            .int().restrictTo(min = 1)
    private val preamble by option("-p", "--preamble",
            help = "Set the file preamble based on the configuration file")
    private val workingDirectory by option("-d", "--working-directory",
            help = "Set the working directory for all tools")
            .path(mustExist = true, canBeFile = false, mustBeReadable = true)

    private val reference by argument("file",
            help = "The file(s) to evaluate and process")
            .multiple(required = true)

    /**
     * Update the default configuration with the values parsed from the
     * command line.
     */
    private fun updateConfigurationFromCommandLine() {
        language?.let {
            Arara.config[AraraSpec.Execution.language] = Language(it)
            LanguageController.setLocale(Arara.config[AraraSpec.Execution.language]
                    .locale)
        }

        Arara.config[AraraSpec.Execution.executionOptions] = ExecutionOptions(
                maxLoops = maxLoops
                        ?: Arara.config[AraraSpec.Execution.executionOptions].maxLoops,
                timeoutValue = timeout?.milliseconds
                        ?: Arara.config[AraraSpec.Execution.executionOptions].timeoutValue,
                verbose = if (verbose)
                    true
                else
                    Arara.config[AraraSpec.Execution.executionOptions].verbose,
                executionMode = if (dryrun)
                    ExecutionMode.DRY_RUN
                else
                    Arara.config[AraraSpec.Execution.executionOptions].executionMode,
                parseOnlyHeader = if (onlyheader)
                    true
                else
                    Arara.config[AraraSpec.Execution.executionOptions].parseOnlyHeader
        )

        if (log) Arara.config[AraraSpec.Execution.logging] = log
        preamble?.let {
            val preambles = Arara.config[AraraSpec.Execution.preambles]
            if (preambles.containsKey(it)) {
                Arara.config[AraraSpec.Execution.preamblesActive] = true
                Arara.config[AraraSpec.Execution.preamblesContent] =
                        // will never throw (see check above)
                        preambles.getValue(it)
            } else {
                throw AraraException(
                        LanguageController.getMessage(
                                Messages.ERROR_PARSER_INVALID_PREAMBLE, it)
                )
            }
        }

        Arara.config[AraraSpec.UserInteraction.displayTime] = true
    }

    /**
     * The actual main method of arara (when run in command-line mode)
     */
    override fun run() {
        // the first component to be initialized is the
        // logging controller; note init() actually disables
        // the logging, so early exceptions won't generate
        // a lot of noise in the terminal
        LoggingUtils.init()

        // arara features a stopwatch, so we can see how much time has passed
        // since everything started; internally, this class makes use of
        // nano time, so we might get an interesting precision here
        // (although timing is not a serious business in here, it's
        // just a cool addition)
        val executionStart = TimeSource.Monotonic.markNow()

        // arara stores the environment variables accessible at the start
        // of the execution in the session object for the user
        Session.updateEnvironmentVariables()

        // logging has to be initialized only once and for all because
        // context resets lead to missing output
        LoggingUtils.enableLogging(log)

        val workingDir = workingDirectory ?: AraraSpec.Execution.workingDirectory.default
        try {
            // TODO: this will have to change for parallelization
            Project(
                    workingDir.fileName.toString(),
                    workingDir,
                    reference.map {
                        FileSearchingUtils.resolveFile(it, workingDir.toFile())
                    }.toSet()
            ).files.map {
                if (it.path.isAbsolute)
                    it
                else
                    ProjectFile(
                            workingDirectory.resolve(it.path).toRealPath(),
                            it.fileType,
                            it.priority
                    )
            }.toSet().forEach {
                // TODO: do we have to reset some more file-specific config?
                // especially the working directory will have to be set and
                // changed
                Arara.config = Arara.baseconfig.withLayer(it.toString())
                // load the configuration (we need to set the working directory
                // first because the configuration loading relies on it)
                // CLI options are prioritized
                Arara.config[AraraSpec.Execution.workingDirectory] = workingDir
                Configuration.load()
                updateConfigurationFromCommandLine()
                // run arara
                Arara.config[AraraSpec.Execution.reference] = it
                Arara.run()
                // add an empty line between file executions
                println()
            }

            // this is the last command from arara; once the execution time is
            // available, print it; note that this notification is suppressed
            // when the command line parsing returns false as result (it makes
            // no sense to print the execution time for a help message, I guess)
            DisplayUtils.printTime(executionStart.elapsedNow().inSeconds)
        } catch (ex: AraraException) {
            DisplayUtils.printException(ex)
            Arara.config[AraraSpec.Execution.exitCode] = 2
        }

        // gets the application exit status; the rule here is:
        // 0 : everything went just fine (note that the dry-run mode always
        //     makes arara exit with 0, unless it is an error in the directive
        //     builder itself).
        // 1 : one of the tasks failed, so the execution ended abruptly. This
        //     means the error relies on the command line call, not with arara.
        // 2 : arara just handled an exception, meaning that something bad
        //     just happened and might require user intervention.
        exitProcess(Arara.config[AraraSpec.Execution.exitCode])
    }
}
