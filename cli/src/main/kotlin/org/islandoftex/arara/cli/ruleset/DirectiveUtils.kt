// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveFetchingHooks
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.mvel.utils.MvelState
import org.mvel2.templates.TemplateRuntime
import org.slf4j.LoggerFactory

/**
 * Implements directive utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object DirectiveUtils {
    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(DirectiveUtils::class.java)

    /**
     * The [Directives] core object can't process directives without an
     * implementation. This hook collection ensures we can use it.
     */
    fun initializeDirectiveCore() {
        Directives.hooks = DirectiveFetchingHooks(
                processPotentialDirective = { line, directive ->
                    directive.trim().also {
                        logger.info(LanguageController.messages
                                .LOG_INFO_POTENTIAL_PATTERN_FOUND
                                .format(line, it)
                        )
                    }
                },
                buildDirectiveRaw = { id, parameters, conditional, lines ->
                    val parameterMap = getParameters(parameters, lines)
                    DirectiveImpl(
                            identifier = id,
                            parameters = parameterMap,
                            conditional = conditional,
                            lineNumbers = lines
                    )
                },
                buildDirective = { id, parameters, conditional, lines ->
                    DirectiveImpl(
                            identifier = id,
                            parameters = parameters,
                            conditional = conditional,
                            lineNumbers = lines
                    )
                }
        )
    }

    /**
     * Gets the parameters from the input string.
     *
     * @param text The input string.
     * @param numbers The list of line numbers.
     * @return A map containing the directive parameters.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun getParameters(
        text: String?,
        numbers: List<Int>
    ): Map<String, Any> {
        if (text == null)
            return mapOf()

        val map = ObjectMapper(YAMLFactory()).registerKotlinModule().runCatching {
            readValue<Map<String, Any>>(text)
        }.getOrElse {
            throw AraraException(
                    LanguageController.messages.ERROR_VALIDATE_YAML_EXCEPTION
                            .format(
                                    numbers.joinToString(", ", "(", ")")
                            ),
                    it
            )
        }

        return if ("options" in map.keys && LinearExecutor.executionOptions
                        .executionMode != ExecutionMode.SAFE_RUN) {
            // perform directive interpolation by applying MVEL methods to the
            // directive arguments
            map.plus("options" to (map["options"]?.takeIf { it is List<*> }
                    ?.let { list ->
                        (list as List<*>).map { it.toString() }
                    }
                    ?.map { value ->
                        kotlin.runCatching {
                            TemplateRuntime.eval(value, MvelState.directiveMethods)
                        }.getOrElse {
                            throw AraraException(LanguageController.messages
                                    .ERROR_EXTRACTOR_INTERPOLATION_FAILURE,
                                    it
                            )
                        }
                    } ?: emptyList()))
        } else map
    }

    /**
     * Validates the list of directives, returning a new list.
     *
     * @param file The file these directives have been extracted from.
     * @param directives The list of directives.
     * @return A new list of directives.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun process(
        file: ProjectFile,
        directives: List<Directive>
    ): List<Directive> =
        directives.flatMap { directive ->
            val parameters = directive.parameters

            if (parameters.containsKey("reference"))
                throw AraraException(
                        LanguageController.messages.ERROR_VALIDATE_REFERENCE_IS_RESERVED
                                .format(directive.lineNumbers.joinToString(", ", "(", ")"))
                )

            if (parameters.containsKey("files")) {
                Directives.replicateDirective(
                        parameters.getValue("files"),
                        parameters.minus("files"),
                        directive
                )
            } else {
                listOf(DirectiveImpl(
                        directive.identifier,
                        parameters.plus("reference" to file.path.toFile()),
                        directive.conditional,
                        directive.lineNumbers
                ))
            }
        }.also { result ->
            logger.info(LanguageController.messages.LOG_INFO_VALIDATED_DIRECTIVES)
            logger.info(DisplayUtils.displayOutputSeparator(
                    LanguageController.messages.LOG_INFO_DIRECTIVES_BLOCK))
            result.forEach { logger.info(it.toString()) }
            logger.info(DisplayUtils.displaySeparator())
        }
}
