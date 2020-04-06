// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.util.regex.Pattern
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.filehandling.FileHandlingUtils
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.slf4j.LoggerFactory

/**
 * Implements directive utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@OptIn(kotlinx.serialization.ImplicitReflectionSerializer::class)
object DirectiveUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(DirectiveUtils::class.java)

    private const val directivestart = """^\s*(\w+)\s*(:\s*(\{.*\})\s*)?"""
    private const val pattern = """(\s+(if|while|until|unless)\s+(\S.*))?$"""
    // pattern to match directives against
    private val directivePattern = (directivestart + pattern).toPattern()

    // what to expect after a line break in a directive
    private val linebreakPattern = "^\\s*-->\\s(.*)$".toPattern()

    /**
     * This function filters the lines of a file to identify the potential
     * directives.
     *
     * @param lines The lines of the file.
     * @return A map containing the line number and the line's content.
     */
    private fun getPotentialDirectiveLines(lines: List<String>):
            Map<Int, String> {
        val header = Arara.config[AraraSpec.Execution.onlyHeader]
        val validLineRegex = Arara.config[AraraSpec.Execution.filePattern]
        val validLinePattern = validLineRegex.toPattern()
        val validLineStartPattern = (validLineRegex + Arara.config[AraraSpec
                .Application.namePattern]).toPattern()
        val map = mutableMapOf<Int, String>()
        for ((i, text) in lines.withIndex()) {
            val validLineMatcher = validLineStartPattern.matcher(text)
            if (validLineMatcher.find()) {
                val line = text.substring(validLineMatcher.end())
                map[i + 1] = line

                logger.info(messages.getMessage(
                        Messages.LOG_INFO_POTENTIAL_PATTERN_FOUND,
                        i + 1, line.trim()))
            } else if (header && !checkLinePattern(validLinePattern, text)) {
                // if we should only look within the file's header and reached
                // a point where the line pattern does not match anymore, we
                // assume we have left the header and break
                break
            }
        }
        return map
    }

    /**
     * Extracts a list of directives from a list of strings.
     *
     * @param lines List of strings.
     * @return A list of directives.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    @Suppress("MagicNumber")
    fun extractDirectives(lines: List<String>): List<Directive> {
        val pairs = getPotentialDirectiveLines(lines)
                .takeIf { it.isNotEmpty() }
                ?: throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_VALIDATE_NO_DIRECTIVES_FOUND
                        )
                )

        val assemblers = mutableListOf<DirectiveAssembler>()
        var assembler = DirectiveAssembler()
        for ((lineno, content) in pairs) {
            val linebreakMatcher = linebreakPattern.matcher(content)
            if (linebreakMatcher.find()) {
                if (!assembler.isAppendAllowed) {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATE_ORPHAN_LINEBREAK,
                                    lineno
                            )
                    )
                } else {
                    assembler.addLineNumber(lineno)
                    assembler.appendLine(linebreakMatcher.group(1))
                }
            } else {
                if (assembler.isAppendAllowed) {
                    assemblers.add(assembler)
                }
                assembler = DirectiveAssembler()
                assembler.addLineNumber(lineno)
                assembler.appendLine(content)
            }
        }
        if (assembler.isAppendAllowed) {
            assemblers.add(assembler)
        }

        return assemblers.map { generateDirective(it) }
    }

    /**
     * Generates a directive from a directive assembler.
     *
     * @param assembler The directive assembler.
     * @return The corresponding directive.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    @Suppress("MagicNumber")
    fun generateDirective(assembler: DirectiveAssembler): Directive {
        val matcher = directivePattern.matcher(assembler.getText())
        if (matcher.find()) {
            val directive = DirectiveImpl(
                    identifier = matcher.group(1)!!,
                    parameters = getParameters(matcher.group(3),
                            assembler.getLineNumbers()),
                    conditional = DirectiveConditionalImpl(
                            type = getType(matcher.group(5)),
                            condition = matcher.group(6) ?: ""
                    ),
                    lineNumbers = assembler.getLineNumbers()
            )

            logger.info(messages.getMessage(
                    Messages.LOG_INFO_POTENTIAL_DIRECTIVE_FOUND, directive))

            return directive
        } else {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATE_INVALID_DIRECTIVE_FORMAT,
                            "(" + assembler.getLineNumbers()
                                    .joinToString(", ") + ")"
                    )
            )
        }
    }

    /**
     * Gets the conditional type based on the input string.
     *
     * @param text The input string.
     * @return The conditional type.
     */
    private fun getType(text: String?): DirectiveConditionalType {
        return when (text) {
            null -> DirectiveConditionalType.NONE
            "if" -> DirectiveConditionalType.IF
            "while" -> DirectiveConditionalType.WHILE
            "until" -> DirectiveConditionalType.UNTIL
            else -> DirectiveConditionalType.UNLESS
        }
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

        /* Before using kotlinx.serialization, there has been a dedicated
         * directive resolver which instructed SnakeYAML to do the following:
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
                    messages.getMessage(
                            Messages.ERROR_VALIDATE_YAML_EXCEPTION,
                            "(" + numbers.joinToString(", ") + ")"
                    ),
                    it
            )
        }
    }

    /**
     * Replicate a directive for given files.
     *
     * @param holder The list of files.
     * @param parameters The parameters for the directive.
     * @param directive The directive to clone.
     * @return List of cloned directives.
     * @throws AraraException If there is an error validating the [holder]
     *   object.
     */
    @Throws(AraraException::class)
    private fun replicateDirective(
        holder: Any,
        parameters: Map<String, Any>,
        directive: Directive
    ): List<Directive> {
        return if (holder is List<*>) {
            // we received a file list, so we map that list to files
            holder.filterIsInstance<Any>()
                    .asSequence()
                    .map { File(it.toString()) }
                    .map(FileHandlingUtils::getCanonicalFile)
                    // and because we want directives, we replicate our
                    // directive to be applied to that file
                    .map { reference ->
                        DirectiveImpl(
                                directive.identifier,
                                parameters.plus("reference" to reference),
                                directive.conditional,
                                directive.lineNumbers
                        )
                    }
                    .toList()
                    // we take the result if and only if we have at least one
                    // file and we did not filter out any invalid argument
                    .takeIf { it.isNotEmpty() && holder.size == it.size }
            // TODO: check exception according to condition
                    ?: throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATE_EMPTY_FILES_LIST,
                                    "(" + directive.lineNumbers
                                            .joinToString(", ") + ")"
                            )
                    )
        } else {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATE_FILES_IS_NOT_A_LIST,
                            "(" + directive.lineNumbers.joinToString(", ") + ")"
                    )
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
                        messages.getMessage(
                                Messages.ERROR_VALIDATE_REFERENCE_IS_RESERVED,
                                "(" + directive.lineNumbers.joinToString(", ") + ")"
                        )
                )

            if (parameters.containsKey("files")) {
                result.addAll(replicateDirective(parameters.getValue("files"),
                        parameters.minus("files"), directive))
            } else {
                result.add(DirectiveImpl(
                        directive.identifier,
                        parameters.plus("reference" to
                                Arara.config[AraraSpec.Execution.reference]),
                        directive.conditional,
                        directive.lineNumbers
                ))
            }
        }

        logger.info(messages.getMessage(
                Messages.LOG_INFO_VALIDATED_DIRECTIVES))
        logger.info(DisplayUtils.displayOutputSeparator(
                messages.getMessage(Messages.LOG_INFO_DIRECTIVES_BLOCK)))
        result.forEach { logger.info(it.toString()) }
        logger.info(DisplayUtils.displaySeparator())

        return result
    }

    /**
     * Checks if the provided line contains the corresponding pattern, based on
     * the file type, or an empty line.
     *
     * @param pattern Pattern to be matched, based on the file type.
     * @param line Provided line.
     * @return Logical value indicating if the provided line contains the
     * corresponding pattern, based on the file type, or an empty line.
     */
    private fun checkLinePattern(pattern: Pattern, line: String): Boolean {
        return line.isBlank() || pattern.matcher(line).find()
    }
}
