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

import com.github.cereda.arara.controller.ConfigurationController
import com.github.cereda.arara.controller.LanguageController
import com.github.cereda.arara.utils.*
import org.mvel2.templates.TemplateRuntime
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/**
 * Interprets the list of directives.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class Interpreter {
    // list of directives to be
    // interpreted in here
    private var directives: List<Directive>? = null

    /**
     * Sets the list of directives.
     *
     * @param directives The list of directives.
     */
    fun setDirectives(directives: List<Directive>) {
        this.directives = directives
    }

    /**
     * Executes each directive, throwing an exception if something bad has
     * happened.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun execute() {
        for (directive in directives!!) {
            logger.info(
                    messages.getMessage(
                            Messages.LOG_INFO_INTERPRET_RULE,
                            directive.identifier!!
                    )
            )

            ConfigurationController.put("execution.file",
                    directive.parameters!!["reference"]!!
            )
            val file = getRule(directive)

            logger.info(
                    messages.getMessage(
                            Messages.LOG_INFO_RULE_LOCATION,
                            file.parent
                    )
            )

            ConfigurationController.put("execution.info.rule.id", directive.identifier!!)
            ConfigurationController.put("execution.info.rule.path", file.parent)
            ConfigurationController.put("execution.directive.lines",
                    directive.lineNumbers!!
            )
            ConfigurationController.put("execution.directive.reference",
                    directive.parameters!!["reference"]!!
            )

            val rule = parseRule(file, directive)
            val parameters = parseArguments(rule, directive)
            Methods.addRuleMethods(parameters.toMutableMap())

            val name = rule.name
            val authors = if (rule.authors == null)
                listOf()
            else
                rule.authors
            ConfigurationController.put("execution.rule.arguments",
                    InterpreterUtils.getRuleArguments(rule)
            )

            val evaluator = Evaluator()

            var available = true
            if (InterpreterUtils.runPriorEvaluation(directive.conditional!!)) {
                available = evaluator.evaluate(directive.conditional!!)
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
                        if (CommonUtils.checkClass(List::class.java, result)) {
                            execution = CommonUtils.flatten(result as List<*>)
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

                                if (current.toString().isNotEmpty()) {
                                    // TODO: check null handling
                                    DisplayUtils.printEntry(name ?: "NO NAME",
                                            command.name
                                                    ?: messages.getMessage(Messages.INFO_LABEL_UNNAMED_TASK)
                                    )
                                    var success = true

                                    if (CommonUtils.checkClass(
                                                    Trigger::class.java, current)) {
                                        if (!(ConfigurationController["execution.dryrun"] as Boolean)) {
                                            if (ConfigurationController["execution.verbose"] as Boolean) {
                                                DisplayUtils.wrapText(
                                                        messages.getMessage(
                                                                Messages.INFO_INTERPRETER_VERBOSE_MODE_TRIGGER_MODE
                                                        )
                                                )
                                            }
                                        } else {
                                            DisplayUtils.printAuthors(authors!!)
                                            DisplayUtils.wrapText(
                                                    messages.getMessage(
                                                            Messages.INFO_INTERPRETER_DRYRUN_MODE_TRIGGER_MODE
                                                    )
                                            )
                                            DisplayUtils.printConditional(
                                                    directive.conditional!!
                                            )
                                        }
                                        val trigger = current as Trigger
                                        trigger.process()
                                    } else {
                                        if (CommonUtils.checkClass(
                                                        Boolean::class.java, current)) {
                                            success = current as Boolean
                                            logger.info(
                                                    messages.getMessage(
                                                            Messages.LOG_INFO_BOOLEAN_MODE,
                                                            success.toString()
                                                    )
                                            )

                                            if (ConfigurationController["execution.dryrun"] as Boolean) {
                                                DisplayUtils.printAuthors(authors!!)
                                                DisplayUtils.wrapText(
                                                        messages.getMessage(
                                                                Messages.INFO_INTERPRETER_DRYRUN_MODE_BOOLEAN_MODE,
                                                                success
                                                        )
                                                )
                                                DisplayUtils.printConditional(
                                                        directive.conditional!!
                                                )
                                            }
                                        } else {

                                            val representation = if (CommonUtils.checkClass(
                                                            Command::class.java,
                                                            current
                                                    ))
                                                current
                                            else
                                                current.toString()
                                            logger.info(
                                                    messages.getMessage(
                                                            Messages.LOG_INFO_SYSTEM_COMMAND,
                                                            representation
                                                    )
                                            )

                                            if (!(ConfigurationController["execution.dryrun"] as Boolean)) {
                                                val code = InterpreterUtils.run(representation)
                                                val check: Any
                                                try {
                                                    val context = HashMap<String, Any>()
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

                                                // TODO: check equality with
                                                // if (CommonUtils.checkClass(Boolean::class.java,check)) {
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
                                                DisplayUtils.printAuthors(authors!!)
                                                DisplayUtils.wrapText(
                                                        messages.getMessage(
                                                                Messages.INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND,
                                                                representation
                                                        )
                                                )
                                                DisplayUtils.printConditional(
                                                        directive.conditional!!
                                                )
                                            }
                                        }
                                    }

                                    DisplayUtils.printEntryResult(success)

                                    if (ConfigurationController["trigger.halt"] as Boolean
                                            || ConfigurationController["execution.errors.halt"] as Boolean
                                            && !success) {
                                        return
                                    }
                                }
                            }
                        }
                    }
                } while (evaluator.evaluate(directive.conditional!!))
            }
        }
    }

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
        return InterpreterUtils.buildRulePath(directive.identifier!!)
                ?: throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_INTERPRETER_RULE_NOT_FOUND,
                                directive.identifier!!,
                                "(" + CommonUtils.allRulePaths
                                        .joinToString("; ") + ")"
                        )
                )
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
        return RuleUtils.parseRule(file, directive.identifier!!)
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

        val unknown = CommonUtils.getUnknownKeys(directive.parameters!!, arguments)
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
        mapping["file"] = directive.parameters!!["file"]!!
        mapping["reference"] = directive.parameters!!["reference"]!!

        val context = mutableMapOf<String, Any>()
        context["parameters"] = directive.parameters!!
        context["file"] = directive.parameters!!["file"]!!
        context["reference"] = directive.parameters!!["reference"]!!
        Methods.addRuleMethods(context)

        for (argument in arguments) {
            if (argument.isRequired && !directive.parameters!!.containsKey(
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

            if (argument.flag != null && directive.parameters!!.containsKey(
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
