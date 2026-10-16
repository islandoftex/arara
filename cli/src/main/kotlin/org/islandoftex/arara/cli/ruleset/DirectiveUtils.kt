// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.charleskorn.kaml.Yaml
import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.cli.utils.RunLog
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.DirectiveFetchingHooks
import org.islandoftex.arara.core.rules.Directives
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
object DirectiveUtils {
    private val logger = KotlinLogging.logger { }

    /**
     * The [Directives] core object can't process directives without an
     * implementation. This hook collection ensures we can use it.
     */
    fun initializeDirectiveCore(engine: RuleEngine = RuleEngine.YAML) {
        Directives.hooks = DirectiveFetchingHooks(
            processPotentialDirective = { line, directive ->
                directive.trim().also {
                    logger.info {
                        "Potential directive at line $line: $it"
                    }
                }
            },
            buildDirectiveRaw = { id, parameters, conditional, lines ->
                val parameterMap = getParameters(parameters, lines)
                DirectiveImpl(
                    identifier = id,
                    parameters = parameterMap,
                    conditional = conditional,
                    lineNumbers = lines,
                    engine = engine
                )
            },
            buildDirective = { id, parameters, conditional, lines ->
                DirectiveImpl(
                    identifier = id,
                    parameters = parameters,
                    conditional = conditional,
                    lineNumbers = lines,
                    engine = engine
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

        val map = kotlin.runCatching {
            YamlNodeConversion.toParameterMap(Yaml.default.parseToYamlNode(text))
        }.getOrElse {
            throw AraraException(
                LanguageController.messages.errorValidateYamlException(
                    numbers.joinToString(", ", "(", ")")
                ),
                it
            )
        }

        return map
    }

    internal fun interpolateOptions(value: Any, engine: RuleEngine): Any {
        fun interpolate(text: String): Any = if (engine == RuleEngine.LUA) {
            LuaInterpolation.interpolate(text)
        } else {
            if ("{{" in text) throw AraraException(
                LanguageController.messages.errorExtractorInterpolationFailure,
                IllegalArgumentException(LanguageController.messages.errorLuaTagsInYaml)
            )
            kotlin.runCatching { TemplateRuntime.eval(text, MvelState.directiveMethods) }.getOrElse {
                throw AraraException(LanguageController.messages.errorExtractorInterpolationFailure, it)
            }
        }
        return if (value is List<*>) value.map { interpolate(it.toString()) } else interpolate(value.toString())
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
        directives: List<Directive>,
        engine: RuleEngine = RuleEngine.YAML
    ): List<Directive> =
        directives.flatMap { directive ->
            val parameters = directive.parameters

            if (parameters.containsKey("reference"))
                throw AraraException(
                    LanguageController.messages.errorValidateReferenceIsReserved(directive.lineNumbers.joinToString(", ", "(", ")"))
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
                        directive.lineNumbers,
                        (directive as? DirectiveImpl)?.engine ?: engine
                    )
                )
            }
        }.also { result ->
            logger.info {
                buildString {
                    appendLine("All directives were validated.")
                    appendLine(RunLog.outputSeparator("DIRECTIVES"))
                    result.forEach { appendLine(it) }
                    append(RunLog.separator())
                }
            }
        }
}
