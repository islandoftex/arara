// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import com.charleskorn.kaml.Yaml
import java.io.File
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements the rule model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
data class RuleImpl(
    override val identifier: String,
    @SerialName("name")
    override val displayName: String? = null,
    override val authors: List<String> = listOf(),
    override val commands: List<SerialRuleCommand> = listOf(),
    override val arguments: List<RuleArgument> = listOf()
) : Rule {
    companion object {
        private var ruleId: String? = null
        private var rulePath: String? = null

        /**
         * Gets the rule error header, containing the identifier and the path, if
         * any.
         */
        private val ruleErrorHeader: String
            get() {
                return ruleId?.let { id ->
                    rulePath?.let { path ->
                        LanguageController.messages.ERROR_RULE_IDENTIFIER_AND_PATH
                                .format(id, path) + " "
                    }
                } ?: ""
            }

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
        fun parse(file: File, identifier: String): Rule {
            ruleId = identifier
            rulePath = file.absolutePath
            val rule = file.runCatching {
                val text = readText()
                if (!text.startsWith("!config"))
                    throw Exception("Rule should start with !config")
                Yaml.default.parse(RuleImpl.serializer(), text)
            }.getOrElse {
                throw AraraException(ruleErrorHeader + LanguageController
                        .messages.ERROR_PARSERULE_GENERIC_ERROR, it)
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
            if (rule.identifier != identifier) {
                throw AraraException(ruleErrorHeader +
                        LanguageController.messages.ERROR_VALIDATEHEADER_WRONG_IDENTIFIER
                                .format(rule.identifier, identifier)
                )
            }
            if (rule.displayName == null) {
                throw AraraException(ruleErrorHeader + LanguageController
                        .messages.ERROR_VALIDATEHEADER_NULL_NAME)
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
        private fun validateBody(rule: RuleImpl) {
            if (rule.commands.any { it.commandString == null })
                throw AraraException(ruleErrorHeader + LanguageController
                        .messages.ERROR_VALIDATEBODY_NULL_COMMAND)

            val arguments = rule.arguments.map { argument ->
                if (argument.identifier.isNotBlank()) {
                    if (argument.flag != null || argument.defaultValue != null) {
                        argument.identifier
                    } else {
                        throw AraraException(ruleErrorHeader + LanguageController
                                .messages.ERROR_VALIDATEBODY_MISSING_KEYS)
                    }
                } else {
                    throw AraraException(ruleErrorHeader + LanguageController
                            .messages.ERROR_VALIDATEBODY_NULL_ARGUMENT_ID)
                }
            }

            arguments.intersect(listOf("files", "reference")).forEach {
                throw AraraException(ruleErrorHeader + LanguageController
                        .messages.ERROR_VALIDATEBODY_ARGUMENT_ID_IS_RESERVED
                        .format(it)
                )
            }

            if (arguments.size != arguments.toSet().size) {
                throw AraraException(ruleErrorHeader + LanguageController.messages
                        .ERROR_VALIDATEBODY_DUPLICATE_ARGUMENT_IDENTIFIERS)
            }
        }
    }
}
