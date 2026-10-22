// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.installMordantMarkdown
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.restrictTo
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.configuration.ConfigurationFiles
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.console.ConsoleMode
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.cli.ruleset.RuleEngine
import org.islandoftex.arara.cli.ui.TerminalDialogRenderer
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.cli.utils.RunLog
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
import org.islandoftex.arara.core.ui.Dialogs
import org.islandoftex.arara.lua.LuaInterpreter
import org.islandoftex.arara.mvel.utils.MvelState
import java.time.LocalDate
import java.util.Properties
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

/**
 * arara's command line interface
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
class CLI : CliktCommand("arara") {

    init {
        context {
            // Mordant's JVM fallback can infer an interactive terminal from CI environment variables.
            val hasConsole = System.console() != null
            terminal = Terminal(
                ansiLevel = if (hasConsole) null else AnsiLevel.NONE,
                hyperlinks = if (hasConsole) null else false,
                interactive = hasConsole,
                theme = Theme {
                    flags["markdown.code.block.border"] = false
                }
            )
            helpFormatter = {
                MordantHelpFormatter(it)
            }
        }
        installMordantMarkdown()
    }

    override val printHelpOnEmptyArgs = true
    override fun help(context: Context) = """
        @```
        ${DisplayUtils.logoString.lineSequence().joinToString("\n") { "@$it" }}
        @```
        @
        @The cool TeX automation tool.
        @
        @arara executes the TeX workflow you tell it to execute. Simply specify
        your needs within your TeX file and let arara do the work. These directives
        feature conditional execution and parameter expansion.
    """.trimMargin("@")

    private val engine by option("--engine").help("Rule engine (default: yaml)")
        .choice("yaml" to RuleEngine.YAML, "lua" to RuleEngine.LUA).default(RuleEngine.YAML)

    private val log by option("-l", "--log")
        .help("Write a text diagnostic log (shorthand for --log-format=text)")
        .switch("-l" to LogFormat.TEXT, "--log" to LogFormat.TEXT)
    private val verbose by option("-v", "--verbose")
        .help("Print the command output")
        .flag()
    private val silent by option("-s", "--silent")
        .help("Suppress command output, overriding configuration and --verbose")
        .flag()
    private val dryRun by option("-n", "--dry-run")
        .help(
            "Go through all the motions of running a command, but " +
                "with no actual calls"
        )
        .flag()
    private val safeRun by option("-S", "--safe-run")
        .help(
            "Run in safe mode and disable potentially harmful features. " +
                "Make sure your projects uses only allowed features."
        )
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
    private val properties: Properties? by option("-F", "--properties-file")
        .help("Pass a properties file to the application to be used within the session.")
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .convert { Properties().apply { ConfigurationFiles.loadProperties(this, it) } }

    private val logFormat by option("--log-format")
        .help("Set the diagnostic log format: none (default), text or json")
        .choice("none" to LogFormat.NONE, "text" to LogFormat.TEXT, "json" to LogFormat.JSON)

    private val console by option("-c", "--console")
        .help("Set the console presentation: plain or styled")
        .choice("plain" to ConsoleMode.PLAIN, "styled" to ConsoleMode.STYLED)

    private var appliedConsole: ConsoleMode? = null

    private fun applyConsole() {
        val mode = ConsoleMode.resolve(console, Session.userInterfaceOptions.consoleMode)
        if (mode != appliedConsole) {
            DisplayUtils.renderer.close()
            DisplayUtils.renderer = mode.renderer(terminal)
            Dialogs.renderer = when (mode) {
                ConsoleMode.PLAIN -> TerminalDialogRenderer(beforeDialog = { DisplayUtils.renderer.suspendProgress() })
                ConsoleMode.STYLED -> org.islandoftex.arara.cli.console.styled.StyledDialogRenderer(
                    terminal, beforeDialog = { DisplayUtils.renderer.suspendProgress() }
                )
            }
            appliedConsole = mode
        }
    }

    private val reference by argument("file")
        .help("The file(s) to evaluate and process")
        .multiple(required = true)

    /** Specifications share a graph; loose inputs form a final anonymous project. */
    private fun resolveProjects(configuration: ConfigurationSnapshot): List<org.islandoftex.arara.api.files.Project> {
        val workingDir = (workingDirectory?.toMPPPath() ?: MPPPath(".")).normalize()
        val (specifications, inputs) = reference.partition { it.endsWith(".proj.lua") }
        val projects = specifications.flatMap { specification ->
            val path = workingDir.resolve(specification)
            LuaInterpreter(workingDir).parseProjectsFromLua(
                path.readText(), path.fileName.removeSuffix(".proj.lua"), path.toString()
            )
        }
        // Specification dependencies refer only to named specification projects.
        org.islandoftex.arara.core.dependencies.ProjectValidation.executionOrder(projects)
        return if (inputs.isEmpty()) projects else projects + resolveLooseInputs(
            inputs, workingDir, configuration, projects.map { it.name }.toSet()
        )
    }

    private fun resolveLooseInputs(
        inputs: List<String>,
        workingDir: MPPPath,
        configuration: ConfigurationSnapshot,
        dependencies: Set<String>
    ): org.islandoftex.arara.api.files.Project {
        val project = Project("Single files", workingDir, emptySet(), dependencies)
        return try {
            configureProject(project, configuration)
            val files = inputs.map { FileSearching.resolveFile(it, workingDir, LinearExecutor.executionOptions) }
            if (files.map { it.path.normalize() }.toSet().size != files.size) {
                throw AraraException("Loose inputs refer to the same file more than once.")
            }
            Project(project.name, workingDir, files.toSet(), dependencies)
        } finally {
            configuration.restore(configureLogging = false)
            updateConfigurationFromCommandLine()
        }
    }

    /**
     * Update arara's configuration with the command line arguments.
     */
    private fun updateConfigurationFromCommandLine() {
        Session.userInterfaceOptions = UserInterfaceOptions(
            locale = language?.let { MPPLocale(it) }
                ?: Session.userInterfaceOptions.locale,
            terminalOutputWidth = Session.userInterfaceOptions.terminalOutputWidth,
            consoleMode = Session.userInterfaceOptions.consoleMode,
            enableAnimations = Session.userInterfaceOptions.enableAnimations
        )
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)

        LinearExecutor.executionOptions = ExecutionOptions
            .from(LinearExecutor.executionOptions)
            .copy(
                maxLoops = maxLoops
                    ?: LinearExecutor.executionOptions.maxLoops,
                timeoutValue = timeout?.let { it.milliseconds }
                    ?: LinearExecutor.executionOptions.timeoutValue,
                verbose = when {
                    silent -> false
                    verbose -> true
                    else -> LinearExecutor.executionOptions.verbose
                },
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
            appendLog = Session.loggingOptions.appendLog,
            logFile = Session.loggingOptions.logFile,
            format = logFormat ?: log ?: Session.loggingOptions.format
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
                    .errorParserInvalidPreamble(resolvedPreamble)
            )
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
                    .messages.errorValidateNoDirectivesFound
            )
        return allDirectives
    }

    /**
     * The actual main method of arara (when run in command-line mode)
     */
    override fun run() {
        val configuration = ConfigurationSnapshot.capture()
        val previousRun = LoggingUtils.beginRun()
        val previousHooks = LinearExecutor.hooks
        val previousRenderer = DisplayUtils.renderer
        val previousPresentation = DisplayUtils.presentation
        DisplayUtils.presentation = org.islandoftex.arara.cli.console.RunPresentation()
        val previousDialogs = Dialogs.renderer
        appliedConsole = null
        DisplayUtils.renderer = org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer()
        try {
            runSession(configuration)
        } finally {
            try {
                DisplayUtils.renderer.close()
            } finally {
                configuration.restore()
                LinearExecutor.hooks = previousHooks
                DisplayUtils.renderer = previousRenderer
                DisplayUtils.presentation = previousPresentation
                Dialogs.renderer = previousDialogs
                LoggingUtils.restoreRun(previousRun)
            }
        }
    }

    private fun configureProject(
        project: org.islandoftex.arara.api.files.Project,
        configuration: ConfigurationSnapshot
    ) {
        configuration.restore(configureLogging = false)
        LanguageController.loadMessagesFor(language?.let { MPPLocale(it) } ?: Session.userInterfaceOptions.locale)
        ConfigurationFiles.configFileForProject(project)?.let {
            DisplayUtils.configurationFileName = it.toString()
            ConfigurationFiles.load(it, project, configureLogging = false)
        }
        updateConfigurationFromCommandLine()
        LoggingUtils.setupLogging(Session.loggingOptions)
        applyConsole()
    }

    private fun prepareProjects(
        projects: List<org.islandoftex.arara.api.files.Project>,
        configuration: ConfigurationSnapshot
    ): List<org.islandoftex.arara.api.files.Project> = try {
        projects.map { project ->
            configureProject(project, configuration)
            LuaInterpreter.resolvePendingFileTypes(project, LinearExecutor.executionOptions)
        }
    } finally {
        configuration.restore(configureLogging = false)
        updateConfigurationFromCommandLine()
    }

    private fun runSession(configuration: ConfigurationSnapshot) {
        // initializing logging has to come first; init() actually disables
        // the logging, so early exceptions won't generate a lot of noise in
        // the terminal
        LoggingUtils.init()

        applyConsole()
        DisplayUtils.printBanner()

        val executionStart = TimeSource.Monotonic.markNow()
        updateConfigurationFromCommandLine()

        // logging has to be initialized only once and for all because
        // context resets lead to missing output
        LoggingUtils.setupLogging(Session.loggingOptions)

        // add all key/value pairs found in the properties file, if
        // provided in the command line
        properties?.let(ConfigurationFiles::toMap)?.forEach { (key, value) -> Session.put("arg:$key", value) }

        // add all command line call parameters to the session
        // (command line parameters take precedence over properties)
        parameters.forEach { (key, value) -> Session.put("arg:$key", value) }

        try {
            LinearExecutor.hooks = ExecutorHooks(
                executeBeforeExecution = {
                    RunLog.executionStarted(AraraAPI.version)
                    Session.updateEnvironmentVariables()
                    // directive processing has to be initialized, so that the core
                    // component respects our MVEL processing
                    DirectiveUtils.initializeDirectiveCore(engine)
                },
                executeBeforeProject = { project ->
                    configureProject(project, configuration)
                    RunLog.project(project.name, project.workingDirectory.toString())
                    DisplayUtils.renderer.printProjectInformation(project.name, project.workingDirectory.toString())
                },
                executeBeforeFile = {
                    // TODO: do we have to reset some more file-specific config?
                    updateConfigurationFromCommandLine()
                    it.printFileInformation()
                },
                executeAfterFile = { report ->
                    DisplayUtils.printFileResult(report.exitCode)
                    // add an empty line between file executions
                    println()
                },
                processDirectives = { file, list ->
                    DirectiveUtils.process(
                        file,
                        list
                            .takeIf {
                                list.isNotEmpty() && !MvelState.prependPreambleIfDirectivesGiven && preamble == null
                            }
                            ?: prependPreambleDirectives(file.fileType, list),
                        engine
                    )
                }
            )

            val projects = prepareProjects(resolveProjects(configuration), configuration)
            DisplayUtils.presentation.prepare(projects)
            ExecutionStatus.FinishedWithCode(LinearExecutor.execute(projects).exitCode)
        } catch (ex: AraraException) {
            // catch a propagated exception to replace intentionally left
            // out local treatment
            DisplayUtils.printException(ex)
            ExecutionStatus.CaughtException()
        }.let { executionStatus ->
            // print the execution time if the command line parsing does not
            // return false as result (it makes no sense to print the execution
            // time for a help message)
            RunLog.executionOutcome(executionStatus.exitCode)
            DisplayUtils.printRunSummary(executionStatus.exitCode == 0)
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
        suggestTypoCorrection = { enteredValue, possibleValues ->
            possibleValues.filter { it.startsWith(enteredValue) }
        }
    }
        .completionOption(
            help = "Generate a completion script for arara. " +
                "Add 'source <(arara --generate-completion <shell>)' " +
                "to your shell's init file."
        )
        .versionOption(
            AraraAPI.version, names = setOf("-V", "--version"),
            message = {
                "${DisplayUtils.logoString}\n\n" +
                    "arara ${AraraAPI.version}\n" +
                    "Copyright (c) ${LocalDate.now().year}, Island of TeX\n" +
                    LanguageController.messages.infoParserNotes + "\n\n" +
                    "New features in version ${AraraAPI.version}:\n" +
                    CLI::class.java
                        .getResource("/org/islandoftex/arara/cli/configuration/release-notes")
                        .readText()
            }
        )
        .main(args)
}
