// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.core.localization.LanguageController

internal object RuleArguments {
    /**
     * Gets a set of strings containing unknown keys from a map and a list. It
     * is a set difference from the keys in the map and the entries in the list.
     *
     * @param parameters The map of parameters.
     * @param arguments The list of arguments.
     * @return A set of strings representing unknown keys from a map and a list.
     */
    private fun getUnknownKeys(
        parameters: Map<String, Any>,
        arguments: List<org.islandoftex.arara.api.rules.RuleArgument<*>>
    ): Set<String> = parameters.keys - arguments.map { it.identifier }

    /**
     * Parses the rule arguments against the provided directive.
     *
     * @param rule The rule object.
     * @param directive The directive.
     * @return A map containing all arguments resolved according to the
     * directive parameters.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun parseArguments(rule: Rule, directive: Directive):
        Map<String, Any> {
        val unknown = getUnknownKeys(directive.parameters, rule.arguments)
            .minus("reference")
        if (unknown.isNotEmpty())
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .errorInterpreterUnknownKeys(
                        unknown.joinToString(", ", "(", ")")
                    )
            )

        val context = mapOf(
            "parameters" to directive.parameters,
            "reference" to directive.parameters.getValue("reference")
        )

        return mapOf("reference" to directive.parameters.getValue("reference")) +
            rule.arguments.associate { definition ->
                // Script interpreters validate their own input types; the shared list erases that type parameter.
                @Suppress("UNCHECKED_CAST")
                val argument = definition as RuleArgument<Any?>
                argument.identifier to processArgument(
                    argument,
                    directive.parameters.containsKey(argument.identifier),
                    context,
                    directive.parameters[argument.identifier]
                )
            }
    }

    /**
     * Process a single argument and return the evaluated result.
     *
     * @param argument The argument to process.
     * @param idInDirectiveParams Whether the argument's identifier is
     *   contained in the directive's parameters field.
     * @param context The context for the evaluation.
     * @param parameterValue The value as passed by the user.
     * @return The result of the evaluation.
     * @throws AraraException The argument could not be processed.
     */
    @Throws(AraraException::class)
    private fun processArgument(
        argument: RuleArgument<Any?>,
        idInDirectiveParams: Boolean,
        context: Map<String, Any>,
        parameterValue: Any?
    ): List<*> {
        if (argument.isRequired && !idInDirectiveParams)
            throw AraraExceptionWithHeader(
                LanguageController.messages.errorInterpreterArgumentIsRequired(argument.identifier)
            )

        return if (idInDirectiveParams)
            argument.processor(parameterValue, context)
        else
            argument.defaultValue?.let { default ->
                argument.processor(default, context)
            } ?: emptyList<String>()
    }

}
