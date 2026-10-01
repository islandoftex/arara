// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.rules.RuleCommand
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.ruleset.RuleEngine
import org.islandoftex.arara.cli.ruleset.RuleFiles
import org.islandoftex.arara.cli.utils.Display
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveConditionalEvaluator as ConditionalEvaluator
import org.islandoftex.arara.core.rules.InputHandling
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.lua.rules.LuaConditionalEvaluator
import org.islandoftex.arara.lua.rules.LuaRuleCommand
import org.islandoftex.arara.mvel.rules.DirectiveConditionalEvaluator
import org.islandoftex.arara.mvel.rules.MvelState
import org.islandoftex.arara.mvel.rules.SerialRuleCommand
import org.mvel2.templates.TemplateRuntime

/**
 * Checks if the current conditional has a prior evaluation.
 *
 * @param conditional The current conditional object.
 * @return A boolean value indicating if the current conditional has a prior
 * evaluation.
 */
internal fun runPriorEvaluation(conditional: DirectiveConditional): Boolean {
    return if (LinearExecutor.executionOptions.executionMode == ExecutionMode.DRY_RUN) {
        false
    } else {
        when (conditional.type) {
            DirectiveConditionalType.IF,
            DirectiveConditionalType.WHILE,
            DirectiveConditionalType.UNLESS -> true
            else -> false
        }
    }
}

// the interpreter gained the conditional dispatch policy alongside its
// evaluation flow; the function count mirrors the directive surface it
// owns rather than accumulated responsibilities

/**
 * Interprets the list of directives.
 *
 * @author Island of TeX
 */
