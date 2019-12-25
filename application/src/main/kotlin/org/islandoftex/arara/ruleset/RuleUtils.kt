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
package org.islandoftex.arara.ruleset

import com.charleskorn.kaml.Yaml
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.model.AraraException
import org.islandoftex.arara.utils.CommonUtils
import java.io.File

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
        val rule = file.runCatching {
            val text = readText()
            if (!text.startsWith("!config"))
                throw Exception("Rule should start with !config")
            Yaml.default.parse(Rule.serializer(), text)
        }.getOrElse {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_PARSERULE_GENERIC_ERROR
                    ), it)
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
    @Suppress("ThrowsCount")
    private fun validateHeader(rule: Rule, identifier: String) {
        if (rule.identifier != Rule.INVALID_RULE_IDENTIFIER) {
            if (rule.identifier != identifier) {
                throw AraraException(CommonUtils.ruleErrorHeader +
                        messages.getMessage(
                                Messages.ERROR_VALIDATEHEADER_WRONG_IDENTIFIER,
                                rule.identifier,
                                identifier))
            }
        } else {
            throw AraraException(CommonUtils.ruleErrorHeader +
                    messages.getMessage(Messages.ERROR_VALIDATEHEADER_NULL_ID))
        }
        if (rule.name == Rule.INVALID_RULE_NAME) {
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
    @Suppress("ThrowsCount")
    private fun validateBody(rule: Rule) {
        if (rule.commands.any { it.command == null }) {
            throw AraraException(CommonUtils.ruleErrorHeader +
                    messages.getMessage(
                            Messages.ERROR_VALIDATEBODY_NULL_COMMAND))
        }

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

        arguments.intersect(listOf("files", "reference")).forEach {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_VALIDATEBODY_ARGUMENT_ID_IS_RESERVED,
                            it
                    )
            )
        }

        val expected = arguments.size
        val found = arguments.toSet().size
        if (expected != found) {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_VALIDATEBODY_DUPLICATE_ARGUMENT_IDENTIFIERS
                    )
            )
        }
    }
}
