package com.github.cereda.arara

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.restrictTo
import com.github.cereda.arara.configuration.AraraSpec
import com.github.cereda.arara.localization.Language
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.model.Session
import com.github.cereda.arara.utils.CommonUtils
import com.github.cereda.arara.utils.DisplayUtils
import com.github.cereda.arara.utils.LoggingUtils
import kotlin.system.exitProcess
import kotlin.time.ClockMark
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock
import kotlin.time.milliseconds

class CLI : CliktCommand(name = "arara", printHelpOnEmptyArgs = true) {
    private val log by option("-l", "--log",
            help = "Generate a log output")
            .flag(default = AraraSpec.Execution.logging.default)
    private val verbose by option("-v", "--verbose",
            help = "Print the command output")
            .flag("-s", "--silent",
                    default = AraraSpec.Execution.verbose.default)
    private val dryrun by option("-n", "--dry-run",
            help = "Go through all the motions of running a command, but " +
                    "with no actual calls")
            .flag(default = AraraSpec.Execution.dryrun.default)
    private val onlyheader by option("-H", "--header",
            help = "Extract directives only in the file header")
            .flag(default = AraraSpec.Execution.onlyHeader.default)
    private val timeout by option("-t", "--timeout",
            help = "Set the execution timeout (in milliseconds)")
            .int().restrictTo(min = 1)
    private val language by option("-L", "--language",
            help = "Set the application language")
            .default(AraraSpec.Application.defaultLanguageCode.default)
    private val maxLoops by option("-m", "--max-loops",
            help = "Set the maximum number of loops (> 0)")
            .int().restrictTo(min = 1)
            .default(AraraSpec.Execution.maxLoops.default)
    private val preamble by option("-p", "--preamble",
            help = "Set the file preamble based on the configuration file")
    private val workingDirectory by option("-d", "--working-directory",
            help = "Set the working directory for all tools")
            .path(exists = true, fileOkay = false, readable = true)
            .default(AraraSpec.Execution.workingDirectory.default)

    private val reference by argument("file",
            help = "The file(s) to evaluate and process")
            .multiple(required = true)

    /**
     * Update the default configuration with the values parsed from the
     * command line.
     */
    @ExperimentalTime
    private fun updateConfigurationFromCommandLine() {
        Arara.config[AraraSpec.Execution.language] = Language(language)
        LanguageController.setLocale(Arara.config[AraraSpec.Execution.language]
                .locale)

        Arara.config[AraraSpec.Execution.logging] = log
        Arara.config[AraraSpec.Execution.verbose] = verbose
        Arara.config[AraraSpec.Execution.dryrun] = dryrun
        Arara.config[AraraSpec.Execution.onlyHeader] = onlyheader
        Arara.config[AraraSpec.Execution.maxLoops] = maxLoops
        Arara.config[AraraSpec.Execution.workingDirectory] = workingDirectory
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
        timeout?.let {
            Arara.config[AraraSpec.Execution.timeout] = true
            Arara.config[AraraSpec.Execution.timeoutValue] = it.milliseconds
        }

        LoggingUtils.enableLogging(log)
        Arara.config[AraraSpec.UserInteraction.displayTime] = true
    }

    /**
     * The actual main method of arara (when run in command-line mode)
     */
    @ExperimentalTime
    override fun run() {
        // the first component to be initialized is the
        // logging controller; note init() actually disables
        // the logging, so early exceptions won't generate
        // a lot of noise in the terminal
        LoggingUtils.init()

        // print the arara logo in the terminal; I just
        // hope people use this tool in a good terminal with
        // fixed-width fonts, otherwise the logo will be messed
        DisplayUtils.printLogo()

        // arara features a stopwatch, so we can see how much time has passed
        // since everything started; internally, this class makes use of
        // nano time, so we might get an interesting precision here
        // (although timing is not a serious business in here, it's
        // just a cool addition)
        val executionStart: ClockMark = MonoClock.markNow()

        // arara stores the environment variables accessible at the start
        // of the execution in the session object for the user
        Session.updateEnvironmentVariables()

        // TODO: this will have to change for parallelization
        reference.forEach {
            // TODO: do we have to reset some more file-specific config?
            // especially the working directory will have to be set and
            // changed
            Arara.config = Arara.baseconfig.withLayer(it)
            // next, update the configuration
            updateConfigurationFromCommandLine()
            CommonUtils.discoverFile(it)
            Arara.run()
        }

        // this is the last command from arara; once the execution time is
        // available, print it; note that this notification is suppressed
        // when the command line parsing returns false as result (it makes
        // no sense to print the execution time for a help message, I guess)
        DisplayUtils.printTime(executionStart.elapsedNow().inSeconds)

        // gets the application exit status; the rule here is:
        // 0 : everything went just fine (note that the dry-run mode always
        //     makes arara exit with 0, unless it is an error in the directive
        //     builder itself).
        // 1 : one of the tasks failed, so the execution ended abruptly. This
        //     means the error relies on the command line call, not with arara.
        // 2 : arara just handled an exception, meaning that something bad
        //     just happened and might require user intervention.
        exitProcess(CommonUtils.exitStatus)
    }
}
