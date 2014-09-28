/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda 
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
package com.github.cereda.arara.model;

import com.github.cereda.arara.controller.ConfigurationController;
import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.utils.CommonUtils;
import com.github.cereda.arara.utils.DisplayUtils;
import com.github.cereda.arara.utils.InterpreterUtils;
import com.github.cereda.arara.utils.Methods;
import com.github.cereda.arara.utils.RuleUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interprets the list of directives.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Interpreter {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();
    
    // the class logger obtained from
    // the logger factory
    private static final Logger logger =
            LoggerFactory.getLogger(Interpreter.class);

    // list of directives to be
    // interpreted in here
    private List<Directive> directives;

    /**
     * Sets the list of directives.
     * @param directives The list of directives.
     */
    public void setDirectives(List<Directive> directives) {
        this.directives = directives;
    }

    /**
     * Executes each directive, throwing an exception if something bad has
     * happened.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public void execute() throws AraraException {

        for (Directive directive : directives) {

            logger.info(
                    messages.getMessage(
                            Messages.LOG_INFO_INTERPRET_RULE,
                            directive.getIdentifier()
                    )
            );

            ConfigurationController.getInstance().
                    put("execution.file",
                            directive.getParameters().get("reference")
                    );
            File file = getRule(directive);

            logger.info(
                    messages.getMessage(
                            Messages.LOG_INFO_RULE_LOCATION,
                            file.getParent()
                    )
            );

            ConfigurationController.getInstance().
                    put("execution.info.rule.id", directive.getIdentifier());
            ConfigurationController.getInstance().
                    put("execution.info.rule.path", file.getParent());
            ConfigurationController.getInstance().
                    put("execution.directive.lines",
                            directive.getLineNumbers()
                    );

            Rule rule = parseRule(file, directive);
            Map<String, Object> parameters = parseArguments(rule, directive);
            Methods.addRuleMethods(parameters);

            String name = rule.getName();
            List<String> authors = rule.getAuthors() == null ?
                    new ArrayList<String>() : rule.getAuthors();
            ConfigurationController.getInstance().
                    put("execution.rule.arguments",
                            InterpreterUtils.getRuleArguments(rule)
                    );

            Evaluator evaluator = new Evaluator();

            boolean available = true;
            if (InterpreterUtils.
                    runPriorEvaluation(directive.getConditional())) {
                available = evaluator.evaluate(directive.getConditional());
            }

            if (available) {

                do {

                    List<RuleCommand> commands = rule.getCommands();
                    for (RuleCommand command : commands) {
                        String closure = command.getCommand();
                        Object result = null;
                        try {
                            result = TemplateRuntime.eval(closure, parameters);
                        } catch (RuntimeException exception) {
                            throw new AraraException(
                                    CommonUtils.getRuleErrorHeader().concat(
                                            messages.getMessage(
                                                    Messages.ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR
                                            )
                                    ),
                                    exception
                            );
                        }

                        List<Object> execution = new ArrayList<Object>();
                        if (CommonUtils.checkClass(List.class, result)) {
                            execution = CommonUtils.flatten((List<?>) result);
                        } else {
                            execution.add(result);
                        }

                        for (Object current : execution) {

                            if (current == null) {
                                throw new AraraException(
                                        CommonUtils.getRuleErrorHeader().concat(
                                                messages.getMessage(
                                                        Messages.ERROR_INTERPRETER_NULL_COMMAND
                                                )
                                        )
                                );
                            } else {

                                if (!CommonUtils.checkEmptyString(
                                        String.valueOf(current))) {

                                    DisplayUtils.printEntry(name,
                                            command.getName() == null ?
                                                    messages.getMessage(
                                                            Messages.INFO_LABEL_UNNAMED_TASK
                                                    )
                                                    : command.getName()
                                    );
                                    boolean success = true;

                                    if (CommonUtils.checkClass(
                                            Trigger.class, current)) {
                                        if (((Boolean) ConfigurationController.
                                                getInstance().
                                                get("execution.dryrun")) == false) {
                                            if (((Boolean) ConfigurationController.
                                                    getInstance().
                                                    get("execution.verbose")) == true) {
                                                DisplayUtils.wrapText(
                                                        messages.getMessage(
                                                                Messages.INFO_INTERPRETER_VERBOSE_MODE_TRIGGER_MODE
                                                        )
                                                );
                                            }
                                        } else {
                                            DisplayUtils.printAuthors(authors);
                                            DisplayUtils.wrapText(
                                                    messages.getMessage(
                                                            Messages.INFO_INTERPRETER_DRYRUN_MODE_TRIGGER_MODE
                                                    )
                                            );
                                            DisplayUtils.printConditional(
                                                    directive.getConditional()
                                            );
                                        }
                                        Trigger trigger = (Trigger) current;
                                        trigger.process();
                                    } else {
                                        Object representation =
                                                CommonUtils.checkClass(
                                                        Command.class,
                                                        current
                                                ) ?
                                                current 
                                                : String.valueOf(current);
                                        logger.info(
                                                messages.getMessage(
                                                        Messages.LOG_INFO_SYSTEM_COMMAND,
                                                        representation
                                                )
                                        );

                                        if (((Boolean) ConfigurationController.
                                                getInstance().
                                                get("execution.dryrun")) == false) {
                                            int code = InterpreterUtils.
                                                    run(representation);
                                            Object check = null;
                                            try {
                                                Map<String, Object> context =
                                                        new HashMap<String, Object>();
                                                context.put("value", code);
                                                check = TemplateRuntime.eval(
                                                        "@{ ".concat(
                                                                command.getExit() == null ?
                                                                        "value == 0"
                                                                        : command.getExit()).concat(" }"),
                                                        context);
                                            } catch (RuntimeException exception) {
                                                throw new AraraException(
                                                        CommonUtils.getRuleErrorHeader().
                                                                concat(
                                                                        messages.getMessage(
                                                                                Messages.ERROR_INTERPRETER_EXIT_RUNTIME_ERROR
                                                                        )
                                                                ),
                                                        exception
                                                );
                                            }
                                            if (CommonUtils.
                                                    checkClass(
                                                            Boolean.class,
                                                            check)) {
                                                success = (Boolean) check;
                                            } else {
                                                throw new AraraException(
                                                        CommonUtils.getRuleErrorHeader().concat(
                                                                messages.getMessage(
                                                                        Messages.ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN
                                                                )
                                                        )
                                                );
                                            }
                                        } else {
                                            DisplayUtils.printAuthors(authors);
                                            DisplayUtils.wrapText(
                                                    messages.getMessage(
                                                            Messages.INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND,
                                                            representation
                                                    )
                                            );
                                            DisplayUtils.printConditional(
                                                    directive.getConditional()
                                            );
                                        }
                                    }

                                    DisplayUtils.printEntryResult(success);

                                    if (((Boolean) ConfigurationController.
                                            getInstance().get("trigger.halt"))
                                            || (((Boolean) ConfigurationController.
                                                    getInstance().
                                                    get("execution.errors.halt")
                                            && !success))) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                } while (evaluator.evaluate(directive.getConditional()));
            }
        }
    }

    /**
     * Gets the rule according to the provided directive.
     * @param directive The provided directive.
     * @return The absolute canonical path of the rule, given the provided
     * directive.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private File getRule(Directive directive) throws AraraException {
        File file = InterpreterUtils.buildRulePath(directive.getIdentifier());
        if (file == null) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_INTERPRETER_RULE_NOT_FOUND,
                            directive.getIdentifier(),
                            CommonUtils.getCollectionElements(
                                    CommonUtils.getAllRulePaths(),
                                    "(",
                                    ")",
                                    "; "
                            )
                    )
            );
        } else {
            return file;
        }
    }

    /**
     * Parses the rule against the provided directive.
     * @param file The file representing the rule.
     * @param directive The directive to be analyzed.
     * @return A rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private Rule parseRule(File file, Directive directive)
            throws AraraException {
        return RuleUtils.parseRule(file, directive.getIdentifier());
    }

    /**
     * Parses the rule arguments against the provided directive.
     * @param rule The rule object.
     * @param directive The directive.
     * @return A map containing all arguments resolved according to the
     * directive parameters.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private Map<String, Object> parseArguments(Rule rule, Directive directive)
            throws AraraException {

        List<Argument> arguments = rule.getArguments();

        Set<String> unknown = CommonUtils.
                getUnknownKeys(directive.getParameters(), arguments);
        unknown.remove("file");
        unknown.remove("reference");
        if (!unknown.isEmpty()) {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_INTERPRETER_UNKNOWN_KEYS,
                                    CommonUtils.getCollectionElements(
                                            unknown,
                                            "(",
                                            ")",
                                            ", "
                                    )
                            )
                    )
            );
        }

        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("file", directive.getParameters().get("file"));
        mapping.put("reference", directive.getParameters().get("reference"));

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("parameters", directive.getParameters());
        Methods.addRuleMethods(context);

        for (Argument argument : arguments) {
            if ((argument.isRequired()) &&
                    (!directive.getParameters().containsKey(
                            argument.getIdentifier()))) {
                throw new AraraException(
                        CommonUtils.getRuleErrorHeader().concat(
                                messages.getMessage(
                                        Messages.ERROR_INTERPRETER_ARGUMENT_IS_REQUIRED,
                                        argument.getIdentifier()
                                )
                        )
                );
            }

            if (argument.getDefault() != null) {
                try {
                    Object result = TemplateRuntime.
                            eval(argument.getDefault(), context);
                    mapping.put(argument.getIdentifier(), result);
                } catch (RuntimeException exception) {
                    throw new AraraException(
                            CommonUtils.getRuleErrorHeader().
                                    concat(messages.getMessage(
                                            Messages.ERROR_INTERPRETER_DEFAULT_VALUE_RUNTIME_ERROR
                                    )
                            ),
                            exception
                    );
                }
            } else {
                mapping.put(argument.getIdentifier(), "");
            }

            if ((argument.getFlag() != null) &&
                    (directive.getParameters().containsKey(
                            argument.getIdentifier()))) {

                try {
                    Object result = TemplateRuntime.eval(
                            argument.getFlag(),
                            context
                    );
                    mapping.put(argument.getIdentifier(), result);
                } catch (RuntimeException exception) {
                    throw new AraraException(CommonUtils.getRuleErrorHeader().
                            concat(
                                    messages.getMessage(
                                            Messages.ERROR_INTERPRETER_FLAG_RUNTIME_EXCEPTION
                                    )
                            ),
                            exception
                    );
                }
            }
        }
        
        return mapping;
    }

}
