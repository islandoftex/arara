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
import org.islandoftex.arara.api.rules.RuleCommand
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.ruleset.RuleUtils
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.InputHandling
import org.islandoftex.arara.mvel.interpreter.AraraExceptionWithHeader
import org.islandoftex.arara.mvel.rules.DirectiveConditionalEvaluator
import org.islandoftex.arara.mvel.rules.SerialRuleCommand
import org.islandoftex.arara.mvel.utils.MvelState
import org.mvel2.templates.TemplateRuntime

/**
 * Interprets the list of directives.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
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
     * Gets the rule according to the provided directive.
     *
     * @param directive The provided directive.
     * @param workingDirectory The working directory to search.
     * @return The absolute canonical path of the rule, given the provided
     *   directive, or null if the rule could not be found by looking up
     *   the respective path.
     */
    @Throws(AraraException::class)
    private fun getRuleFromSingleRuleFile(directive: Directive, workingDirectory: MPPPath): MPPPath? =
            executionOptions.rulePaths.let { paths ->
                paths.flatMap { path ->
                    listOf(
                            InterpreterUtils.resolveAgainstDirectory(path, workingDirectory)
                                    .resolve("${directive.identifier}.yaml"),
                            // this lookup adds support for the rules distributed with
                            // arara (in TL names should be unique, hence we avoided
                            // going for pdflatex.yaml in favor of arara-rule-pdflatex.yaml
                            // from version 6 on)
                            InterpreterUtils.resolveAgainstDirectory(path, workingDirectory)
                                    .resolve("arara-rule-${directive.identifier}.yaml")
                    )
                }.firstOrNull { it.exists }
            }

    /**
     * Gets the rule according to the provided directive.
     *
     * @param directive The provided directive.
     * @param workingDirectory The working directory to search.
     * @return The rule for the directive.
     * @throws AraraException No rule has been found, to be caught in the
     *   higher levels.
     */
    private fun getRuleFor(directive: Directive, workingDirectory: MPPPath): Rule =
            getRuleFromSingleRuleFile(directive, workingDirectory)
                    ?.let {
                        // check for the YAML rule found
                        logger.info(
                                LanguageController.messages.LOG_INFO_RULE_LOCATION
                                        .format(it.parent)
                        )
                        RuleUtils.parseYAMLRule(it, directive.identifier)
                    }
            // otherwise check potential Kotlin DSL rules
            // TODO: try cache and only act on miss
                    ?: executionOptions.rulePaths.forEach {
                        val dir = InterpreterUtils.resolveAgainstDirectory(it, workingDirectory)
                        FileSearching.listFilesByExtensions(dir, listOf("kts"), false)
                                .forEach { rule ->
                                    RuleUtils.parseKotlinDSLRuleFile(rule)
                                }
                    }.let { RuleUtils.getRule(directive.identifier) }
                    ?: throw AraraException(
                            LanguageController.messages.ERROR_INTERPRETER_RULE_NOT_FOUND.format(
                                    directive.identifier,
                                    directive.identifier,
                                    executionOptions.rulePaths.joinToString("; ", "(", ")") {
                                        it.normalize().toString()
                                    }
                            )
                    )

    /**
     * "Run" a boolean return value
     * @param value The boolean.
     * @return Returns [value]
     */
    private fun runBoolean(value: Boolean): Boolean = value
            .also {
                logger.info(LanguageController.messages.LOG_INFO_BOOLEAN_MODE
                        .format(it))
            }

    /**
     * Run a command
     *
     * @param command The command to run.
     * @param ruleCommandExitValue The exit value of the rule command.
     * @return Success of the execution.
     * @throws AraraException Execution failed.
     */
    @Throws(AraraException::class)
    private fun runCommand(
        command: Command,
        ruleCommandExitValue: String?
    ): Boolean {
        logger.info(LanguageController.messages.LOG_INFO_SYSTEM_COMMAND.format(command))
        val code = InterpreterUtils.run(command)
        val context = mapOf<String, Any>("value" to code)
        return kotlin.runCatching {
            TemplateRuntime.eval(
                    "@{ " + (ruleCommandExitValue ?: "value == 0") + " }",
                    context)
        }.getOrElse {
            throw AraraExceptionWithHeader(LanguageController.messages
                    .ERROR_INTERPRETER_EXIT_RUNTIME_ERROR, it)
        }.let { check ->
            check as? Boolean
                    ?: throw AraraExceptionWithHeader(
                            LanguageController.messages
                                    .ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN
                    )
        }
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
     * @param rule The rule (only passed for output purposes).
     * @param parameters The parameters for evaluation
     * @throws AraraException Running the command failed.
     */
    @Throws(AraraException::class)
    private fun executeCommand(
        command: SerialRuleCommand,
        rule: Rule,
        parameters: Map<String, Any>
    ) = kotlin.runCatching {
        resultToList(TemplateRuntime.eval(command.commandString!!, parameters))
    }
            .getOrElse {
                throw AraraExceptionWithHeader(LanguageController
                        .messages.ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR, it)
            }
            .filter { it.toString().isNotBlank() }
            .fold(ExecutionStatus.Processing() as ExecutionStatus) { _, current ->
                DisplayUtils.printEntry(rule.displayName!!, command.name
                        ?: LanguageController.messages.INFO_LABEL_UNNAMED_TASK)

                val success = when (current) {
                    is Boolean -> runBoolean(current)
                    is Command -> runCommand(current, command.exit)
                    else ->
                        throw AraraExceptionWithHeader(LanguageController
                                .messages.ERROR_INTERPRETER_WRONG_RETURN_TYPE)
                }

                DisplayUtils.printEntryResult(success)
                when {
                    Session.contains(haltKey) ->
                        throw HaltExpectedException(LanguageController.messages
                                .ERROR_INTERPRETER_USER_REQUESTED_HALT,
                                ExecutionStatus.FinishedWithCode(
                                        Session[haltKey].toString().toInt()))
                    executionOptions.haltOnErrors && !success ->
                        throw HaltExpectedException(LanguageController
                                .messages.ERROR_INTERPRETER_COMMAND_UNSUCCESSFUL_EXIT
                                .format(command.name),
                                ExecutionStatus.ExternalCallFailed())
                    success -> ExecutionStatus.Processing()
                    else -> ExecutionStatus.ExternalCallFailed()
                }
            }

    /**
     * Execute a DSL command.
     *
     * This is a very shallow copy of the logic from [executeCommand].
     *
     * @param command The command to evaluate.
     * @param rule The rule (only passed for output purposes).
     * @throws AraraException Running the command failed.
     */
    // TODO: remove code duplication
    private fun executeDSLCommand(command: RuleCommand, rule: Rule): ExecutionStatus {
        DisplayUtils.printEntry(rule.displayName!!, command.name
                ?: LanguageController.messages.INFO_LABEL_UNNAMED_TASK)

        val exitStatus = ExecutionStatus.FinishedWithCode(command.command())
        DisplayUtils.printEntryResult(exitStatus.exitCode == 0)

        return when {
            Session.contains(haltKey) ->
                throw HaltExpectedException(LanguageController.messages
                        .ERROR_INTERPRETER_USER_REQUESTED_HALT,
                        ExecutionStatus.FinishedWithCode(
                                Session[haltKey].toString().toInt()))
            executionOptions.haltOnErrors && exitStatus.exitCode != 0 ->
                throw HaltExpectedException(LanguageController
                        .messages.ERROR_INTERPRETER_COMMAND_UNSUCCESSFUL_EXIT
                        .format(command.name),
                        ExecutionStatus.ExternalCallFailed())
            exitStatus.exitCode == 0 -> ExecutionStatus.Processing()
            else -> ExecutionStatus.ExternalCallFailed()
        }
    }

    /**
     * Execute the dry run on a command, printing helpful debug information.
     *
     * @param ruleCommand The rule command that will be used to determine the
     *   commands to run.
     * @param rule The rule which is executed.
     * @param conditional The conditions under which this command is executed.
     * @param parameters The parameters for MVEL interpolation.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun dryRunCommand(
        ruleCommand: RuleCommand,
        rule: Rule,
        conditional: DirectiveConditional,
        parameters: Map<String, Any>
    ) {
        DisplayUtils.printEntry(rule.displayName!!, ruleCommand.name
                ?: LanguageController.messages.INFO_LABEL_UNNAMED_TASK)
        DisplayUtils.printAuthors(rule.authors)

        if (ruleCommand is SerialRuleCommand) {
            try {
                resultToList(TemplateRuntime.eval(ruleCommand.commandString!!, parameters))
            } catch (exception: RuntimeException) {
                throw AraraExceptionWithHeader(LanguageController
                        .messages.ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR,
                        exception
                )
            }.filter { it.toString().isNotBlank() }.forEach { current ->
                DisplayUtils.printWrapped(
                        when (current) {
                            is Boolean -> LanguageController.messages
                                    .INFO_INTERPRETER_DRYRUN_MODE_BOOLEAN_MODE
                            is Command -> LanguageController.messages
                                    .INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND
                            else ->
                                throw AraraExceptionWithHeader(LanguageController
                                        .messages.ERROR_INTERPRETER_WRONG_RETURN_TYPE)
                        }.format(current)
                )
            }
        }
        // TODO: “about to run …” is missing for Kotlin DSL

        DisplayUtils.printConditional(conditional)
    }

    /**
     * Executes each directive, throwing an exception if something bad has
     * happened.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    @Suppress("NestedBlockDepth")
    fun execute(directive: Directive): ExecutionStatus {
        logger.info(
                LanguageController.messages.LOG_INFO_INTERPRET_RULE.format(
                        directive.identifier
                )
        )

        val rule = getRuleFor(directive, workingDirectory)
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
                        retValue = rule.commands.fold(ExecutionStatus.Processing()
                                as ExecutionStatus) { _, command ->
                            when {
                                executionOptions.executionMode == ExecutionMode.DRY_RUN ->
                                    dryRunCommand(
                                            command,
                                            rule,
                                            directive.conditional,
                                            parameters
                                    ).let { ExecutionStatus.Processing() }
                                command is SerialRuleCommand ->
                                    executeCommand(
                                            command,
                                            rule,
                                            parameters
                                    )
                                else -> executeDSLCommand(command, rule)
                            }
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
                                    .format(
                                            directive.identifier,
                                            getRuleFromSingleRuleFile(directive, workingDirectory)?.parent.toString()
                                    ) + " " +
                                    e.message, e.exception ?: e)
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
    @Throws(AraraException::class)
    @Suppress("UNCHECKED_CAST")
    private fun parseArguments(rule: Rule, directive: Directive):
            Map<String, Any> {
        val unknown = getUnknownKeys(directive.parameters, rule.arguments)
                .minus("reference")
        if (unknown.isNotEmpty())
            throw AraraExceptionWithHeader(LanguageController.messages
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
