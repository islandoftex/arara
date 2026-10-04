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
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.restrictTo
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import java.time.LocalDate
import java.util.Properties
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.configuration.ConfigurationFiles
import org.islandoftex.arara.cli.console.ConsoleMode
import org.islandoftex.arara.cli.ruleset.DirectiveProcessing
import org.islandoftex.arara.cli.ruleset.RuleEngine
import org.islandoftex.arara.cli.utils.Display
import org.islandoftex.arara.cli.utils.Logging
import org.islandoftex.arara.cli.utils.RunLog
import org.islandoftex.arara.cli.utils.StandardStreams
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.PreambleState
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.ExecutorHooks
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.TerminalDialogs
import org.islandoftex.arara.lua.LuaInterpreter

/**
 * arara's command line interface
 *
 * @author Island of TeX
 */
class CLI : CliktCommand("arara") {
    /**
     * The logger of the command line interface.
     */
    private val logger = KotlinLogging.logger {}

    init {
        context {
            terminal = Terminal(
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

    /**
     * Prints the usage help when arara is invoked without arguments.
     */
    override val printHelpOnEmptyArgs = true
    /**
     * The usage help, headed by the logo and a plain description of
     * the tool.
     */
    override fun help(context: Context) = """
        @```
        ${Display.logoString.lineSequence().joinToString("\n") { "@$it" }}
        @```
        @
        @The cool TeX automation tool.
        @
        @arara executes the TeX workflow you tell it to execute. Simply specify
        your needs within your TeX file and let arara do the work. These directives
        feature conditional execution and parameter expansion.
    """.trimMargin("@")

    /**
     * Whether the run writes its log file.
     */
    private val log by option("-l", "--log")
        .help("Generate a log output")
        .flag()
    /**
     * Whether the tool output echoes live; the silent alias
     * negates the request.
     */
    private val verbose by option("-v", "--verbose")
        .help("Print the command output")
        .flag("-s", "--silent")
    /**
     * Whether the run goes through every motion of its commands
     * without ever calling them.
     */
    private val dryRun by option("-n", "--dry-run")
        .help(
            "Go through all the motions of running a command, but " +
                "with no actual calls"
        )
        .flag()
    /**
     * Whether the run forbids the potentially harmful helpers.
     */
    private val safeRun by option("-S", "--safe-run")
        .help(
            "Run in safe mode and disable potentially harmful features. " +
                "Make sure your project uses only allowed features."
        )
        .flag()
    /**
     * Whether the directive extraction reads the whole file
     * instead of the header alone.
     */
    private val wholeFile by option("-w", "--whole-file")
        .help("Extract directives in the file, not only in the header")
        .flag()
    /**
     * The name of the configuration preamble this run asks for,
     * if any.
     */
    private val preamble by option("-p", "--preamble")
        .help("Set the file preamble based on the configuration file")
    /**
     * The wall clock span in milliseconds after which a running
     * system command is aborted.
     */
    private val timeout by option("-t", "--timeout")
        .help("Set the execution timeout (in milliseconds)")
        .int().restrictTo(min = 1)
    /**
     * The name of the language the console speaks.
     */
    private val language by option("-L", "--language")
        .help("Set the application language")
    /**
     * The maximum number of loops a looping directive may take.
     */
    private val maxLoops by option("-m", "--max-loops")
        .help("Set the maximum number of loops (> 0)")
        .int().restrictTo(min = 1)
    /**
     * The rule engine, and with it the dialect of the rule files
     * and the directive expressions.
     */
    private val engine by option("-e", "--engine")
        .help(
            "Set the rule engine and thus the rule format to use: " +
                "'yaml' (deprecated) or 'lua'"
        )
        .choice("yaml" to RuleEngine.YAML, "lua" to RuleEngine.LUA)
        .default(RuleEngine.YAML)

    /**
     * The console presentation this run asks for, if any.
     */
    private val console by option("-c", "--console")
        .help("Set the console presentation to use: 'plain' or 'styled'")
        .choice("plain" to ConsoleMode.PLAIN, "styled" to ConsoleMode.STYLED)
    /**
     * The working directory the run and its tools operate from.
     */
    private val workingDirectory by option("-d", "--working-directory")
        .help("Set the working directory for all tools")
        .path(mustExist = true, canBeFile = false, mustBeReadable = true)
    /**
     * The call parameters to hold in the session, keyed by name.
     */
    private val parameters: Map<String, String> by option("-P", "--call-property")
        .help("Pass parameters to the application to be used within the session.")
        .associate()
    /**
     * The properties file whose entries join the session.
     */
    private val properties: Properties? by option("-F", "--properties-file")
        .help("Pass a properties file to the application to be used within the session.")
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .convert { Properties().also { props ->
                ConfigurationFiles.loadProperties(props, it)
            } }

    /**
     * The file references the run evaluates.
     */
    private val reference by argument("file")
        .help("The file(s) to evaluate and process")
        .multiple(required = true)

    /**
     * Resolve the projects to run: a single Lua file argument may
     * name a project specification, and otherwise the called files
     * become one project of their own.
     */
    private fun resolveProjects(): List<org.islandoftex.arara.api.files.Project> {
        // resolve the working directory from the one that may be given
        // as command line parameter; otherwise resolve current directory
        val workingDir = (workingDirectory?.toMPPPath() ?: MPPPath("."))
            .normalize()

        return reference.singleOrNull()
            ?.takeIf { it.trim().endsWith(".lua") }
            ?.let { workingDir.resolve(it) }
            ?.takeIf { it.exists }?.readText()
            ?.let { luaScript ->
                resolveLuaProjectSpecification(workingDir, luaScript)
            }
            ?: Project(
                "Untitled",
                workingDir,
                emptySet()
            ).also { project ->
                loadProjectConfigurations(listOf(project))
            }.let { project ->
                listOf(
                    Project(
                        project.name,
                        project.workingDirectory,
                        reference.map { fileName ->
                            FileSearching.resolveFile(
                                fileName,
                                workingDir,
                                LinearExecutor.executionOptions
                            )
                        }.toSet()
                    )
                )
            }
    }

    /**
     * Resolves the projects a Lua file argument names, when the
     * single reference points at an existing script: the file is
     * offered to the project interpreter, and a script that turns
     * out not to be a project - or one that fails outright -
     * yields null so the caller falls back to ordinary input
     * treatment, with both verdicts spoken on the console and
     * recorded in the log.
     *
     * @param workingDir The working directory the script resolves
     *   its file references against.
     * @param luaScript The contents of the Lua file argument.
     * @return The projects the specification resolved to, or null
     *   when the file is not one.
     */
    private fun resolveLuaProjectSpecification(
        workingDir: MPPPath,
        luaScript: String
    ): List<org.islandoftex.arara.api.files.Project>? {
        return runCatching {
            logger.info {
                "Guessing provided Lua file is a project specification, " +
                    "trying to extract information from that."
            }
            LuaInterpreter(workingDir).parseProjectsFromLua(luaScript)
        }.getOrElse {
            if (it is AraraException) {
                Display.printWrapped(
                    LanguageController.messages
                        .infoInterpreterLuaFileNotAProject + "\n"
                )
                logger.info {
                    "Failed to parse Lua file argument as project: ${it.message}\n" +
                        "This file is now treated as regular input file. " +
                        "If it is a project specification, please fix the errors above."
                }
            } else {
                // an evaluation that crashed outright has to leave
                // a trace: the fallback keeps the run alive, but
                // the person waiting on the console must not watch
                // the file pass by in total silence
                Display.printWrapped(
                    LanguageController.messages
                        .infoInterpreterLuaFileEvaluationFailed + "\n"
                )
                logger.error {
                    "The provided Lua file raised " +
                        "${it::class.simpleName} during project " +
                        "resolution: ${it.message}. The file is " +
                        "now treated as regular input file."
                }
            }
            null
        }?.also { projects ->
            // project specifications do not go through the caller's
            // fallback branch, so the configuration file of every
            // project has to be loaded right here
            loadProjectConfigurations(projects)
        }
    }

    /**
     * Load the configuration file of each project, if any. The working
     * directory of a project decides where to look, so multi-directory
     * project specifications work as well.
     *
     * @param projects The projects to load the configuration for.
     */
    private fun loadProjectConfigurations(
        projects: List<org.islandoftex.arara.api.files.Project>
    ) {
        projects.forEach { project ->
            ConfigurationFiles.configFileForProject(project)?.let {
                Display.configurationFileName = it.toString()
                val resource = ConfigurationFiles.load(it, project)
                // the command line wins over the configuration file,
                // which wins over the plain default; a mode the
                // configuration names but the application does not
                // offer is an error
                applyConsoleMode(
                    ConsoleMode.resolve(console, resource.console)
                )
            }
        }
    }

    /**
     * The console presentation the run applied so far, so a
     * re-application of the same mode keeps the renderers - and
     * with them the run's ledger - untouched.
     */
    private var appliedConsoleMode: ConsoleMode? = null

    /**
     * The names of every file the run was asked to process, set
     * when the projects resolve, so the completion block can name
     * the files an exception left unreached.
     */
    private var allFileNames: List<String> = emptyList()

    /**
     * The names of the files that actually started processing,
     * collected by the per-file hook, so the completion block can
     * compute the difference.
     */
    private val processedFiles = mutableListOf<String>()

    /**
     * Applies the console presentation: the console renderer and the
     * dialog renderer of the mode, so the run and its conversations
     * share one presentation, and the provenance line naming the
     * presentation in the log. The renderer holds the run's ledger,
     * so re-applying the same mode must not reset it - the
     * renderers are recreated only when the presentation genuinely
     * changes.
     *
     * @param mode The console mode to apply.
     */
    private fun applyConsoleMode(mode: ConsoleMode) {
        if (appliedConsoleMode == mode) {
            return
        }
        appliedConsoleMode = mode
        logConsoleMode(mode)
        Display.renderer = mode.renderer(terminal)
        TerminalDialogs.renderer = mode.dialogRenderer(terminal)
    }

    /**
     * Logs the console presentation the run renders through: the
     * provenance names plain and styled alike, and the degradation an
     * undecorated stream forces on the styled one - a fact no other
     * record carries. The startup application happens before the
     * log backend exists, so the run names its effective
     * presentation again once the backend is in place; every later
     * genuine change names itself at its application.
     *
     * @param mode The console mode to name.
     */
    private fun logConsoleMode(mode: ConsoleMode) {
        logger.info {
            val degraded = mode == ConsoleMode.STYLED &&
                terminal.terminalInfo.ansiLevel == AnsiLevel.NONE
            "Console set to ${mode.name.lowercase()}" +
                if (degraded) " (degraded)" else ""
        }
    }

    /**
     * Update the session with the command line's own contributions:
     * the key/value pairs of the properties file, if provided, and
     * the call parameters, which take precedence over the
     * properties.
     */
    private fun updateSessionFromCommandLine() {
        properties?.let { props ->
            ConfigurationFiles.toMap(props)
                .forEach { (key, value) ->
                    Session.put("arg:$key", value)
                }
        }

        parameters.forEach { (key, value) -> Session.put("arg:$key", value) }
    }

    /**
     * Update arara's configuration with the command line arguments.
     */
    private fun updateConfigurationFromCommandLine() {
        RuleEngine.current = engine
        // logged per file so a log file stays self-describing about the
        // rule format the run used
        logger.info { "Rule engine set to ${engine.name.lowercase()}" }

        // the command line presentation always wins, whatever the
        // configuration file says, so it re-applies per file like
        // every other command line setting
        console?.let { applyConsoleMode(it) }

        // the command line owns the language alone; every other
        // member carries from the configuration, so a fresh
        // construction must state them explicitly - a silent reset
        // here would quietly bury what the user configured
        Session.userInterfaceOptions = UserInterfaceOptions(
            locale = language?.let { MPPLocale(it) }
                ?: Session.userInterfaceOptions.locale,
            terminalOutputWidth = Session.userInterfaceOptions.terminalOutputWidth,
            enableAnimations = Session.userInterfaceOptions.enableAnimations,
        )
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)

        LinearExecutor.executionOptions = ExecutionOptions
            .from(LinearExecutor.executionOptions)
            .copy(
                maxLoops = maxLoops
                    ?: LinearExecutor.executionOptions.maxLoops,
                timeoutValue = timeout?.let { it.milliseconds }
                    ?: LinearExecutor.executionOptions.timeoutValue,
                verbose = verbose ||
                    LinearExecutor.executionOptions.verbose,
                executionMode = when {
                    dryRun -> ExecutionMode.DRY_RUN
                    safeRun -> ExecutionMode.SAFE_RUN
                    else -> LinearExecutor.executionOptions.executionMode
                },
                parseOnlyHeader = !wholeFile &&
                    LinearExecutor.executionOptions.parseOnlyHeader
            )

        Session.loggingOptions = LoggingOptions(
            enableLogging = log ||
                Session.loggingOptions.enableLogging,
            appendLog = Session.loggingOptions.appendLog,
            logFile = Session.loggingOptions.logFile
        )
    }

    /**
     * Prepend potential preamble lines to the given directives.
     *
     * The preamble the command line names wins; without one, the
     * default preamble of the configuration applies; with
     * neither, the directives pass through unchanged. A named
     * preamble the configuration does not define is an error,
     * and an empty result means the file asked for nothing at
     * all.
     */
    private fun prependPreambleDirectives(
        fileType: FileType,
        directives: List<Directive>
    ): List<Directive> {
        val resolvedPreamble = preamble ?: PreambleState.defaultPreamble
        if (resolvedPreamble != null && resolvedPreamble !in PreambleState.preambles) {
            throw AraraException(
                LanguageController.messages
                    .errorParserInvalidPreamble(resolvedPreamble)
            )
        }
        val allDirectives = resolvedPreamble
            ?.takeIf { PreambleState.preambles.containsKey(it) }
            ?.let { preambleName ->
                Directives.extractDirectives(
                    PreambleState.preambles.getValue(preambleName)
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
        // initializing logging has to come first; init() actually disables
        // the logging, so early exceptions won't generate a lot of noise in
        // the terminal
        Logging.init()

        // the effective presentation applies before the run greets
        // - the one the command line names, or the plain default -
        // so the banner and every conversation already belong to it
        // and the provenance names it from the start; a
        // configuration-selected presentation applies when the
        // projects resolve, from the first file onward
        applyConsoleMode(console ?: ConsoleMode.PLAIN)

        Display.printBanner()

        val executionStart = TimeSource.Monotonic.markNow()
        updateConfigurationFromCommandLine()

        // logging has to be initialized only once and for all because
        // context resets lead to missing output
        Logging.setupLogging(Session.loggingOptions)

        // the startup application of the presentation happens before
        // the log backend exists; the provenance names the effective
        // presentation once the backend is in place, and every later
        // genuine change names itself at its application
        logConsoleMode(appliedConsoleMode!!)

        updateSessionFromCommandLine()

        try {
            LinearExecutor.hooks = ExecutorHooks(
                executeBeforeExecution = {
                    Session.updateEnvironmentVariables()
                    // directive processing has to be initialized, so that the core
                    // component respects our MVEL processing
                    DirectiveProcessing.initializeDirectiveCore()
                },
                executeBeforeProject = {
                    // the configuration file of each project is loaded in
                    // resolveProjects, where it has to be available before
                    // the file references are resolved against the file
                    // types the configuration may provide
                },
                executeBeforeFile = {
                    processedFiles.add(it.path.fileName)
                    updateConfigurationFromCommandLine()
                    Display.printFileInformation(it)
                },
                executeAfterFile = {
                    // the closing blank between file executions
                    // prints only when the console showed something
                    // for the file: a file whose directives were all
                    // skipped or absent already ends on its header's
                    // own blank, and a second one would stack
                    if (Display.printedForFile) {
                        println()
                    }
                },
                processDirectives = { file, list ->
                    DirectiveProcessing.process(
                        file,
                        list
                            // an explicit command line preamble always
                            // applies - the policy below governs the
                            // configuration's standing default only
                            .takeIf {
                                list.isNotEmpty() && preamble == null &&
                                    !PreambleState.prependPreambleIfDirectivesGiven
                            }
                            ?: prependPreambleDirectives(file.fileType, list)
                    )
                }
            )

            val projects = resolveProjects()
            allFileNames = projects.flatMap { it.files }.map { it.path.fileName }
            if (LinearExecutor.execute(projects).exitCode != 0)
                ExecutionStatus.ExternalCallFailed()
            else
                ExecutionStatus.Processing()
        } catch (ex: AraraException) {
            // catch a propagated exception to replace intentionally left
            // out local treatment
            Display.printException(ex)
            ExecutionStatus.CaughtException()
        }.let { executionStatus ->
            // the run's status carries its completion: the summary
            // speaks the verdict, the unreached files are named,
            // the time line closes the run and its code leaves the
            // process - whichever way the run ended
            Display.printRunSummary(executionStatus.exitCode == 0)

            // the files the exception left unreached are named
            // between the summary and the closing time
            val unprocessed = allFileNames.filter { it !in processedFiles }
            if (unprocessed.isNotEmpty()) {
                Display.printUnprocessedFiles(unprocessed)
            }

            Display.printTime(executionStart.elapsedNow().toDouble(DurationUnit.SECONDS))

            // the closing entry records the verdict in the log; the
            // console has already said everything it needs to
            RunLog.executionOutcome(executionStatus.exitCode)

            throw ProgramResult(executionStatus.exitCode)
        }
    }
}

/**
 * Main method. This is the application entry point.
 * @param args A string array containing all command line arguments.
 */
fun main(args: Array<String>) {
    // the standard streams speak UTF-8 to a terminal that
    // expects it, before anything downstream ever prints
    StandardStreams.adaptForTerminal()
    // the logging facade announces its detected backend once per
    // virtual machine by default; the command line owns its
    // output surface, so the announcement is turned off up front
    KotlinLoggingConfiguration.logStartupMessage = false
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
                "${Display.logoString}\n\n" +
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
