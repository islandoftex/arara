/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.model

import com.github.cereda.arara.Arara
import com.github.cereda.arara.configuration.AraraSpec
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.ruleset.Argument
import com.github.cereda.arara.ruleset.Command
import com.github.cereda.arara.ruleset.Conditional
import com.github.cereda.arara.ruleset.Directive
import com.github.cereda.arara.ruleset.Rule
import com.github.cereda.arara.ruleset.RuleCommand
import com.github.cereda.arara.ruleset.RuleUtils
import com.github.cereda.arara.utils.CommonUtils
import com.github.cereda.arara.utils.DisplayUtils
import com.github.cereda.arara.utils.InterpreterUtils
import com.github.cereda.arara.utils.Methods
import org.mvel2.templates.TemplateRuntime
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.ExperimentalTime

/**
 * Interprets the list of directives.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class Interpreter(
        // list of directives to be
        // interpreted in here
        val directives: List<Directive>) {
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
    private fun runBoolean(value: Boolean, conditional: Conditional,
                           authors: List<String>): Boolean {
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

    @ExperimentalTime
    @Throws(AraraException::class)
    private fun runCommand(current: Command, conditional: Conditional,
                           authors: List<String>,
                           ruleCommandExitValue: String?): Boolean {
        logger.info(messages.getMessage(Messages.LOG_INFO_SYSTEM_COMMAND,
                current))
        var success = true

        if (!Arara.config[AraraSpec.Execution.dryrun]) {
            val code = InterpreterUtils.run(current)
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
                    current))
            DisplayUtils.printConditional(conditional)
        }

        return success
    }

    // TODO: document
    @ExperimentalTime
    private fun executeCommand(command: RuleCommand, directive: Directive,
                               rule: Rule, parameters: Map<String, Any>) {
        val result: Any = try {
            TemplateRuntime.eval(command.command!!, parameters)
        } catch (exception: RuntimeException) {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR
                    ),
                    exception
            )
        }

        // TODO: check nullability
        val execution = if (result is List<*>) {
            CommonUtils.flatten(result)
        } else {
            listOf(result)
        }

        execution.forEach { current ->
            if (current.toString().isNotBlank()) {
                DisplayUtils.printEntry(rule.name, command.name
                        ?: messages.getMessage(Messages
                                .INFO_LABEL_UNNAMED_TASK))

                val success = when (current) {
                    is Boolean -> runBoolean(current, directive.conditional,
                            rule.authors)
                    is Command -> runCommand(current, directive.conditional,
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
    }

    /**
     * Executes each directive, throwing an exception if something bad has
     * happened.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @ExperimentalTime
    @Throws(AraraException::class)
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
            val parameters = parseArguments(rule, directive).toMutableMap()
            parameters.putAll(Methods.getRuleMethods())

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
                        executeCommand(command, directive, rule, parameters)
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
     * @param rule      The rule object.
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
    private fun processArgument(argument: Argument, idInDirectiveParams: Boolean,
                                context: Map<String, Any>): Any {
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
