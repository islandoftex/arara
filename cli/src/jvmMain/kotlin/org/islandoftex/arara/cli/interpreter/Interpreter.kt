// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import mu.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.ruleset.RuleFormat
import org.islandoftex.arara.cli.ruleset.RuleUtils
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.InputHandling
import org.islandoftex.arara.mvel.interpreter.AraraExceptionWithHeader
import org.islandoftex.arara.mvel.rules.DirectiveConditionalEvaluator
import org.islandoftex.arara.mvel.rules.SerialRuleCommand
import org.islandoftex.arara.mvel.utils.MvelState
import org.mvel2.templates.TemplateRuntime
import kotlin.time.ExperimentalTime

/**
 * Interprets the list of directives.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class Interpreter @OptIn(ExperimentalTime::class) constructor(
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
     * Gets the rule according to the provided directive.
     *
     * @param directive The provided directive.
     * @return The absolute canonical path of the rule, given the provided
     * directive.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @OptIn(ExperimentalTime::class)
    @Throws(AraraException::class)
    private fun getRule(directive: Directive, workingDirectory: MPPPath): MPPPath =
        executionOptions.rulePaths.let { paths ->
            paths.flatMap { path ->
                listOf(
                    InterpreterUtils.construct(
                        path, directive.identifier,
                        RuleFormat.MVEL, workingDirectory
                    ),
                    InterpreterUtils.construct(
                        path, directive.identifier,
                        RuleFormat.KOTLIN_DSL, workingDirectory
                    ),
                    // this lookup adds support for the rules distributed with
                    // arara (in TL names should be unique, hence we avoided
                    // going for pdflatex.yaml in favor of arara-rule-pdflatex.yaml
                    // from version 6 on)
                    InterpreterUtils.construct(
                        path, "arara-rule-" + directive.identifier,
                        RuleFormat.MVEL, workingDirectory
                    )
                )
            }.firstOrNull { it.exists } ?: throw AraraException(
                LanguageController.messages.ERROR_INTERPRETER_RULE_NOT_FOUND.format(
                    directive.identifier,
                    directive.identifier,
                    paths.joinToString("; ", "(", ")") {
                        it.normalize().toString()
                    }
                )
            )
        }

    // TODO: in the following, extract the printing into the higher level
    // function
    /**
     * "Run" a boolean return value
     * @param value The boolean.
     * @param conditional The conditional to print in dry-run mode.
     * @param authors The authors of the rule.
     * @return Returns [value]
     */
    @OptIn(ExperimentalTime::class)
    private fun runBoolean(
        value: Boolean,
        conditional: DirectiveConditional,
        authors: List<String>
    ): Boolean = value.also {
        logger.info {
            LanguageController.messages.LOG_INFO_BOOLEAN_MODE.format(it)
        }

        if (executionOptions.executionMode == ExecutionMode.DRY_RUN) {
            DisplayUtils.printAuthors(authors)
            DisplayUtils.printWrapped(
                LanguageController.messages
                    .INFO_INTERPRETER_DRYRUN_MODE_BOOLEAN_MODE.format(it)
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
     * @param ruleCommandExitValue The exit value of the rule command.
     * @return Success of the execution.
     * @throws AraraException Execution failed.
     */
    @OptIn(ExperimentalTime::class)
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught")
    private fun runCommand(
        command: Command,
        conditional: DirectiveConditional,
        authors: List<String>,
        ruleCommandExitValue: String?
    ): Boolean {
        logger.info {
            LanguageController.messages.LOG_INFO_SYSTEM_COMMAND.format(command)
        }
        var success = true

        if (executionOptions.executionMode != ExecutionMode.DRY_RUN) {
            val code = InterpreterUtils.run(command)
            val check: Any = try {
                val context = mapOf<String, Any>("value" to code)
                TemplateRuntime.eval(
                    "@{ " + (ruleCommandExitValue ?: "value == 0") + " }",
                    context
                )
            } catch (exception: RuntimeException) {
                throw AraraExceptionWithHeader(
                    LanguageController.messages
                        .ERROR_INTERPRETER_EXIT_RUNTIME_ERROR,
                    exception
                )
            }

            success = if (check is Boolean) {
                check
            } else {
                throw AraraExceptionWithHeader(
                    LanguageController.messages
                        .ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN
                )
            }
        } else {
            DisplayUtils.printAuthors(authors)
            DisplayUtils.printWrapped(
                LanguageController.messages
                    .INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND.format(command)
            )
            DisplayUtils.printConditional(conditional)
        }

        return success
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
     * Execute a command.
     * @param command The command to evaluate.
     * @param conditional Under which condition to execute.
     * @param rule The rule (only passed for output purposes).
     * @param parameters The parameters for evaluation
     * @throws AraraException Running the command failed.
     */
    @OptIn(ExperimentalTime::class)
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught", "ThrowsCount")
    private fun executeCommand(
        command: SerialRuleCommand,
        conditional: DirectiveConditional,
        rule: Rule,
        parameters: Map<String, Any>
    ) = try {
        resultToList(TemplateRuntime.eval(command.commandString!!, parameters))
    } catch (exception: RuntimeException) {
        throw AraraExceptionWithHeader(
            LanguageController
                .messages.ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR,
            exception
        )
    }.filter { it.toString().isNotBlank() }
        .fold(ExecutionStatus.Processing() as ExecutionStatus) { _, current ->
            DisplayUtils.printEntry(
                rule.displayName!!,
                command.name
                    ?: LanguageController.messages.INFO_LABEL_UNNAMED_TASK
            )

            val success = when (current) {
                is Boolean -> runBoolean(
                    current, conditional,
                    rule.authors
                )
                is Command -> runCommand(
                    current, conditional,
                    rule.authors, command.exit
                )
                else ->
                    throw AraraExceptionWithHeader(
                        LanguageController
                            .messages.ERROR_INTERPRETER_WRONG_RETURN_TYPE
                    )
            }

            DisplayUtils.printEntryResult(success)
            when {
                Session.contains(haltKey) ->
                    throw HaltExpectedException(
                        LanguageController.messages
                            .ERROR_INTERPRETER_USER_REQUESTED_HALT,
                        ExecutionStatus.FinishedWithCode(
                            Session[haltKey].toString().toInt()
                        )
                    )
                executionOptions.haltOnErrors && !success ->
                    throw HaltExpectedException(
                        LanguageController
                            .messages.ERROR_INTERPRETER_COMMAND_UNSUCCESSFUL_EXIT
                            .format(command.name),
                        ExecutionStatus.ExternalCallFailed()
                    )
                success -> ExecutionStatus.Processing()
                else -> ExecutionStatus.ExternalCallFailed()
            }
        }

    /**
     * Executes each directive, throwing an exception if something bad has
     * happened.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @OptIn(ExperimentalTime::class)
    @Throws(AraraException::class)
    @Suppress("NestedBlockDepth")
    fun execute(directive: Directive): ExecutionStatus {
        logger.info {
            LanguageController.messages.LOG_INFO_INTERPRET_RULE.format(
                directive.identifier
            )
        }

        val file = getRule(directive, workingDirectory)
        logger.info {
            LanguageController.messages.LOG_INFO_RULE_LOCATION.format(
                file.parent
            )
        }

        // parse the rule identified by the directive (may throw an exception)
        val rule = RuleUtils.parseRule(file, directive.identifier)
        val parameters = parseArguments(rule, directive).plus(MvelState.ruleMethods)
        val evaluator = DirectiveConditionalEvaluator(executionOptions)
        val available =
            if (InterpreterUtils.runPriorEvaluation(directive.conditional))
                evaluator.evaluate(directive.conditional)
            else true

        return when {
            !available ->
                // if this directive is conditionally disabled, skip by returning
                // a zero exit code meaning a conditionally disabled directive
                // never fails
                ExecutionStatus.Processing()
            Session.contains(haltKey) ->
                // a halt was requested by a rule
                ExecutionStatus.FinishedWithCode(Session[haltKey].toString().toInt())
            else -> {
                try {
                    // if not execute the commands associated with the directive
                    var retValue: ExecutionStatus
                    do {
                        retValue = rule.commands.fold(
                            ExecutionStatus.Processing()
                                as ExecutionStatus
                        ) { _, command ->
                            executeCommand(
                                // TODO: remove cast
                                command as SerialRuleCommand,
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
                        LanguageController.messages.ERROR_RULE_IDENTIFIER_AND_PATH
                            .format(directive.identifier, file.parent.toString()) + " " +
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
                    .ERROR_INTERPRETER_UNKNOWN_KEYS.format(
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
                LanguageController.messages.ERROR_INTERPRETER_ARGUMENT_IS_REQUIRED
                    .format(argument.identifier)
            )

        return if (idInDirectiveParams)
            argument.processor(parameterValue, context)
        else
            argument.defaultValue?.let { default ->
                argument.processor(default, context)
            } ?: emptyList<String>()
    }
}
