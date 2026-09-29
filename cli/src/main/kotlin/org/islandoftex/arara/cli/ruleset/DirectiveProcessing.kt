// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.charleskorn.kaml.Yaml
import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.cli.utils.Display
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveFetchingHooks
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.lua.rules.LuaInterpolation
import org.islandoftex.arara.mvel.utils.MvelState
import org.mvel2.templates.TemplateRuntime

/**
 * Implements directive utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object DirectiveProcessing {
    private val logger = KotlinLogging.logger { }

    /**
     * The [Directives] core object can't process directives without an
     * implementation. This hook collection ensures we can use it.
     */
    fun initializeDirectiveCore() {
        Directives.hooks = DirectiveFetchingHooks(
            processPotentialDirective = { line, directive ->
                directive.trim().also {
                    logger.info {
                        "I found a potential pattern in line $line: $it"
                    }
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

        val map = runCatching {
            // the directive parameters are parsed into the YAML node
            // model and converted into the plain Kotlin values the
            // interpreter expects; null values are rejected, preserving
            // the contract that directive parameters must carry a value
            YamlNodeConversion.toParameterMap(
                Yaml.default.parseToYamlNode(text)
            )
        }.getOrElse {
            throw AraraException(
                LanguageController.messages.errorValidateYamlException(
                    numbers.joinToString(", ", "(", ")")
                ),
                it
            )
        }

        return if ("options" in map.keys && LinearExecutor.executionOptions
            .executionMode != ExecutionMode.SAFE_RUN
        ) {
            // perform directive interpolation by applying the methods of
            // the selected engine to the directive arguments; the value
            // keeps its shape, so a scalar option stays a scalar (the
            // safe run preserves it verbatim as well)
            map.plus(
                "options" to when (val options = map["options"]) {
                    is List<*> -> options.map { value ->
                        interpolate(value.toString())
                    }
                    else -> interpolate(options.toString())
                }
            )
        } else map
    }

    /**
     * Interpolate the provided value with the tag machinery of the
     * selected rule engine.
     */
    @Throws(AraraException::class)
    private fun interpolate(value: String): Any =
        when (RuleEngine.current) {
            RuleEngine.LUA -> LuaInterpolation.interpolate(value)
            RuleEngine.YAML ->
                runCatching {
                    TemplateRuntime.eval(value, MvelState.directiveMethods)
                }.getOrElse {
                    throw AraraException(
                        LanguageController.messages
                            .errorExtractorInterpolationFailure,
                        it
                    )
                }
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
                    LanguageController.messages
                        .errorValidateReferenceIsReserved(
                            directive.lineNumbers
                                .joinToString(", ", "(", ")")
                        )
                )

            if (parameters.containsKey("files")) {
                Directives.replicateDirective(
                    parameters.getValue("files"),
                    parameters.minus("files"),
                    directive
                )
            } else {
                listOf(
                    DirectiveImpl(
                        directive.identifier,
                        parameters.plus("reference" to file.path),
                        directive.conditional,
                        directive.lineNumbers
                    )
                )
            }
        }.also { result ->
            logger.info {
                """
                    All directives were validated. We are good to go.
                    ${Display.displayOutputSeparator(
                    "DIRECTIVES"
                )}
                    ${result.joinToString("\n")}
                    ${Display.displaySeparator()}
                """.trimIndent()
            }
        }
}
