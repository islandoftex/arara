package com.github.cereda.arara

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import com.github.cereda.arara.configuration.AraraSpec
import com.github.cereda.arara.localization.Language
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.utils.CommonUtils
import com.github.cereda.arara.utils.LoggingUtils
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

class CLI : CliktCommand(name = "arara", printHelpOnEmptyArgs = true) {
    private val log by option("-l", "--log",
            help = "Generate a log output")
            .flag(default = false)
    private val verbose by option("-v", "--verbose",
            help = "Print the command output")
            .flag("-s", "--silent", default = false)
    private val dryrun by option("-n", "--dry-run",
            help = "Go through all the motions of running a command, but " +
                    "with no actual calls")
            .flag(default = false)
    private val onlyheader by option("-H", "--header",
            help = "Extract directives only in the file header")
            .flag(default = false)
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

    private val reference by argument("file",
            help = "The file(s) to evaluate and process")
            .multiple(required = true)

    @ExperimentalTime
    override fun run() {
        Arara.config[AraraSpec.Execution.language] = Language(language)
        LanguageController.setLocale(Arara.config[AraraSpec.Execution.language]
                .locale)

        Arara.config[AraraSpec.Execution.logging] = log
        Arara.config[AraraSpec.Execution.verbose] = verbose
        Arara.config[AraraSpec.Execution.dryrun] = dryrun
        Arara.config[AraraSpec.Execution.header] = onlyheader
        Arara.config[AraraSpec.Execution.maxLoops] = maxLoops
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

        // TODO: process more than one file
        CommonUtils.discoverFile(reference[0])
        LoggingUtils.enableLogging(log)
        Arara.config[AraraSpec.UserInteraction.displayTime] = true

        Arara.run()
    }
}
