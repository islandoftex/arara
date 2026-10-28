// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.installMordantMarkdown
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import java.time.LocalDate
import java.util.Properties
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.rules.RuleDiscoveryResult
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.configuration.ConfigurationFiles
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.cli.configuration.ExecutionFlags
import org.islandoftex.arara.cli.configuration.InvocationFlags
import org.islandoftex.arara.cli.configuration.LoggingFlags
import org.islandoftex.arara.cli.configuration.PresentationFlags
import org.islandoftex.arara.cli.configuration.WatchFlags
import org.islandoftex.arara.cli.console.ConsoleMode
import org.islandoftex.arara.cli.ruleset.RuleDiscovery
import org.islandoftex.arara.cli.ruleset.RuleQuery
import org.islandoftex.arara.cli.ruleset.RulesCommand
import org.islandoftex.arara.cli.ui.TerminalDialogRenderer
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.EventLog
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.cli.utils.RunLog
import org.islandoftex.arara.cli.utils.StandardStreams
import org.islandoftex.arara.cli.utils.WindowsProcessJob
import org.islandoftex.arara.cli.utils.reportBuildWorkspace
import org.islandoftex.arara.cli.watch.WatchGraph
import org.islandoftex.arara.cli.watch.WatchProjectConfiguration
import org.islandoftex.arara.cli.watch.WatchProjectLoader
import org.islandoftex.arara.cli.watch.WatchReporter
import org.islandoftex.arara.cli.watch.WatchSession
import org.islandoftex.arara.cli.watch.WatchSettings
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.Dialogs
import org.islandoftex.arara.lua.LuaInterpreter

