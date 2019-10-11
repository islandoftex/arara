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
import com.github.cereda.arara.ruleset.Command
import com.github.cereda.arara.ruleset.Directive
import com.github.cereda.arara.ruleset.Rule
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
     * Executes each directive, throwing an exception if something bad has
     * happened.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @ExperimentalTime
    @Throws(AraraException::class)
    fun execute() {
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
        fun getRule(directive: Directive): File {
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

        directives.forEach { directive ->
            logger.info(
                    messages.getMessage(
                            Messages.LOG_INFO_INTERPRET_RULE,
                            directive.identifier
                    )
            )

            Arara.config[AraraSpec.Execution.file] =
                    directive.parameters.getValue("reference") as File
            val file = getRule(directive)

            logger.info(
                    messages.getMessage(
                            Messages.LOG_INFO_RULE_LOCATION,
                            file.parent
                    )
            )

            Arara.config[AraraSpec.Execution.InfoSpec.ruleId] =
                    directive.identifier
            Arara.config[AraraSpec.Execution.InfoSpec.rulePath] =
                    file.parent
            Arara.config[AraraSpec.Execution.DirectiveSpec.lines] =
                    directive.lineNumbers

            val rule = parseRule(file, directive)
            val parameters = parseArguments(rule, directive).toMutableMap()
            parameters.putAll(Methods.getRuleMethods())

            val name = rule.name
            val authors = rule.authors

            // save the identifiers of the rule's arguments for later use
            // TODO: never used?!
            Arara.config[AraraSpec.Execution.ruleArguments] =
                    rule.arguments.mapNotNull { it.identifier }

            val evaluator = Evaluator()

            var available = true
            if (InterpreterUtils.runPriorEvaluation(directive.conditional)) {
                available = evaluator.evaluate(directive.conditional)
            }

            if (available) {
                do {
                    val commands = rule.commands
                    for (command in commands) {
                        val closure = command.command
                        val result: Any
                        try {
                            result = TemplateRuntime.eval(closure!!, parameters)
                        } catch (exception: RuntimeException) {
                            throw AraraException(
                                    CommonUtils.ruleErrorHeader + messages.getMessage(
                                            Messages.ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR
                                    ),
                                    exception
                            )
                        }

                        var execution = mutableListOf<Any>()
                        // TODO: check nullability
                        if (result is List<*>) {
                            execution = CommonUtils.flatten(result)
                                    .toMutableList()
                        } else {
                            execution.add(result)
                        }

                        for (current in execution) {
                            if (current == null) {
                                throw AraraException(
                                        CommonUtils.ruleErrorHeader + messages.getMessage(
                                                Messages.ERROR_INTERPRETER_NULL_COMMAND
                                        )
                                )
                            } else {
                                if (current.toString().isNotBlank()) {
                                    DisplayUtils.printEntry(name,
                                            command.name
                                                    ?: messages.getMessage(Messages.INFO_LABEL_UNNAMED_TASK)
                                    )
                                    var success = true

                                    if (current is Trigger) {
                                        if (!Arara.config[AraraSpec.Execution.dryrun]) {
                                            if (Arara.config[AraraSpec.Execution.verbose]) {
                                                DisplayUtils.wrapText(
                                                        messages.getMessage(
                                                                Messages.INFO_INTERPRETER_VERBOSE_MODE_TRIGGER_MODE
                                                        )
                                                )
                                            }
                                        } else {
                                            DisplayUtils.printAuthors(authors)
                                            DisplayUtils.wrapText(
                                                    messages.getMessage(
                                                            Messages.INFO_INTERPRETER_DRYRUN_MODE_TRIGGER_MODE
                                                    )
                                            )
                                            DisplayUtils.printConditional(
                                                    directive.conditional
                                            )
                                        }
                                        current.process()
                                    } else {
                                        if (current is Boolean) {
                                            success = current
                                            logger.info(
                                                    messages.getMessage(
                                                            Messages.LOG_INFO_BOOLEAN_MODE,
                                                            success.toString()
                                                    )
                                            )

                                            if (Arara.config[AraraSpec.Execution.dryrun]) {
                                                DisplayUtils.printAuthors(authors)
                                                DisplayUtils.wrapText(
                                                        messages.getMessage(
                                                                Messages.INFO_INTERPRETER_DRYRUN_MODE_BOOLEAN_MODE,
                                                                success
                                                        )
                                                )
                                                DisplayUtils.printConditional(
                                                        directive.conditional
                                                )
                                            }
                                        } else {
                                            val representation = if (current is Command)
                                                current
                                            else
                                                current.toString()
                                            logger.info(
                                                    messages.getMessage(
                                                            Messages.LOG_INFO_SYSTEM_COMMAND,
                                                            representation
                                                    )
                                            )

                                            if (!Arara.config[AraraSpec.Execution.dryrun]) {
                                                val code = InterpreterUtils.run(representation)
                                                val check: Any
                                                try {
                                                    val context = mutableMapOf<String, Any>()
                                                    context["value"] = code
                                                    check = TemplateRuntime.eval(
                                                            "@{ " + (if (command.exit == null)
                                                                "value == 0"
                                                            else
                                                                command.exit) + " }",
                                                            context)
                                                } catch (exception: RuntimeException) {
                                                    throw AraraException(
                                                            CommonUtils.ruleErrorHeader + messages.getMessage(
                                                                    Messages.ERROR_INTERPRETER_EXIT_RUNTIME_ERROR
                                                            ),
                                                            exception
                                                    )
                                                }

                                                if (check is Boolean) {
                                                    success = check
                                                } else {
                                                    throw AraraException(
                                                            CommonUtils.ruleErrorHeader + messages.getMessage(
                                                                    Messages.ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN
                                                            )
                                                    )
                                                }
                                            } else {
                                                DisplayUtils.printAuthors(authors)
                                                DisplayUtils.wrapText(
                                                        messages.getMessage(
                                                                Messages.INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND,
                                                                representation
                                                        )
                                                )
                                                DisplayUtils.printConditional(
                                                        directive.conditional
                                                )
                                            }
                                        }
                                    }

                                    DisplayUtils.printEntryResult(success)

                                    if (Arara.config[AraraSpec.Trigger.halt]
                                            || Arara.config[AraraSpec.Execution.haltOnErrors]
                                            && !success)
                                        return
                                }
                            }
                        }
                    }
                } while (evaluator.evaluate(directive.conditional))
            }
        }
    }

    /**
     * Parses the rule against the provided directive.
     *
     * @param file      The file representing the rule.
     * @param directive The directive to be analyzed.
     * @return A rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun parseRule(file: File, directive: Directive): Rule {
        return RuleUtils.parseRule(file, directive.identifier)
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
    private fun parseArguments(rule: Rule, directive: Directive): Map<String, Any> {
        val arguments = rule.arguments
        val unknown = CommonUtils.getUnknownKeys(directive.parameters, arguments)
                .toMutableSet()
        unknown.remove("file")
        unknown.remove("reference")
        if (unknown.isNotEmpty()) {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_INTERPRETER_UNKNOWN_KEYS,
                            "(" + unknown.joinToString(", ") + ")")
            )
        }

        val mapping = mutableMapOf<String, Any>()
        mapping["file"] = directive.parameters.getValue("file")
        mapping["reference"] = directive.parameters.getValue("reference")

        val context = mutableMapOf<String, Any>()
        context["parameters"] = directive.parameters
        context["file"] = directive.parameters.getValue("file")
        context["reference"] = directive.parameters.getValue("reference")
        context.putAll(Methods.getRuleMethods())

        for (argument in arguments) {
            if (argument.isRequired && !directive.parameters.containsKey(
                            argument.identifier)) {
                throw AraraException(
                        CommonUtils.ruleErrorHeader + messages.getMessage(
                                Messages.ERROR_INTERPRETER_ARGUMENT_IS_REQUIRED,
                                argument.identifier!!
                        )
                )
            }

            if (argument.default != null) {
                try {
                    val result = TemplateRuntime.eval(argument.default!!, context)
                    mapping[argument.identifier!!] = result
                } catch (exception: RuntimeException) {
                    throw AraraException(
                            CommonUtils.ruleErrorHeader + messages.getMessage(
                                    Messages.ERROR_INTERPRETER_DEFAULT_VALUE_RUNTIME_ERROR
                            ),
                            exception
                    )
                }

            } else {
                mapping[argument.identifier!!] = ""
            }

            if (argument.flag != null && directive.parameters.containsKey(
                            argument.identifier!!)) {

                try {
                    val result = TemplateRuntime.eval(
                            argument.flag!!,
                            context
                    )
                    mapping[argument.identifier!!] = result
                } catch (exception: RuntimeException) {
                    throw AraraException(CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_INTERPRETER_FLAG_RUNTIME_EXCEPTION
                    ),
                            exception
                    )
                }

            }
        }

        return mapping
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
