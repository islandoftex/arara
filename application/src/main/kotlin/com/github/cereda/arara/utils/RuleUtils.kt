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
package com.github.cereda.arara.utils

import com.github.cereda.arara.controller.LanguageController
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.model.Messages
import com.github.cereda.arara.model.Rule
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.MarkedYAMLException
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.io.FileReader
import java.util.*

/**
 * Implements rule utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object RuleUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    /**
     * Parses the provided file, checks the identifier and returns a rule
     * representation.
     *
     * @param file       The rule file.
     * @param identifier The directive identifier.
     * @return The rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun parseRule(file: File, identifier: String): Rule {
        val representer = Representer()
        representer.addClassTag(Rule::class.java, Tag("!config"))
        val yaml = Yaml(Constructor(Rule::class.java), representer)
        val rule: Rule
        try {
            rule = yaml.loadAs(FileReader(file), Rule::class.java)
        } catch (yamlException: MarkedYAMLException) {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_PARSERULE_INVALID_YAML
                    ),
                    yamlException
            )
        } catch (exception: Exception) {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_PARSERULE_GENERIC_ERROR
                    )
            )
        }

        validateHeader(rule, identifier)
        validateBody(rule)
        return rule
    }

    /**
     * Validates the rule header according to the directive identifier.
     *
     * @param rule       The rule object.
     * @param identifier The directive identifier.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun validateHeader(rule: Rule, identifier: String) {
        if (rule.identifier != null) {
            if (rule.identifier != identifier) {
                throw AraraException(CommonUtils.ruleErrorHeader +
                        messages.getMessage(
                                Messages.ERROR_VALIDATEHEADER_WRONG_IDENTIFIER,
                                rule.identifier!!,
                                identifier))
            }
        } else {
            throw AraraException(CommonUtils.ruleErrorHeader +
                    messages.getMessage(Messages.ERROR_VALIDATEHEADER_NULL_ID))
        }
        if (rule.name == null) {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_VALIDATEHEADER_NULL_NAME
                    )
            )
        }
    }

    /**
     * Validates the rule body.
     *
     * @param rule The rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun validateBody(rule: Rule) {
        if (rule.commands == null) {
            throw AraraException(CommonUtils.ruleErrorHeader +
                    messages.getMessage(
                            Messages.ERROR_VALIDATEBODY_NULL_COMMANDS_LIST))
        } else {
            if (rule.commands.any { it.command == null }) {
                throw AraraException(CommonUtils.ruleErrorHeader +
                        messages.getMessage(
                                Messages.ERROR_VALIDATEBODY_NULL_COMMAND))
            }
        }
        if (rule.arguments == null) {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_VALIDATEBODY_ARGUMENTS_LIST
                    )
            )
        } else {
            val keywords = arrayOf("file", "files", "reference")

            val arguments = mutableListOf<String>()
            for (argument in rule.arguments) {
                if (argument.identifier != null) {
                    if (argument.flag != null || argument.default != null) {
                        arguments.add(argument.identifier!!)
                    } else {
                        throw AraraException(
                                CommonUtils.ruleErrorHeader + messages.getMessage(
                                        Messages.ERROR_VALIDATEBODY_MISSING_KEYS
                                )
                        )
                    }
                } else {
                    throw AraraException(
                            CommonUtils.ruleErrorHeader + messages.getMessage(
                                    Messages.ERROR_VALIDATEBODY_NULL_ARGUMENT_ID
                            )
                    )
                }
            }

            for (keyword in keywords) {
                if (arguments.contains(keyword)) {
                    throw AraraException(
                            CommonUtils.ruleErrorHeader + messages.getMessage(
                                    Messages.ERROR_VALIDATEBODY_ARGUMENT_ID_IS_RESERVED,
                                    keyword
                            )
                    )
                }
            }

            val expected = arguments.size
            val found = HashSet(arguments).size
            if (expected != found) {
                throw AraraException(
                        CommonUtils.ruleErrorHeader + messages.getMessage(
                                Messages.ERROR_VALIDATEBODY_DUPLICATE_ARGUMENT_IDENTIFIERS
                        )
                )
            }
        }
    }
}