/**
 * arara's command line interface
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
// Options and their configuration application belong to the same command.

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
        subcommands(RulesCommand {
            queryRules(
                it, invocationFlags.workingDirectory, invocationFlags.engine, ::updateConfigurationFromCommandLine)
        })
    }

    /** Process files when no subcommand was selected. */
    override val invokeWithoutSubcommand = true

    /** Show usage when no input files or project specifications were supplied. */
    override val printHelpOnEmptyArgs = true
    /** Describe the tool and its supported conditional operators. */
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

    private val invocationFlags by InvocationFlags()
    private val executionFlags by ExecutionFlags()
    private val loggingFlags by LoggingFlags()
    private val presentationFlags by PresentationFlags()
    private val watchFlags by WatchFlags()

    private val projectResolver
        get() = ProjectResolver(
            invocationFlags.workingDirectory,
            { project, snapshot -> configureProject(project, snapshot) },
            ::updateConfigurationFromCommandLine
        )

    private var watchConfiguration = emptyMap<String, WatchProjectConfiguration>()

    private var appliedConsole: ConsoleMode? = null

    private fun applyConsole() {
        val mode = ConsoleMode.resolve(presentationFlags.console, Session.userInterfaceOptions.consoleMode)
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
        .multiple(required = false)

    /**
     * Update arara's configuration with the command line arguments.
     */
    private fun updateConfigurationFromCommandLine() {
        Session.userInterfaceOptions = UserInterfaceOptions(
            locale = presentationFlags.language?.let { MPPLocale(it) }
                ?: Session.userInterfaceOptions.locale,
            terminalOutputWidth = presentationFlags.terminalWidth ?: Session.userInterfaceOptions.terminalOutputWidth,
            terminalOutputWidthLimit = presentationFlags.terminalWidth
                ?: Session.userInterfaceOptions.terminalOutputWidthLimit,
            consoleMode = Session.userInterfaceOptions.consoleMode,
            enableAnimations = Session.userInterfaceOptions.enableAnimations
        )
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)

        LinearExecutor.executionOptions = executionFlags.apply(LinearExecutor.executionOptions)

        Session.loggingOptions = LoggingOptions(
            appendLog = Session.loggingOptions.appendLog,
            logFile = Session.loggingOptions.logFile,
            format = loggingFlags.logFormat ?: loggingFlags.log ?: Session.loggingOptions.format,
            level = loggingFlags.logLevel ?: Session.loggingOptions.level,
            retention = loggingFlags.logRetention ?: Session.loggingOptions.retention
        )
    }

    /**
     * The actual main method of arara (when run in command-line mode)
     */
    override fun run() {
        if (currentContext.invokedSubcommand != null) {
            if (watchFlags.watch) throw UsageError(LanguageController.messages.errorWatchArguments)
            return
        }
        if (reference.isEmpty()) throw UsageError("Missing argument file")
        if (watchFlags.watch && (invocationFlags.cleanBuild || reference.any { !it.endsWith(".proj.lua") })) {
            throw UsageError(LanguageController.messages.errorWatchArguments)
        }
        val configuration = ConfigurationSnapshot.capture()
        val previousRun = LoggingUtils.beginRun()
        val previousHooks = LinearExecutor.hooks
        val previousRenderer = DisplayUtils.renderer
        val previousPresentation = DisplayUtils.presentation
        DisplayUtils.presentation = org.islandoftex.arara.core.reporting.RunLedger()
        val previousDialogs = Dialogs.renderer
        appliedConsole = null
        DisplayUtils.renderer = org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer()
        var successful = false
        try {
            runSession(configuration)
        } catch (result: ProgramResult) {
            successful = result.statusCode == 0
            throw result
        } finally {
            try {
                DisplayUtils.renderer.close()
            } finally {
                LoggingUtils.finishRun(successful)
                LoggingUtils.endWatch()
                watchConfiguration = emptyMap()
                configuration.restore(configureLogging = false)
                LoggingUtils.resumeLogging(configuration.logging)
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
        configuration: ConfigurationSnapshot,
        configureLogging: Boolean = true
    ) {
        val frozen = watchConfiguration[project.name]
        (frozen?.snapshot ?: configuration).restore(configureLogging = false)
        LanguageController.loadMessagesFor(presentationFlags.language?.let { MPPLocale(it) }
            ?: Session.userInterfaceOptions.locale)
        val file = if (frozen != null) frozen.file else ConfigurationFiles.configFileForProject(project)
        DisplayUtils.configurationFileName = file?.toString() ?: "[none]"
        if (frozen == null && file != null) ConfigurationFiles.load(file, project, configureLogging = false)
        updateConfigurationFromCommandLine()
        if (configureLogging) initializeRuntime()
        applyConsole()
    }

    private fun prepareProjects(
        projects: List<org.islandoftex.arara.api.files.Project>,
        configuration: ConfigurationSnapshot,
        configureLogging: Boolean = true
    ): List<org.islandoftex.arara.api.files.Project> = try {
        projects.map { project ->
            configureProject(project, configuration, configureLogging)
            LuaInterpreter.resolvePendingFileTypes(project, LinearExecutor.executionOptions)
        }
    } finally {
        configuration.restore(configureLogging = false)
        updateConfigurationFromCommandLine()
    }

    private fun runWatch(configuration: ConfigurationSnapshot): ExecutionStatus {
        val root = (invocationFlags.workingDirectory ?: MPPPath(".")).normalize()
        val specifications = reference.map { root.resolve(it).normalize() }.toSet()
        val fallback = WatchGraph(emptyList(), specifications +
            ConfigurationFiles.candidatesForProject(Project("Watch", root, emptySet())))
        LoggingUtils.beginWatch()
        return ExecutionStatus.FinishedWithCode(WatchSession(
            fallback = fallback,
            load = WatchProjectLoader(
                specifications,
                { prepareProjects(projectResolver.resolveProjects(reference, configuration), configuration, false) },
                { configureProject(it, configuration, false) },
                { configuration.restore(configureLogging = false); updateConfigurationFromCommandLine() }
            )::load,
            execute = { graph, selected, previous, number ->
                watchConfiguration = graph.configuration
                try {
                    graph.projects.firstOrNull { it.name in selected }?.let { configureProject(it, configuration) }
                    EventLog.info("watch.cycle.started", "Watch build $number",
                        mapOf("cycle" to number, "projects" to selected))
                    DisplayUtils.presentation.prepare(graph.projects, selected)
                    val results = LinearExecutor.executeWatchCycle(graph.projects, selected, previous) { _, exception ->
                        DisplayUtils.printException(exception)
                    }
                    val code = if (selected.any { results[it] != 0 }) 1 else 0
                    DisplayUtils.printRunSummary(code == 0)
                    results
                } finally {
                    watchConfiguration = emptyMap()
                }
            },
            report = WatchReporter(),
            settings = WatchSettings(
                watchFlags.watchMode, watchFlags.watchDebounce.toLong(), watchFlags.watchInterval.toLong())
        ).run())
    }

    private fun runProjects(configuration: ConfigurationSnapshot): ExecutionStatus {
        val projects = prepareProjects(projectResolver.resolveProjects(reference, configuration), configuration)
        DisplayUtils.presentation.prepare(projects)
        return executeProjects(projects, invocationFlags.cleanBuild)
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
        initializeRuntime()

        // add all key/value pairs found in the properties file, if
        // provided in the command line
        invocationFlags.properties
            ?.let { path -> Properties().apply { ConfigurationFiles.loadProperties(this, path) } }
            ?.let(ConfigurationFiles::toMap)
            ?.forEach { (key, value) -> Session.put("arg:$key", value) }

        // add all command line call parameters to the session
        // (command line parameters take precedence over properties)
        invocationFlags.parameters.forEach { (key, value) -> Session.put("arg:$key", value) }

        try {
            LinearExecutor.hooks = executionHooks(
                invocationFlags.engine, watchFlags.watch, executionFlags.preamble,
                ExecutionConfiguration(
                    { configureProject(it, configuration) }, ::updateConfigurationFromCommandLine,
                    { checkNotNull(appliedConsole) }
                )
            )

            if (watchFlags.watch) runWatch(configuration) else runProjects(configuration)
        } catch (ex: AraraException) {
            // catch a propagated exception to replace intentionally left
            // out local treatment
            DisplayUtils.printException(ex)
            ExecutionStatus.CaughtException()
        }.let { executionStatus ->
            // print the execution time if the command line parsing does not
            // return false as result (it makes no sense to print the execution
            // time for a help message)
            if (watchFlags.watch) WindowsProcessJob.finish() else finishRuntime(executionStatus.exitCode)
            DisplayUtils.printTime(executionStart.elapsedNow().toDouble(DurationUnit.SECONDS))

            throw ProgramResult(executionStatus.exitCode)
        }
    }
}

/**
 * Main method. This is the application entry point.
 * @param args A string array containing all command line arguments.
 */
fun main(args: Array<String>) = StandardStreams.withTerminalEncoding {
    val startupMessage = KotlinLoggingConfiguration.logStartupMessage
    try {
        KotlinLoggingConfiguration.logStartupMessage = false
        runCommandLine(args)
    } finally {
        KotlinLoggingConfiguration.logStartupMessage = startupMessage
    }
}

private fun runCommandLine(args: Array<String>) {
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

private fun initializeRuntime() {
    LoggingUtils.setupLogging(Session.loggingOptions)
    WindowsProcessJob.install()
}

private fun finishRuntime(exitCode: Int) {
    WindowsProcessJob.finish()
    RunLog.executionOutcome(exitCode)
    DisplayUtils.printRunSummary(exitCode == 0)
}

private fun executeProjects(
    projects: List<org.islandoftex.arara.api.files.Project>, cleanBuild: Boolean
): ExecutionStatus {
    if (cleanBuild) {
        org.islandoftex.arara.core.files.BuildWorkspaces.validate(projects, ::reportBuildWorkspace)
        projects.forEach { org.islandoftex.arara.core.files.BuildWorkspaces.clean(it, ::reportBuildWorkspace) }
        return ExecutionStatus.FinishedWithCode(0)
    }
    return ExecutionStatus.FinishedWithCode(LinearExecutor.execute(projects).exitCode)
}

private fun queryRules(
    query: RuleQuery, workingDirectory: MPPPath?, engine: RuleEngine, configure: () -> Unit
): RuleDiscoveryResult {
    val configuration = ConfigurationSnapshot.capture()
    val directory = (query.directory ?: workingDirectory ?: MPPPath(".")).normalize()
    try {
        val project = Project("Rule discovery", directory, emptySet(), emptySet())
        ConfigurationFiles.configFileForProject(project)?.let {
            ConfigurationFiles.load(it, project, configureLogging = false, scriptOutputToStderr = true)
        }
        configure()
        val preferred = query.engine ?: engine
        return query.info?.let {
            RuleDiscovery.inspect(it, LinearExecutor.executionOptions, directory, preferred)
        } ?: RuleDiscovery.discover(LinearExecutor.executionOptions, directory, preferred).let { result ->
            result.copy(rules = result.rules.filter {
                (query.all || it.effective) &&
                    (query.filter == null || it.identifier.contains(query.filter, ignoreCase = true))
            })
        }
    } finally {
        configuration.restore(configureLogging = false)
    }
}
