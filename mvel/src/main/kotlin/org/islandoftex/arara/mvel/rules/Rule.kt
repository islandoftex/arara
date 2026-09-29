// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements the rule model.
 *
 * @author Island of TeX
 */
@Serializable
data class Rule(
    /**
     * The identifier directives use to ask for this rule.
     */
    override val identifier: String,
    /**
     * The name the rule displays itself with.
     */
    @SerialName("name")
    override val displayName: String? = null,
    /**
     * The authors credited for the rule.
     */
    override val authors: List<String> = emptyList(),
    /**
     * The commands of the rule, in order.
     */
    override val commands: List<SerialRuleCommand> = emptyList(),
    /**
     * The arguments the rule declares.
     */
    override val arguments: List<RuleArgument> = emptyList()
) : Rule {
    /**
     * Holds the rule parser of the YAML format.
     */
    companion object {
        /**
         * Builds the rule error header, containing the identifier and the
         * location of the rule.
         */
        private fun ruleErrorHeader(identifier: String, path: String): String =
            LanguageController.messages.errorRuleIdentifierAndPath(
                identifier,
                path
            ) + " "

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
        fun parse(file: MPPPath, identifier: String): Rule {
            val errorHeader = ruleErrorHeader(
                identifier, file.normalize().parent.toString()
            )
            val rule = file.runCatching {
                val text = readText()
                if (!text.startsWith("!config"))
                    throw AraraException("Rule should start with !config")
                Yaml.default.decodeFromString(
                    org.islandoftex.arara.mvel.rules.Rule.serializer(), text
                )
            }.getOrElse {
                throw AraraException(
                    errorHeader + LanguageController
                        .messages.errorParseRuleGenericError,
                    it
                )
            }

            validateHeader(rule, identifier, errorHeader)
            validateBody(rule, errorHeader)
            return rule
        }

        /**
         * Validates the rule header according to the directive identifier.
         *
         * @param rule The rule object.
         * @param identifier The directive identifier.
         * @param errorHeader The rule error header for error messages.
         * @throws AraraException Something wrong happened, to be caught in the
         * higher levels.
         */
        @Throws(AraraException::class)
        @Suppress("ThrowsCount")
        private fun validateHeader(
            rule: Rule,
            identifier: String,
            errorHeader: String
        ) {
            if (rule.identifier != identifier) {
                throw AraraException(
                    errorHeader +
                        LanguageController.messages
                            .errorValidateHeaderWrongIdentifier(
                                rule.identifier,
                                identifier
                            )
                )
            }
            if (rule.displayName == null) {
                throw AraraException(
                    errorHeader + LanguageController
                        .messages.errorValidateHeaderNullName
                )
            }
        }

        /**
         * Validates the rule body.
         *
         * @param rule The rule object.
         * @param errorHeader The rule error header for error messages.
         * @throws AraraException Something wrong happened, to be caught in the
         * higher levels.
         */
        @Throws(AraraException::class)
        @Suppress("ThrowsCount")
        private fun validateBody(
            rule: org.islandoftex.arara.mvel.rules.Rule,
            errorHeader: String
        ) {
            if (rule.commands.any { it.commandString == null })
                throw AraraException(
                    errorHeader + LanguageController
                        .messages.errorValidateBodyNullCommand
                )

            // the validation compares the argument identifiers, not the
            // argument objects - comparing objects against identifier
            // strings would silently disable both checks
            val arguments = rule.arguments
                .filter { it.validate(errorHeader) }
                .map { it.identifier }

            arguments.intersect(listOf("files", "reference")).forEach {
                throw AraraException(
                    errorHeader + LanguageController.messages
                        .errorValidateBodyArgumentIdIsReserved(it)
                )
            }

            if (arguments.size != arguments.toSet().size) {
                throw AraraException(
                    errorHeader + LanguageController.messages
                        .errorValidateBodyDuplicateArgumentIdentifiers
                )
            }
        }
    }
}