@Suppress("TooManyFunctions")
class Interpreter(
    private val executionOptions: ExecutionOptions,
    currentFile: ProjectFile,
    private val workingDirectory: MPPPath
) {
    private val logger = KotlinLogging.logger { }

    /**
     * This string represents the key necessary for halting the current
     * executor. It will be checked against session values.
     */
    private val haltKey = "arara:${currentFile.path.fileName}:halt"

    /**
     * Read the halt status recorded in the session under the halt key,
     * reporting a localized error when a rule stored a non numeric
     * value there.
     */
    private fun haltStatus(): Int =
        Session[haltKey].toString().toIntOrNull()
            ?: throw AraraExceptionWithHeader(
                LanguageController.messages
                    .errorInterpreterHaltStatusNotANumber(haltKey)
            )

    /**
     * Gets the rule according to the provided directive.
     *
     * @param directive The provided directive.
     * @param workingDirectory The working directory of the file being
     *   processed, used to resolve relative rule paths.
     * @return The absolute canonical path of the rule, given the provided
     * directive.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun getRule(directive: Directive, workingDirectory: MPPPath): MPPPath =
        executionOptions.rulePaths.let { paths ->
            paths.flatMap { path ->
                // the formats considered here depend on the rule engine the
                // user chose on the command line (see the --engine option)
                RuleEngine.current.formats.flatMap { format ->
                    listOf(
                        RuleFiles.construct(
                            path, directive.identifier,
                            format, workingDirectory
                        ),
                        // this lookup adds support for the rules
                        // distributed with arara: their file names carry
                        // the arara-rule- prefix so the names stay
                        // unique in TeX Live
                        RuleFiles.construct(
                            path, "arara-rule-" + directive.identifier,
                            format, workingDirectory
                        )
                    )
                }
            }.firstOrNull { it.exists } ?: throw AraraException(
                LanguageController.messages.errorInterpreterRuleNotFound(
                    directive.identifier,
                    // the message names the file actually sought, so the
                    // extension follows the engine's format rather than
                    // any single format hardwired into the texts
                    directive.identifier + "." +
                        RuleEngine.current.formats.first().extension,
                    paths.joinToString("; ", "(", ")") {
                        it.normalize().toString()
                    }
                )
            )
        }

    // note: the printing in the following could be extracted into the
    // higher level function
    /**
     * "Run" a boolean return value
     * @param value The boolean.
     * @param conditional The conditional to print in dry-run mode.
     * @param authors The authors of the rule.
     * @return Returns [value]
     */
    private fun runBoolean(
        value: Boolean,
        conditional: DirectiveConditional,
        authors: List<String>
    ): Boolean = value.also {
        logger.info {
            "Boolean value: $value"
        }

        if (executionOptions.executionMode == ExecutionMode.DRY_RUN) {
            Display.printAuthors(authors)
            Display.printWrapped(
                LanguageController.messages
                    .infoInterpreterDryRunModeBooleanMode(it)
            )
            Display.printConditional(conditional)
        }
    }

    /**
     * Run a command
     *
     * @param command The command to run.
     * @param conditional The conditional applied to the run (only for printing).
     * @param authors The rule authors (only for printing).
     * @param exitCheck Determines whether an exit code indicates success.
     * @return The success of the execution and the output the command
     * captured, so a failing task can quote its own words.
     * @throws AraraException Execution failed.
     */
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught")
    private fun runCommand(
        command: Command,
        conditional: DirectiveConditional,
        authors: List<String>,
        exitCheck: (Int) -> Boolean
    ): Pair<Boolean, String> {
        logger.info {
            "System command: $command"
        }
        // the verbose presentations show the command they are about
        // to run; the plain console stays as it always was
        Display.printTaskDetails(command, conditional, authors)
        var success = true

        if (executionOptions.executionMode != ExecutionMode.DRY_RUN) {
            val (code, output) = CommandRunner.run(command)
            success = exitCheck(code)
            return success to output
        } else {
            Display.printAuthors(authors)
            Display.printDryRunCommand(command)
            Display.printConditional(conditional)
        }

        return success to ""
    }

    /**
     * Converts the command evaluation result to a flat list.
     * @param result The result
     * @return A flat list.
     */
    private fun resultToList(result: Any) = if (result is List<*>) {
        InputHandling.flatten(result)
    } else {
        listOf(result)
    }

    /**
     * Evaluate an MVEL command against the resolved parameters.
     *
     * @param command The command to evaluate.
     * @param parameters The parameters for evaluation.
     * @return The list of objects the command evaluated to.
     * @throws AraraException Evaluating the command failed.
     */
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught")
    private fun evaluateMvelCommand(
        command: SerialRuleCommand,
        parameters: Map<String, Any>
    ): List<Any> = try {
        resultToList(
            TemplateRuntime.eval(
                command.commandString!!,
                parameters.plus(MvelState.ruleMethods)
            )
        )
    } catch (exception: RuntimeException) {
        throw AraraExceptionWithHeader(
            LanguageController
                .messages.errorInterpreterCommandRuntimeError,
            exception
        )
    }

    /**
     * Build the exit code check of an MVEL command (evaluating the `exit`
     * expression of the rule, defaulting to a zero exit code check).
     */
    @Suppress("TooGenericExceptionCaught")
    private fun mvelExitCheck(command: SerialRuleCommand): (Int) -> Boolean =
        { code ->
            val check: Any = try {
                val context = mapOf<String, Any>("value" to code)
                TemplateRuntime.eval(
                    "@{ " + (command.exit ?: "value == 0") + " }",
                    context
                )
            } catch (exception: RuntimeException) {
                throw AraraExceptionWithHeader(
                    LanguageController.messages
                        .errorInterpreterExitRuntimeError,
                    exception
                )
            }
            check as? Boolean
                ?: throw AraraExceptionWithHeader(
                    LanguageController.messages
                        .errorInterpreterWrongExitClosureReturn
                )
        }

    /**
     * Runs one evaluated task of a command: the boolean or the
     * command the rule produced, with the captured output traveling
     * beside the outcome so a failing task can quote its own words.
     *
     * @param current The evaluated task.
     * @param conditional The conditional of the directive.
     * @param rule The rule the command belongs to.
     * @param exitCheck Determines whether an exit code indicates
     * success.
     * @return The success of the task and the output it captured.
     */
    private fun runTask(
        current: Any,
        conditional: DirectiveConditional,
        rule: Rule,
        exitCheck: (Int) -> Boolean
    ): Pair<Boolean, String> {
        return when (current) {
        is Boolean -> runBoolean(
            current, conditional,
            rule.authors
        ) to ""
        is Command -> runCommand(
            current, conditional,
            rule.authors, exitCheck
        )
        else ->
            throw AraraExceptionWithHeader(
                LanguageController
                    .messages.errorInterpreterWrongReturnType
            )
        }
    }

    /**
     * Enforces the halt policy after a task: the halt directive
     * stops the run with its own exit code, and the error-halt
     * option stops it where a failed task stands.
     *
     * @param success The outcome of the task.
     * @param command The command the task ran under.
     */
    private fun enforceHaltPolicy(success: Boolean, command: RuleCommand) {
        when {
            Session.contains(haltKey) ->
                throw HaltExpectedException(
                    LanguageController.messages
                        .errorInterpreterUserRequestedHalt,
                    ExecutionStatus.FinishedWithCode(haltStatus())
                )
            executionOptions.haltOnErrors && !success ->
                throw HaltExpectedException(
                    LanguageController.messages
                        .errorInterpreterCommandUnsuccessfulExit(
                            command.name
                                ?: LanguageController.messages
                                    .infoLabelUnnamedTask
                        ),
                    ExecutionStatus.ExternalCallFailed()
                )
        }
    }

    private fun executeCommand(
        command: RuleCommand,
        conditional: DirectiveConditional,
        rule: Rule,
        parameters: Map<String, Any>
    ): ExecutionStatus {
        val (results, exitCheck) = when (command) {
            is SerialRuleCommand ->
                evaluateMvelCommand(command, parameters) to
                    mvelExitCheck(command)
            is LuaRuleCommand ->
                command.evaluate(parameters) to command.exitCheck
            else ->
                throw AraraExceptionWithHeader(
                    LanguageController
                        .messages.errorInterpreterWrongReturnType
                )
        }
        return results.filter { it.toString().isNotBlank() }
            .fold(ExecutionStatus.Processing() as ExecutionStatus) { _, current ->
                Display.printEntry(
                    rule.displayName!!,
                    command.name
                        ?: LanguageController.messages.infoLabelUnnamedTask
                )

                val (success, output) = runTask(
                    current, conditional, rule, exitCheck
                )
            Display.printEntryResult(success)
            // the failed task quotes the tail of its own output;
            // the plain console stays as it always was
            if (!success) {
                Display.printCommandFailure(output)
            }
            enforceHaltPolicy(success, command)
            if (success) {
                ExecutionStatus.Processing()
            } else {
                ExecutionStatus.ExternalCallFailed()
            }
        }
    }

    /**
     * Executes each directive, throwing an exception if something bad has
     * happened.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun execute(directive: Directive): ExecutionStatus {
        logger.info {
            "I am ready to interpret rule \"${directive.identifier}\"."
        }

        val file = getRule(directive, workingDirectory)
        logger.info {
            "Rule location: \"${file.parent}\""
        }
        // the resolved file itself matters more than its directory: a
        // rule shadowed by another of the same name is the classic
        // wrong-rule-run confusion, and only the file settles it
        logger.info {
            "Rule file: \"$file\""
        }

        // parse the rule identified by the directive (may throw an exception)
        val rule = RuleFiles.parseRule(file, directive.identifier)
        // the resolved arguments; engine-specific extras (e.g. the MVEL rule
        // methods) are added by the respective command evaluators
        val parameters = parseArguments(rule, directive)
        // the conditional dialect follows the rule engine: the Lua engine
        // evaluates directive conditionals as Lua expressions, the (default
        // and deprecated) YAML engine keeps the MVEL dialect
        val evaluator = when (RuleEngine.current) {
            RuleEngine.LUA -> LuaConditionalEvaluator(executionOptions)
            RuleEngine.YAML -> DirectiveConditionalEvaluator(executionOptions)
        }
        val available =
            if (runPriorEvaluation(directive.conditional))
                evaluator.evaluate(directive.conditional)
            else true

        return when {
            !available -> {
                // a conditionally disabled directive is skipped with a zero
                // exit code so it never fails; the log records the skip
                // because it otherwise leaves no trace at all
                logger.info {
                    "Directive ${directive.identifier} skipped as its " +
                        "conditional evaluated to false"
                }
                // the verbose presentations name the skip on the
                // console; the plain console stays silent, as it
                // always was
                Display.printSkippedDirective(directive.identifier)
                ExecutionStatus.Processing()
            }
            Session.contains(haltKey) ->
                // a halt was requested by a rule
                ExecutionStatus.FinishedWithCode(haltStatus())
            else -> runCommands(directive, file, rule, parameters, evaluator)
        }
    }

    /**
     * Run the command list of an available directive, honouring loop
     * conditionals and translating raised errors to their headed forms.
     *
     * @param directive The directive being executed.
     * @param file The rule file the directive resolved to.
     * @param rule The parsed rule.
     * @param parameters The resolved rule arguments.
     * @param evaluator The conditional evaluator of the active engine.
     * @return The final execution status of the command list.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun runCommands(
        directive: Directive,
        file: MPPPath,
        rule: Rule,
        parameters: Map<String, Any>,
        evaluator: ConditionalEvaluator
    ): ExecutionStatus = try {
        var retValue: ExecutionStatus
        do {
            retValue = rule.commands.fold(
                ExecutionStatus.Processing()
                    as ExecutionStatus
            ) { _, command ->
                executeCommand(
                    command,
                    directive.conditional,
                    rule,
                    parameters
                )
            }
        } while (evaluator.evaluate(directive.conditional))
        retValue
    } catch (e: HaltExpectedException) {
        // If the user uses the halt rule to trigger a halt, this will be
        // raised. Any other exception will not be caught and propagate up.
        e.status
    } catch (e: AraraExceptionWithHeader) {
        // rethrow arara exceptions that are bound to have a header by
        // prepending the header and removing the outer exception with
        // header where possible
        throw AraraException(
            LanguageController.messages.errorRuleIdentifierAndPath(
                directive.identifier,
                file.parent.toString()
            ) + " " +
                e.message,
            e.cause ?: e
        )
    }

    /**
     * Gets a set of strings containing unknown keys from a map and a list. It
     * is a set difference from the keys in the map and the entries in the list.
     *
     * @param parameters The map of parameters.
     * @param arguments The list of arguments.
     * @return A set of strings representing unknown keys from a map and a list.
     */
    private fun getUnknownKeys(
        parameters: Map<String, Any>,
        arguments: List<RuleArgument<*>>
    ): Set<String> {
        val found = parameters.keys
        val expected = arguments.map { it.identifier }
        return found.subtract(expected)
    }

    /**
     * Parses the rule arguments against the provided directive.
     *
     * @param rule The rule object.
     * @param directive The directive.
     * @return A map containing all arguments resolved according to the
     * directive parameters.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(AraraException::class)
    private fun parseArguments(rule: Rule, directive: Directive):
        Map<String, Any> {
        val unknown = getUnknownKeys(directive.parameters, rule.arguments)
            .minus("reference")
        if (unknown.isNotEmpty())
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .errorInterpreterUnknownKeys(
                        unknown.joinToString(", ", "(", ")")
                    )
            )

        val resolvedArguments = mutableMapOf<String, Any>()
        resolvedArguments["reference"] = directive.parameters.getValue("reference")

        val context = mapOf(
            "parameters" to directive.parameters,
            "reference" to directive.parameters.getValue("reference")
        ).plus(MvelState.ruleMethods)

        rule.arguments.forEach { argument ->
            resolvedArguments[argument.identifier] = processArgument(
                argument as RuleArgument<Any?>,
                directive.parameters[argument.identifier],
                context
            )
        }

        return resolvedArguments
    }

    /**
     * Process a single argument and return the evaluated result.
     *
     * A parameter that is present in the directive but set to null counts
     * as if it had not been provided at all.
     *
     * @param argument The argument to process.
     * @param parameterValue The value as provided by the user, or null
     *   when the parameter is absent.
     * @param context The context for the evaluation.
     * @return The result of the evaluation.
     * @throws AraraException The argument could not be processed.
     */
    @Throws(AraraException::class)
    private fun processArgument(
        argument: RuleArgument<Any?>,
        parameterValue: Any?,
        context: Map<String, Any>
    ): List<*> {
        // a directive parameter that is present but set to null counts
        // as if it had not been provided at all
        val idInDirectiveParams = parameterValue != null

        if (argument.isRequired && !idInDirectiveParams)
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .errorInterpreterArgumentIsRequired(argument.identifier)
            )

        return if (idInDirectiveParams)
            argument.processor(parameterValue, context)
        else
            argument.defaultValue?.let { default ->
                argument.processor(default, context)
            } ?: emptyList<String>()
    }
}
