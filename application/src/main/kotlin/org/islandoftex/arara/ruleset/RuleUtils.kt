// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.ruleset

import com.charleskorn.kaml.Yaml
import java.io.File
import org.islandoftex.arara.AraraException
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.utils.CommonUtils

/**
 * Implements rule utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
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
     * @param file The rule file.
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
                    ), it
            )
        }

        validateHeader(rule, identifier)
        validateBody(rule)
        return rule
    }

    /**
     * Validates the rule header according to the directive identifier.
     *
     * @param rule The rule object.
     * @param identifier The directive identifier.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    @Suppress("ThrowsCount")
    private fun validateHeader(rule: Rule, identifier: String) {
        if (rule.identifier != Rule.INVALID_RULE_IDENTIFIER) {
            if (rule.identifier != identifier) {
                throw AraraException(
                        CommonUtils.ruleErrorHeader +
                                messages.getMessage(
                                        Messages.ERROR_VALIDATEHEADER_WRONG_IDENTIFIER,
                                        rule.identifier,
                                        identifier
                                )
                )
            }
        } else {
            throw AraraException(
                    CommonUtils.ruleErrorHeader +
                            messages.getMessage(Messages.ERROR_VALIDATEHEADER_NULL_ID)
            )
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
            throw AraraException(
                    CommonUtils.ruleErrorHeader +
                            messages.getMessage(
                                    Messages.ERROR_VALIDATEBODY_NULL_COMMAND
                            )
            )
        }

        val arguments = mutableListOf<String>()
        for (argument in rule.arguments) {
            if (argument.identifier.isNotBlank()) {
                if (argument.flag != null || argument.defaultValue != null) {
                    arguments.add(argument.identifier)
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
