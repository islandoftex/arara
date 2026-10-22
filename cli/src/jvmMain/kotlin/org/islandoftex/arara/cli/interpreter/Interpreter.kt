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
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.RunLog
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.CommandEvaluator
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.ui.InputHandling
import org.islandoftex.arara.lua.rules.LuaConditionalEvaluator
import org.islandoftex.arara.mvel.interpreter.MvelCommandEvaluator
import org.islandoftex.arara.mvel.rules.DirectiveConditionalEvaluator
import org.islandoftex.arara.mvel.rules.SerialRuleCommand

/**
 * Interprets the list of directives.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class Interpreter(
    private val executionOptions: ExecutionOptions,
    @Suppress("UNUSED_PARAMETER") currentFile: ProjectFile,
    private val workingDirectory: MPPPath,
    private val engine: RuleEngine = RuleEngine.YAML
) {
    private val logger = KotlinLogging.logger { }

    // Read only after failure: booleans set 1; commands set their nonzero exit status (or 1).
    private var lastFailureCode = 1
    private var failureOutput = ""

    // TODO: in the following, extract the printing into the higher level
    // function
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
        if (!value) lastFailureCode = 1
        logger.info {
            "Boolean value: $it"
        }

        if (executionOptions.executionMode == ExecutionMode.DRY_RUN) {
            DisplayUtils.printAuthors(authors)
            DisplayUtils.printWrapped(
                LanguageController.messages
                    .infoInterpreterDryRunModeBooleanMode(it)
            )
            DisplayUtils.printConditional(conditional)
        }
    }

    /**
     * Run a command
     *
     * @param command The command to run.
     * @param conditional The conditional applied to the run (only for printing).
     * @param authors The rule authors (only for printing).
     * @param exitCheck Determines whether the process exit code indicates success.
     * @return Success of the execution.
     * @throws AraraException Execution failed.
     */
    @Throws(AraraException::class)
    private fun runCommand(
        command: Command,
        conditional: DirectiveConditional,
        authors: List<String>,
        exitCheck: (Int) -> Boolean
    ): Boolean {
        logger.info {
            "System command: $command"
        }
        var success = true
        var commandExitCode = 1

        if (executionOptions.executionMode != ExecutionMode.DRY_RUN) {
            DisplayUtils.renderer.printTaskDetails(command.toString(), conditional, authors)
            val result = CommandRunner.runResult(command)
            val code = result.exitCode
            commandExitCode = if (code == 0) 1 else code
            success = exitCheck(code)
            failureOutput = if (success) "" else result.output
        } else {
            DisplayUtils.printAuthors(authors)
            DisplayUtils.printWrapped(
                LanguageController.messages
                    .infoInterpreterDryRunModeSystemCommand(command.toString())
            )
            DisplayUtils.printConditional(conditional)
        }

        if (!success) lastFailureCode = commandExitCode
        return success
    }

    /**
     * Converts the command evaluation result to a flat list.
     * @param result The result
     * @return A flat list.
     */
    private fun resultToList(result: Any?) = if (result == null && HaltRequest.exitCode != null) {
        emptyList()
    } else if (result is List<*>) {
        InputHandling.flatten(result)
    } else {
        listOf(result)
    }

    /**
     * Execute a command.
     * @param command The command to evaluate.
     * @param conditional Under which condition to execute.
     * @param rule The rule (only passed for output purposes).
     * @param parameters The parameters for evaluation
     * @throws AraraException Running the command failed.
     */
    @Throws(AraraException::class)
    @Suppress("ThrowsCount")
    private fun executeCommand(
        command: RuleCommand,
        conditional: DirectiveConditional,
        rule: Rule,
        parameters: Map<String, Any>
    ): ExecutionStatus {
        val evaluator = commandEvaluator(command)
        return resultToList(evaluator.evaluate(parameters)).filter { it.toString().isNotBlank() }
            .also {
                HaltRequest.exitCode?.let { code ->
                    throw HaltExpectedException(
                        LanguageController.messages.errorInterpreterUserRequestedHalt,
                        ExecutionStatus.FinishedWithCode(code)
                    )
                }
            }
            .fold(ExecutionStatus.Processing() as ExecutionStatus) { previous, current ->
                DisplayUtils.printEntry(
                    rule.displayName!!,
                    command.name
                        ?: LanguageController.messages.infoLabelUnnamedTask
                )

                failureOutput = ""
                val success = when (current) {
                    is Boolean -> runBoolean(
                        current, conditional,
                        rule.authors
                    )
                    is Command -> runCommand(
                        current, conditional,
                        rule.authors, evaluator::isSuccessful
                    )
                    else ->
                        throw AraraExceptionWithHeader(
                            LanguageController
                                .messages.errorInterpreterWrongReturnType
                        )
                }

                DisplayUtils.printEntryResult(success)
                if (!success) DisplayUtils.renderer.printCommandFailure(failureOutput)
                when {
                    executionOptions.haltOnErrors && !success ->
                        throw HaltExpectedException(
                            LanguageController
                                .messages.errorInterpreterCommandUnsuccessfulExit(command.name.toString()),
                            ExecutionStatus.FinishedWithCode(lastFailureCode)
                        )
                    success -> ExecutionStatus.Processing()
                    else -> ExecutionStatus.FinishedWithCode(lastFailureCode)
                }.let { if (it.exitCode != 0) it else previous }
            }
    }

    private fun commandEvaluator(command: RuleCommand): CommandEvaluator = when (command) {
        is CommandEvaluator -> command
        is SerialRuleCommand -> MvelCommandEvaluator(command)
        else -> throw AraraExceptionWithHeader(
            LanguageController.messages.errorInterpreterWrongReturnType
        )
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
            "Interpreting rule \"${directive.identifier}\""
        }

        val file = RuleFiles.find(directive, executionOptions, workingDirectory, engine)
        RunLog.rule(directive.identifier, file.toString(), engine.name.lowercase())

        // parse the rule identified by the directive (may throw an exception)
        val rule = RuleFiles.parseRule(file, directive.identifier)
        return executeRule(rule, directive, file)
    }

    @Suppress("NestedBlockDepth")
    internal fun executeRule(rule: Rule, directive: Directive, file: MPPPath): ExecutionStatus {
        val parameters = parseArguments(rule, directive)
        val evaluator = if (engine == RuleEngine.LUA) LuaConditionalEvaluator(executionOptions)
            else DirectiveConditionalEvaluator(executionOptions)
        val available =
            if (runPriorEvaluation(directive.conditional))
                evaluator.evaluate(directive.conditional)
            else true

        return when {
            !available -> {
                DisplayUtils.renderer.printSkippedDirective(directive.identifier)
                ExecutionStatus.Processing()
            }
            HaltRequest.exitCode != null ->
                // a halt was requested by a rule
                ExecutionStatus.FinishedWithCode(HaltRequest.exitCode!!)
            else -> {
                var status: ExecutionStatus = ExecutionStatus.Processing()
                try {
                    do {
                        rule.commands.forEach { command ->
                            executeCommand(
                                command,
                                directive.conditional,
                                rule,
                                parameters
                            ).let { if (it.exitCode != 0) status = it }
                        }
                    } while (evaluator.evaluate(directive.conditional))
                    status
                } catch (e: HaltExpectedException) {
                    // If the user uses the halt rule to trigger a halt, this will be
                    // raised. Any other exception will not be caught and propagate up.
                    if (e.status.exitCode != 0) e.status else status
                } catch (e: AraraExceptionWithHeader) {
                    // rethrow arara exceptions that are bound to have a header by
                    // prepending the header and removing the outer exception with
                    // header where possible
                    throw AraraException(
                        LanguageController.messages.errorRuleIdentifierAndPath(
                            directive.identifier, file.parent.toString()
                        ) + " " +
                            e.message,
                        e.exception ?: e
                    )
                }
            }
        }
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
        arguments: List<org.islandoftex.arara.api.rules.RuleArgument<*>>
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
        )

        rule.arguments.forEach { argument ->
            resolvedArguments[argument.identifier] = processArgument(
                argument as RuleArgument<Any?>,
                directive.parameters.containsKey(argument.identifier),
                context,
                directive.parameters[argument.identifier]
            )
        }

        return resolvedArguments
    }

    /**
     * Process a single argument and return the evaluated result.
     *
     * @param argument The argument to process.
     * @param idInDirectiveParams Whether the argument's identifier is
     *   contained in the directive's parameters field.
     * @param context The context for the evaluation.
     * @param parameterValue The value as passed by the user.
     * @return The result of the evaluation.
     * @throws AraraException The argument could not be processed.
     */
    @Throws(AraraException::class)
    private fun processArgument(
        argument: RuleArgument<Any?>,
        idInDirectiveParams: Boolean,
        context: Map<String, Any>,
        parameterValue: Any?
    ): List<*> {
        if (argument.isRequired && !idInDirectiveParams)
            throw AraraExceptionWithHeader(
                LanguageController.messages.errorInterpreterArgumentIsRequired(argument.identifier)
            )

        return if (idInDirectiveParams)
            argument.processor(parameterValue, context)
        else
            argument.defaultValue?.let { default ->
                argument.processor(default, context)
            } ?: emptyList<String>()
    }

    /**
     * Checks if the current conditional has a prior evaluation.
     *
     * @param conditional The current conditional object.
     * @return A boolean value indicating if the current conditional has a prior
     * evaluation.
     */
    private fun runPriorEvaluation(conditional: DirectiveConditional): Boolean {
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
}
