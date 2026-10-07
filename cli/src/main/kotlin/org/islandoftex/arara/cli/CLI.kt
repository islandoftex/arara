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
// the command line class carries its option surface and the run
// orchestration side by side: the count is intrinsic to the entry
// point, not accumulated responsibilities
@Suppress("TooManyFunctions")
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
     * Resolve the projects to run: the `.proj.lua` arguments name
     * project specifications, and the remaining arguments form the
     * anonymous project of the loose input files. The
     * specifications merge into one dependency graph with names
     * unique among the specification projects - every invocation
     * is a set of projects, named ones from the specifications
     * and at most one anonymous one from the command line.
     *
     * @return The projects of the run.
     */
    private fun resolveProjects(): List<org.islandoftex.arara.api.files.Project> {
        // the invocation's state resets with every resolution: a
        // reused instance must not carry the previous run's
        // anonymous project, its processed files or its expected
        // file names into this one - a resolution that fails would
        // otherwise report the previous run's files as unreached
        anonymousProject = null
        processedFiles.clear()
        allFileNames = emptyList()
        Display.projectCount = 0
        // resolve the working directory from the one that may be given
        // as command line parameter; otherwise resolve current directory
        val workingDir = (workingDirectory?.toMPPPath() ?: MPPPath("."))
            .normalize()

        // a .proj.lua reference names a project specification;
        // everything else is an input file
        val (specifications, inputs) = reference.partition {
            it.trim().endsWith(".proj.lua")
        }
        val projects =
            resolveProjectSpecifications(workingDir, specifications)
                .toMutableList()

        // a specification always yields at least one project, so
        // the loose input files alone decide whether the run
        // carries an anonymous project
        if (inputs.isNotEmpty()) {
            appendAnonymousProject(projects, inputs, workingDir)
        }
        validateNoNameCollision(projects)
        validateNoSharedFile(projects)
        return projects
    }

    /**
     * Builds the anonymous project that wraps the loose input
     * files, loading the invocation directory's configuration
     * first so the file references resolve against the types
     * the configuration provides.
     *
     * @param projects The project list the anonymous project joins.
     * @param inputs The loose input file arguments.
     * @param workingDir The working directory of the invocation.
     */
    private fun appendAnonymousProject(
        projects: MutableList<org.islandoftex.arara.api.files.Project>,
        inputs: List<String>,
        workingDir: MPPPath
    ) {
        // the empty stand-in project the configuration loads for
        // carries no files and keeps the very constants the
        // anonymous project builds from, so the load matters for
        // its side effects alone - the console modes and the file
        // types the configuration puts in effect
        loadProjectConfigurations(
            listOf(Project("Single files", workingDir, emptySet()))
        )
        val anonymous = Project(
            "Single files",
            workingDir,
            inputs.map { fileName ->
                FileSearching.resolveFile(
                    fileName,
                    workingDir,
                    LinearExecutor.executionOptions
                )
            }.toSet()
        )
        anonymousProject = anonymous
        projects += anonymous
    }

    /**
     * Rejects a specification project that carries the anonymous
     * project's name or display label: on the console territory
     * and the summary titles the two would read identically, and
     * the model name also collides in the log's boundary lines.
     *
     * @param projects The projects of the run, the anonymous one
     *   included.
     */
    private fun validateNoNameCollision(
        projects: List<org.islandoftex.arara.api.files.Project>
    ) {
        anonymousProject?.let { anonymous ->
            // the anonymous project displays under the session's
            // localized label, so a specification carrying either
            // the model name or the label reads identically to it
            val reserved = setOf(
                anonymous.name,
                LanguageController.messages.infoLabelSingleFiles
            )
            projects.firstOrNull {
                it !== anonymous && it.name in reserved
            }?.let {
                throw AraraException(
                    LanguageController.messages
                        .errorProjectDuplicateName(it.name)
                )
            }
        }
    }

    /**
     * Rejects a file claimed by two projects of the run, naming
     * the file and the projects with the remedy of removing it
     * from all but one.
     *
     * @param projects The projects of the run.
     */
    private fun validateNoSharedFile(
        projects: List<org.islandoftex.arara.api.files.Project>
    ) {
        // projects claiming the same file - from two
        // specifications, or from a specification and the
        // command line together - is an error naming the file
        // and the projects, with the remedy of removing it from
        // all but one
        val claimed = mutableMapOf<String, String>()
        projects.forEach { project ->
            // the anonymous project carries its display label, so
            // the verdict names what the reader knows - not the
            // model's placeholder
            val displayName = if (project === anonymousProject) {
                LanguageController.messages.infoLabelSingleFiles
            } else {
                project.name
            }
            project.files.forEach { file ->
                val path = file.path.normalize().toString()
                claimed[path]?.let { first ->
                    throw AraraException(
                        LanguageController.messages
                            .errorProjectFileInMultipleProjects(
                                path,
                                "(${first}, ${displayName})"
                            )
                    )
                } ?: run { claimed[path] = displayName }
            }
        }
    }

    /**
     * Resolves the projects the project specifications name. Every
     * specification parses on its own - a failure is an error
     * naming its actual violation, never a guess with a fallback
     * - a project that declared no name takes the base name of
     * its specification file, and the merged projects validate
     * together before their configuration files load, as
     * specifications do not go through the caller's fallback
     * branch.
     *
     * @param workingDir The working directory the specifications
     *   resolve their file references against.
     * @param specifications The specification arguments.
     * @return The projects of all specifications, with their
     *   configuration files loaded.
     */
    private fun resolveProjectSpecifications(
        workingDir: MPPPath,
        specifications: List<String>
    ): List<org.islandoftex.arara.api.files.Project> {
        val projects = specifications.flatMap { argument ->
            val path = workingDir.resolve(argument)
            // a specification is opened directly, never completed
            // from an extension, so the verdict names the file
            // itself instead of enumerating the input extensions
            if (!path.exists || !path.isRegularFile) {
                throw AraraException(
                    LanguageController.messages
                        .errorProjectSpecificationNotFound(argument)
                )
            }
            // a specification file named exactly after the suffix
            // derives an empty base name - the run rejects it with
            // the blank-name verdict, since an empty name can never
            // identify a project
            val derivedName = path.fileName.removeSuffix(".proj.lua")
            if (derivedName.isEmpty()) {
                throw AraraException(
                    LanguageController.messages
                        .errorProjectNameCannotBeBlank
                )
            }
            logger.info {
                "Reading ${path.fileName} as a project specification."
            }
            LuaInterpreter(workingDir).parseProjectsFromLua(
                path.readText(),
                derivedName,
                path.fileName
            )
        }
        validateMergedProjects(projects)
        // the entries the parse left pending resolve against the
        // types the configuration provides
        return loadProjectConfigurations(projects)
    }

    /**
     * Validates the merged specification projects: a dependency
     * names a project of the union - it may live in any of the
     * files - and a name identifies a project uniquely across
     * the files.
     *
     * @param projects The specification projects, merged across
     *   their files.
     */
    private fun validateMergedProjects(
        projects: List<org.islandoftex.arara.api.files.Project>
    ) {
        // the same missing name may repeat across projects; the
        // verdict lists each missing name once
        projects.flatMap { project ->
            project.dependencies.filter { dependency ->
                projects.none { it.name == dependency }
            }
        }.distinct().takeIf { it.isNotEmpty() }?.let { unresolved ->
            throw AraraException(
                LanguageController.messages.errorProjectStrayDependency(
                    unresolved.joinToString(", ", "(", ")")
                )
            )
        }
        // the verdict names one shared name at a time, and the
        // next run reports the next
        projects.map { it.name }
            .groupBy { it }
            .filterValues { it.size > 1 }.keys
            .firstOrNull()?.let { duplicated ->
                throw AraraException(
                    LanguageController.messages.errorProjectDuplicateName(
                        duplicated
                    )
                )
            }
    }

    /**
     * Load the configuration file of each project, if any, and
     * resolve the file types the parse left pending against the
     * types in effect - the project's own configuration is in
     * when its entries resolve. The working directory of a project
     * decides where to look, so multi-directory project
     * specifications work as well.
     *
     * @param projects The projects to load the configuration for.
     * @return The projects with every pending file type resolved.
     */
    private fun loadProjectConfigurations(
        projects: List<org.islandoftex.arara.api.files.Project>
    ): List<org.islandoftex.arara.api.files.Project> =
        projects.map { project ->
            ConfigurationFiles.configFileForProject(project)?.let {
                val resource = ConfigurationFiles.load(it, project)
                // the command line wins over the configuration file,
                // which wins over the plain default; a mode the
                // configuration names but the application does not
                // offer is an error
                applyConsoleMode(
                    ConsoleMode.resolve(console, resource.console)
                )
            }
            // the project's configuration is in effect now, so the
            // entries the parse left pending resolve against its
            // file types, and an extension nothing carries is an
            // error before anything runs
            LuaInterpreter.resolvePendingFileTypes(project)
        }

    /**
     * The console presentation the run applied so far, so a
     * re-application of the same mode keeps the renderers - and
     * with them the run's ledger - untouched.
     */
    private var appliedConsoleMode: ConsoleMode? = null

    /**
     * The anonymous project the invocation builds from its loose
     * input files, if any. The project display names it by its
     * label instead of its model name - the hook knows, by
     * identity, which project the invocation built.
     */
    private var anonymousProject: org.islandoftex.arara.api.files.Project? = null

    /**
     * The full paths of every file the run was asked to process,
     * set when the projects resolve, so the completion block can
     * name the files an exception left unreached - exactly, down
     * to two same-named files in different directories.
     */
    private var allFileNames: List<String> = emptyList()

    /**
     * The full paths of the files that actually started processing,
     * collected by the per-file hook, so the completion block can
     * compute the difference exactly - a bare name cannot tell two
     * same-named files of different directories apart.
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
     * The executor wiring of the command line run: the environment
     * and the directive core come alive before the execution,
     * every file announces itself and re-derives the configuration
     * the command line asks for, the closing blank follows only
     * what the console showed, and the directive list gains the
     * preamble the run asks for.
     */
    private fun executorHooks() = ExecutorHooks(
        executeBeforeExecution = {
            Session.updateEnvironmentVariables()
            // directive processing has to be initialized, so that
            // the core component builds the directives the engine
            // produces
            DirectiveProcessing.initializeDirectiveCore()
        },
        executeBeforeProject = { project ->
            // the configuration file name follows the project, so
            // the per-file log lines name what the file actually
            // used, not what the last-loaded project carried
            Display.configurationFileName =
                ConfigurationFiles.configFileForProject(project)
                    ?.toString() ?: "[none]"
            // the configuration file of each project is loaded in
            // resolveProjects, where it has to be available before
            // the file references are resolved against the file
            // types the configuration may provide; the project
            // line opens the territory when the run is
            // multi-project, and the anonymous project carries its
            // display label instead of its model name
            val isAnonymous = project === anonymousProject
            val displayName = if (isAnonymous) {
                LanguageController.messages.infoLabelSingleFiles
            } else {
                project.name
            }
            val logName = project.name
            Display.printProjectInformation(
                displayName,
                logName,
                project.workingDirectory
            )
        },
        executeBeforeFile = {
            processedFiles.add(it.path.toString())
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
            LinearExecutor.hooks = executorHooks()

            val projects = resolveProjects()
            Display.projectCount = projects.size
            allFileNames = projects.flatMap { it.files }.map { it.path.toString() }
            // the run's exit code reaches the process unchanged - a
            // halt reports its own status, never a flattened failure
            val executionReport = LinearExecutor.execute(projects)
            if (executionReport.exitCode != 0)
                ExecutionStatus.FinishedWithCode(executionReport.exitCode)
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
                // the console keeps the historic bare names: the
                // paths above make the difference exact, and the
                // display speaks the name a user typed
                Display.printUnprocessedFiles(
                    unprocessed.map { MPPPath(it).fileName }
                )
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
