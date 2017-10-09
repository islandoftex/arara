/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2017, Paulo Roberto Massa Cereda 
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
package com.github.cereda.arara.utils;

import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.model.AraraException;
import com.github.cereda.arara.model.Argument;
import com.github.cereda.arara.model.Messages;
import com.github.cereda.arara.model.RuleCommand;
import com.github.cereda.arara.model.Rule;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Implements rule utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class RuleUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Parses the provided file, checks the identifier and returns a rule
     * representation.
     * @param file The rule file.
     * @param identifier The directive identifier.
     * @return The rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Rule parseRule(File file, String identifier)
            throws AraraException {
        Representer representer = new Representer();
        representer.addClassTag(Rule.class, new Tag("!config"));
        Yaml yaml = new Yaml(new Constructor(Rule.class), representer);
        Rule rule = null;
        try {
            rule = yaml.loadAs(new FileReader(file), Rule.class);
        } catch (MarkedYAMLException yamlException) {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_PARSERULE_INVALID_YAML
                            )
                    ),
                    yamlException
            );
        } catch (Exception exception) {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_PARSERULE_GENERIC_ERROR
                            )
                    )
            );
        }
        validateHeader(rule, identifier);
        validateBody(rule);
        return rule;
    }

    /**
     * Validates the rule header according to the directive identifier.
     * @param rule The rule object.
     * @param identifier The directive identifier.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private static void validateHeader(Rule rule, String identifier)
            throws AraraException {
        if (rule.getIdentifier() != null) {
            if (!rule.getIdentifier().equals(identifier)) {
                throw new AraraException(CommonUtils.getRuleErrorHeader().
                        concat(
                                messages.getMessage(
                                        Messages.ERROR_VALIDATEHEADER_WRONG_IDENTIFIER,
                                        rule.getIdentifier(),
                                        identifier
                                )
                        )
                );
            }
        } else {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATEHEADER_NULL_ID
                            )
                    )
            );
        }
        if (rule.getName() == null) {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATEHEADER_NULL_NAME
                            )
                    )
            );
        }
    }

    /**
     * Validates the rule body.
     * @param rule The rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private static void validateBody(Rule rule) throws AraraException {
        if (rule.getCommands() == null) {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATEBODY_NULL_COMMANDS_LIST
                            )
                    )
            );
        } else {
            if (CollectionUtils.exists(rule.getCommands(),
                    new Predicate<RuleCommand>() {
                public boolean evaluate(RuleCommand command) {
                    return (command.getCommand() == null);
                }
            })) {
                throw new AraraException(CommonUtils.getRuleErrorHeader().
                        concat(
                                messages.getMessage(
                                        Messages.ERROR_VALIDATEBODY_NULL_COMMAND
                                )
                        )
                );
            }
        }
        if (rule.getArguments() == null) {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATEBODY_ARGUMENTS_LIST
                            )
                    )
            );
        } else {
            String[] keywords = new String[]{"file", "files", "reference"};

            List<String> arguments = new ArrayList<String>();
            for (Argument argument : rule.getArguments()) {
                if (argument.getIdentifier() != null) {
                    if ((argument.getFlag() != null) ||
                            (argument.getDefault() != null)) {
                        arguments.add(argument.getIdentifier());
                    } else {
                        throw new AraraException(
                                CommonUtils.getRuleErrorHeader().concat(
                                        messages.getMessage(
                                                Messages.ERROR_VALIDATEBODY_MISSING_KEYS
                                        )
                                )
                        );
                    }
                } else {
                    throw new AraraException(
                            CommonUtils.getRuleErrorHeader().concat(
                                    messages.getMessage(
                                            Messages.ERROR_VALIDATEBODY_NULL_ARGUMENT_ID
                                    )
                            )
                    );
                }
            }

            for (String keyword : keywords) {
                if (arguments.contains(keyword)) {
                    throw new AraraException(
                            CommonUtils.getRuleErrorHeader().concat(
                                    messages.getMessage(
                                            Messages.ERROR_VALIDATEBODY_ARGUMENT_ID_IS_RESERVED,
                                            keyword
                                    )
                            )
                    );
                }
            }

            int expected = arguments.size();
            int found = (new HashSet<String>(arguments)).size();
            if (expected != found) {
                throw new AraraException(
                        CommonUtils.getRuleErrorHeader().concat(
                                messages.getMessage(
                                        Messages.ERROR_VALIDATEBODY_DUPLICATE_ARGUMENT_IDENTIFIERS
                                )
                        )
                );
            }
        }
    }

}
