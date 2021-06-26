// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.configuration.ConfigurationUtils
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.cli.utils.printFileInformation
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.mvel.utils.MvelState

/**
 * arara's command line interface
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
class CLI : CliktCommand(name = "arara", printHelpOnEmptyArgs = true, help = """
    ${DisplayUtils.logoString.replace('\n', '\u0085').replace(' ', '\u00A0')}

    The cool TeX automation tool.

    arara executes the TeX workflow you tell it to execute. Simply specify
    your needs within your TeX file and let arara do the work. These directives
    feature conditional execution and parameter expansion.
    """) {
    private val log by option("-l", "--log")
            .help("Generate a log output")
            .flag()
    private val verbose by option("-v", "--verbose")
            .help("Print the command output")
            .flag("-s", "--silent")
    private val dryRun by option("-n", "--dry-run")
            .help("Go through all the motions of running a command, but " +
                    "with no actual calls")
            .flag()
    private val safeRun by option("-S", "--safe-run")
            .help("Run in safe mode and disable potentially harmful features. " +
                    "Make sure your projects uses only allowed features.")
            .flag()
    private val wholeFile by option("-w", "--whole-file")
            .help("Extract directives in the file, not only in the header")
            .flag()
    private val preamble by option("-p", "--preamble")
            .help("Set the file preamble based on the configuration file")
    private val timeout by option("-t", "--timeout")
            .help("Set the execution timeout (in milliseconds)")
            .int().restrictTo(min = 1)
    private val language by option("-L", "--language")
            .help("Set the application language")
    private val maxLoops by option("-m", "--max-loops")
            .help("Set the maximum number of loops (> 0)")
            .int().restrictTo(min = 1)
    private val workingDirectory by option("-d", "--working-directory")
            .help("Set the working directory for all tools")
            .path(mustExist = true, canBeFile = false, mustBeReadable = true)
    private val parameters: Map<String, String> by option("-P", "--call-property")
            .help("Pass parameters to the application to be used within the session.")
            .associate()

    private val reference by argument("file")
            .help("The file(s) to evaluate and process")
            .multiple(required = true)

    /**
     * Resolve the projects to be run from either a configuration file that
     * configures the run or by creating a single project from the called
     * files.
     */
    private fun resolveProjects(): List<org.islandoftex.arara.api.files.Project> {
        // resolve the working directory from the one that may be given
        // as command line parameter; otherwise resolve current directory
        val workingDir = (workingDirectory?.toMPPPath() ?: MPPPath("."))
                .normalize()

        return reference.singleOrNull()
                ?.takeIf { it.trim().endsWith(".yaml") }
                ?.let { workingDir.resolve(it) }
                ?.takeIf { it.exists }?.readText()
                ?.let {
                    // try project resolution in the following order:
                    // - treat project file as list of projects
                    // - treat project file as a single project
                    // - do not consider it a project
                    kotlin.runCatching {
                        Yaml.Default.decodeFromString<List<Project>>(it)
                    }.getOrNull() ?: kotlin.runCatching {
                        listOf(Yaml.Default.decodeFromString<Project>(it))
                    }.getOrNull() ?: throw AraraException(
                            LanguageController.messages.ERROR_INVALID_PROJECT_FILE)
                }
                ?: Project(
                        "Untitled",
                        workingDir,
                        emptySet()
                ).also { project ->
                    ConfigurationUtils.configFileForProject(project)?.let {
                        DisplayUtils.configurationFileName = it.toString()
                        ConfigurationUtils.load(it, project)
                    }
                }.let { project ->
                    listOf(Project(
                            project.name,
                            project.workingDirectory,
                            reference.map { fileName ->
                                FileSearching.resolveFile(
                                        fileName,
                                        workingDir,
                                        LinearExecutor.executionOptions
                                )
                            }.toSet()
                    ))
                }
    }

    /**
     * Update arara's configuration with the command line arguments.
     */
    private fun updateConfigurationFromCommandLine() {
        Session.userInterfaceOptions = UserInterfaceOptions(
                locale = language?.let { MPPLocale(it) }
                        ?: Session.userInterfaceOptions.locale,
                swingLookAndFeel = Session.userInterfaceOptions.swingLookAndFeel
        )
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)

        LinearExecutor.executionOptions = ExecutionOptions
                .from(LinearExecutor.executionOptions)
                .copy(
                        maxLoops = maxLoops
                                ?: LinearExecutor.executionOptions.maxLoops,
                        timeoutValue = timeout?.let { Duration.milliseconds(it) }
                                ?: LinearExecutor.executionOptions.timeoutValue,
                        verbose = if (verbose)
                            true
                        else
                            LinearExecutor.executionOptions.verbose,
                        executionMode = when {
                            dryRun -> ExecutionMode.DRY_RUN
                            safeRun -> ExecutionMode.SAFE_RUN
                            else -> LinearExecutor.executionOptions.executionMode
                        },
                        parseOnlyHeader = if (wholeFile)
                            false
                        else
                            LinearExecutor.executionOptions.parseOnlyHeader
                )

        Session.loggingOptions = LoggingOptions(
                enableLogging = if (log)
                    true
                else
                    Session.loggingOptions.enableLogging,
                appendLog = Session.loggingOptions.appendLog,
                logFile = Session.loggingOptions.logFile
        )
    }

    /**
     * Prepend potential preamble lines to the given directives.
     *
     * If there is no preamble given on command-line, returns the [directives],
     * otherwise checks existence of the preamble and returns the processed
     * preamble for a file of [fileType] prepended to [directives].
     */
    private fun prependPreambleDirectives(
        fileType: FileType,
        directives: List<Directive>
    ): List<Directive> {
        val resolvedPreamble = preamble ?: MvelState.defaultPreamble
        if (resolvedPreamble != null && resolvedPreamble !in MvelState.preambles) {
            throw AraraException(
                LanguageController.messages
                    .ERROR_PARSER_INVALID_PREAMBLE.format(resolvedPreamble))
        }
        val allDirectives = resolvedPreamble
                ?.takeIf { MvelState.preambles.containsKey(it) }
                ?.let { preambleName ->
                    Directives.extractDirectives(
                            MvelState.preambles.getValue(preambleName)
                                    .lines()
                                    .filterNot { it.isEmpty() },
                            true,
                            fileType
                    ).plus(directives)
                } ?: directives
        if (allDirectives.isEmpty())
            throw AraraException(
                LanguageController
                    .messages.ERROR_VALIDATE_NO_DIRECTIVES_FOUND)
        return allDirectives
    }

    /**
     * The actual main method of arara (when run in command-line mode)
     */
    override fun run() {
        // initializing logging has to come first; init() actually disables
        // the logging, so early exceptions won't generate a lot of noise in
        // the terminal
        LoggingUtils.init()

        // print the arara logo in the terminal; I just
        // hope people use this tool in a good terminal with
        // fixed-width fonts, otherwise the logo will be messed
        println("${DisplayUtils.logoString}\n")

        // start the internal stopwatch before any of arara's real working
        // starts
        val executionStart = TimeSource.Monotonic.markNow()

        // logging has to be initialized only once and for all because
        // context resets lead to missing output
        updateConfigurationFromCommandLine()
        LoggingUtils.setupLogging(Session.loggingOptions)

        // add all command line call parameters to the session
        parameters.forEach { (key, value) -> Session.put("arg:$key", value) }

        try {
            LinearExecutor.hooks = ExecutorHooks(
                    executeBeforeExecution = {
                        Session.updateEnvironmentVariables()
                        // directive processing has to be initialized, so that the core
                        // component respects our MVEL processing
                        DirectiveUtils.initializeDirectiveCore()
                    },
                    executeBeforeProject = {
                        // TODO: load config file here
                    },
                    executeBeforeFile = {
                        // TODO: do we have to reset some more file-specific config?
                        updateConfigurationFromCommandLine()
                        it.printFileInformation()
                    },
                    executeAfterFile = {
                        // add an empty line between file executions
                        println()
                    },
                    processDirectives = { file, list ->
                        DirectiveUtils.process(file, list
                                .takeIf { list.isNotEmpty() && !MvelState.prependPreambleIfDirectivesGiven }
                                ?: prependPreambleDirectives(file.fileType, list))
                    }
            )

            val projects = resolveProjects()
            if (LinearExecutor.execute(projects).exitCode != 0)
                ExecutionStatus.ExternalCallFailed()
            else
                ExecutionStatus.Processing()
        } catch (ex: AraraException) {
            // catch a propagated exception to replace intentionally left
            // out local treatment
            DisplayUtils.printException(ex)
            ExecutionStatus.CaughtException()
        }.let { executionStatus ->
            // print the execution time if the command line parsing does not
            // return false as result (it makes no sense to print the execution
            // time for a help message)
            DisplayUtils.printTime(executionStart.elapsedNow().toDouble(DurationUnit.SECONDS))

            throw ProgramResult(executionStatus.exitCode)
        }
    }
}

/**
 * Main method. This is the application entry point.
 * @param args A string array containing all command line arguments.
 */
fun main(args: Array<String>) {
    CLI().context {
        correctionSuggestor = { enteredValue, possibleValues ->
            possibleValues.filter { it.startsWith(enteredValue) }
        }
    }
            .completionOption(help = "Generate a completion script for arara. " +
                    "Add 'source <(arara --generate-completion <shell>)' " +
                    "to your shell's init file.")
            .versionOption(AraraAPI.version, names = setOf("-V", "--version"),
            message = {
                "${DisplayUtils.logoString}\n\n" +
                        "arara ${AraraAPI.version}\n" +
                        "Copyright (c) ${LocalDate.now().year}, Island of TeX\n" +
                        LanguageController.messages.INFO_PARSER_NOTES + "\n\n" +
                        "New features in version ${AraraAPI.version}:\n" +
                        CLI::class.java
                                .getResource("/org/islandoftex/arara/cli/configuration/release-notes")
                                .readText()
            })
            .main(args)
}
