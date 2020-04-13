// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveFetchingHooks
import org.islandoftex.arara.core.rules.Directives
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

    init {
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
     * Extracts a list of directives from a list of strings.
     *
     * @param lines List of strings.
     * @param parseOnlyHeader Whether to parse only the header.
     * @param fileType The file type of the file to investigate.
     * @return A list of directives.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    @Suppress("MagicNumber")
    fun extractDirectives(
        lines: List<String>,
        parseOnlyHeader: Boolean,
        fileType: FileType
    ): List<Directive> = process(
            Directives.extractDirectives(lines, parseOnlyHeader, fileType)
    )

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

        /*
         * Before using Jackson, there has been a dedicated directive resolver
         * which instructed SnakeYAML to do the following:
         *
         * addImplicitResolver(Tag.MERGE, MERGE, "<")
         * addImplicitResolver(Tag.NULL, NULL, "~nN\u0000")
         * addImplicitResolver(Tag.NULL, EMPTY, null)
         *
         * This has been removed.
         */
        return ObjectMapper(YAMLFactory()).registerKotlinModule().runCatching {
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
    }

    /**
     * Validates the list of directives, returning a new list.
     *
     * @param directives The list of directives.
     * @return A new list of directives.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun process(directives: List<Directive>): List<Directive> {
        val result = mutableListOf<Directive>()
        directives.forEach { directive ->
            val parameters = directive.parameters

            if (parameters.containsKey("reference"))
                throw AraraException(
                        LanguageController.messages.ERROR_VALIDATE_REFERENCE_IS_RESERVED
                                .format(directive.lineNumbers.joinToString(", ", "(", ")"))
                )

            if (parameters.containsKey("files")) {
                result.addAll(Directives.replicateDirective(
                        parameters.getValue("files"),
                        parameters.minus("files"),
                        directive
                ))
            } else {
                result.add(DirectiveImpl(
                        directive.identifier,
                        parameters.plus("reference" to
                                Arara.config[AraraSpec.Execution.reference].path.toFile()),
                        directive.conditional,
                        directive.lineNumbers
                ))
            }
        }

        logger.info(LanguageController.messages.LOG_INFO_VALIDATED_DIRECTIVES)
        logger.info(DisplayUtils.displayOutputSeparator(
                LanguageController.messages.LOG_INFO_DIRECTIVES_BLOCK))
        result.forEach { logger.info(it.toString()) }
        logger.info(DisplayUtils.displaySeparator())

        return result
    }
}
