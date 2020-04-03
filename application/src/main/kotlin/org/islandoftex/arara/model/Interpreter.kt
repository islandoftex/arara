// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.model

import java.io.File
import org.islandoftex.arara.Arara
import org.islandoftex.arara.configuration.AraraSpec
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.ruleset.Argument
import org.islandoftex.arara.ruleset.Command
import org.islandoftex.arara.ruleset.Conditional
import org.islandoftex.arara.ruleset.Directive
import org.islandoftex.arara.ruleset.Rule
import org.islandoftex.arara.ruleset.RuleCommand
import org.islandoftex.arara.ruleset.RuleUtils
import org.islandoftex.arara.utils.CommonUtils
import org.islandoftex.arara.utils.DisplayUtils
import org.islandoftex.arara.utils.InterpreterUtils
import org.islandoftex.arara.utils.Methods
import org.mvel2.templates.TemplateRuntime
import org.slf4j.LoggerFactory

/**
 * Interprets the list of directives.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class Interpreter(
    /**
     * The list of directives to be interpreted and evaluated.
     */
    val directives: List<Directive>
) {
    /**
     * Exception class to represent that the interpreter should stop for some
     * reason
     */
    private class HaltExpectedException(msg: String) : Exception(msg)

    /**
     * Gets the rule according to the provided directive.
     *
     * @param directive The provided directive.
     * @return The absolute canonical path of the rule, given the provided
     * directive.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun getRule(directive: Directive): File {
        return InterpreterUtils.buildRulePath(directive.identifier)
                ?: throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_INTERPRETER_RULE_NOT_FOUND,
                                directive.identifier,
                                "(" + CommonUtils.allRulePaths
                                        .joinToString("; ") + ")"
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
    private fun runBoolean(
        value: Boolean,
        conditional: Conditional,
        authors: List<String>
    ): Boolean {
        logger.info(messages.getMessage(Messages.LOG_INFO_BOOLEAN_MODE,
                value.toString()))

        if (Arara.config[AraraSpec.Execution.dryrun]) {
            DisplayUtils.printAuthors(authors)
            DisplayUtils.wrapText(messages.getMessage(Messages
                    .INFO_INTERPRETER_DRYRUN_MODE_BOOLEAN_MODE,
                    value))
            DisplayUtils.printConditional(conditional)
        }

        return value
    }

    /**
     * Run a command
     * @param command The command to run.
     * @param conditional The conditional applied to the run (only for printing).
     * @param authors The rule authors (only for printing).
     * @param ruleCommandExitValue The exit value of the rule command.
     * @return Success of the execution.
     * @throws AraraException Execution failed.
     */
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught")
    private fun runCommand(
        command: Command,
        conditional: Conditional,
        authors: List<String>,
        ruleCommandExitValue: String?
    ): Boolean {
        logger.info(messages.getMessage(Messages.LOG_INFO_SYSTEM_COMMAND,
                command))
        var success = true

        if (!Arara.config[AraraSpec.Execution.dryrun]) {
            val code = InterpreterUtils.run(command)
            val check: Any = try {
                val context = mapOf<String, Any>("value" to code)
                TemplateRuntime.eval(
                        "@{ " + (ruleCommandExitValue ?: "value == 0") + " }",
                        context)
            } catch (exception: RuntimeException) {
                throw AraraException(CommonUtils.ruleErrorHeader +
                        messages.getMessage(Messages
                                .ERROR_INTERPRETER_EXIT_RUNTIME_ERROR),
                        exception)
            }

            success = if (check is Boolean) {
                check
            } else {
                throw AraraException(
                        CommonUtils.ruleErrorHeader + messages.getMessage(
                                Messages.ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN
                        )
                )
            }
        } else {
            DisplayUtils.printAuthors(authors)
            DisplayUtils.wrapText(messages.getMessage(
                    Messages.INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND,
                    command))
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
        CommonUtils.flatten(result)
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
    @Suppress("TooGenericExceptionCaught", "ThrowsCount")
    private fun executeCommand(
        command: RuleCommand,
        conditional: Conditional,
        rule: Rule,
        parameters: Map<String, Any>
    ) {
        val result: Any = try {
            TemplateRuntime.eval(command.command!!, parameters)
        } catch (exception: RuntimeException) {
            throw AraraException(CommonUtils.ruleErrorHeader +
                    messages.getMessage(Messages
                            .ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR),
                    exception)
        }

        // TODO: check nullability
        resultToList(result).filter { it.toString().isNotBlank() }
                .forEach { current ->
                    DisplayUtils.printEntry(rule.name, command.name
                            ?: messages.getMessage(Messages
                                    .INFO_LABEL_UNNAMED_TASK))

                    val success = when (current) {
                        is Boolean -> runBoolean(current, conditional,
                                rule.authors)
                        is Command -> runCommand(current, conditional,
                                rule.authors, command.exit)
                        else -> TODO("error: this should not happen" +
                                "we are only supporting boolean + command")
                    }

                    DisplayUtils.printEntryResult(success)

                    if (Arara.config[AraraSpec.Execution.haltOnErrors] && !success)
                    // TODO: localize
                        throw HaltExpectedException("Command failed")

                    // TODO: document this key
                    val haltKey = "arara:${Arara.config[AraraSpec
                            .Execution.reference].name}:halt"
                    if (Session.contains(haltKey)) {
                        Arara.config[AraraSpec.Execution.status] =
                                Session[haltKey].toString().toInt()
                        // TODO: localize
                        throw HaltExpectedException("User requested halt")
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
    @Suppress("NestedBlockDepth")
    fun execute() {
        for (directive in directives) {
            logger.info(messages.getMessage(Messages.LOG_INFO_INTERPRET_RULE,
                    directive.identifier))

            Arara.config[AraraSpec.Execution.file] =
                    directive.parameters.getValue("reference") as File
            val file = getRule(directive)

            logger.info(messages.getMessage(Messages.LOG_INFO_RULE_LOCATION,
                    file.parent))

            Arara.config[AraraSpec.Execution.InfoSpec.ruleId] =
                    directive.identifier
            Arara.config[AraraSpec.Execution.InfoSpec.rulePath] =
                    file.parent
            Arara.config[AraraSpec.Execution.DirectiveSpec.lines] =
                    directive.lineNumbers

            // parse the rule identified by the directive
            // (may throw an exception)
            val rule = RuleUtils.parseRule(file, directive.identifier)
            val parameters = parseArguments(rule, directive)
                    .plus(Methods.getRuleMethods())

            val evaluator = Evaluator()

            var available = true
            if (InterpreterUtils.runPriorEvaluation(directive.conditional)) {
                available = evaluator.evaluate(directive.conditional)
            }

            // if this directive is conditionally disabled, skip
            if (!available) continue
            // if not execute the commands associated with the directive
            do {
                rule.commands.forEach { command ->
                    try {
                        executeCommand(command, directive.conditional, rule, parameters)
                    } catch (_: HaltExpectedException) {
                        // if the user uses the halt rule to trigger
                        // a halt, this will be raised
                        return
                    }
                }
            } while (evaluator.evaluate(directive.conditional))
        }
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
    private fun parseArguments(rule: Rule, directive: Directive):
            Map<String, Any> {
        val arguments = rule.arguments
        val unknown = CommonUtils.getUnknownKeys(directive.parameters,
                arguments).minus("reference")
        if (unknown.isNotEmpty())
            throw AraraException(CommonUtils.ruleErrorHeader +
                    messages.getMessage(
                            Messages.ERROR_INTERPRETER_UNKNOWN_KEYS,
                            "(" + unknown.joinToString(", ") + ")"))

        val resolvedArguments = mutableMapOf<String, Any>()
        resolvedArguments["reference"] = directive.parameters
                .getValue("reference")

        val context = mapOf(
                "parameters" to directive.parameters,
                "reference" to directive.parameters.getValue("reference")
        ).plus(Methods.getRuleMethods())

        arguments.forEach { argument ->
            resolvedArguments[argument.identifier!!] = processArgument(argument,
                    directive.parameters.containsKey(argument.identifier!!),
                    context)
        }

        return resolvedArguments
    }

    /**
     * Process a single argument and return the evaluated result.
     * @param argument The argument to process.
     * @param idInDirectiveParams Whether the argument's identifier is
     *   contained in the directive's parameters field.
     * @param context The context for the evaluation.
     * @return The result of the evaluation.
     * @throws AraraException The argument could not be processed.
     */
    @Throws(AraraException::class)
    @Suppress("TooGenericExceptionCaught", "ThrowsCount")
    private fun processArgument(
        argument: Argument,
        idInDirectiveParams: Boolean,
        context: Map<String, Any>
    ): Any {
        if (argument.isRequired && !idInDirectiveParams)
            throw AraraException(CommonUtils.ruleErrorHeader +
                    messages.getMessage(
                            Messages.ERROR_INTERPRETER_ARGUMENT_IS_REQUIRED,
                            argument.identifier!!))

        var ret = argument.default?.let {
            try {
                TemplateRuntime.eval(it, context)
            } catch (exception: RuntimeException) {
                throw AraraException(CommonUtils.ruleErrorHeader +
                        messages.getMessage(Messages
                                .ERROR_INTERPRETER_DEFAULT_VALUE_RUNTIME_ERROR),
                        exception)
            }
        } ?: ""

        if (argument.flag != null && idInDirectiveParams) {
            ret = try {
                TemplateRuntime.eval(argument.flag!!, context)
            } catch (exception: RuntimeException) {
                throw AraraException(CommonUtils.ruleErrorHeader + messages
                        .getMessage(Messages
                                .ERROR_INTERPRETER_FLAG_RUNTIME_EXCEPTION),
                        exception)
            }
        }

        return ret
    }

    companion object {
        // the application messages obtained from the
        // language controller
        private val messages = LanguageController

        // the class logger obtained from
        // the logger factory
        private val logger = LoggerFactory.getLogger(Interpreter::class.java)
    }
}
