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
package com.github.cereda.arara.ruleset

import com.github.cereda.arara.Arara
import com.github.cereda.arara.configuration.AraraSpec
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.utils.CommonUtils
import com.github.cereda.arara.utils.DisplayUtils
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.MarkedYAMLException
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.util.*
import java.util.regex.Pattern

/**
 * Implements directive utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object DirectiveUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(DirectiveUtils::class.java)

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
        val validLinePattern = Pattern.compile(validLineRegex)
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
                ?: throw AraraException(messages.getMessage(
                        Messages.ERROR_VALIDATE_NO_DIRECTIVES_FOUND))

        val assemblers = mutableListOf<DirectiveAssembler>()
        var assembler = DirectiveAssembler()
        val linebreakPattern = Arara.config[AraraSpec.Directive
                .linebreakPattern].toPattern()
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
        val regex = Arara.config[AraraSpec.Directive.directivePattern]
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(assembler.getText())
        if (matcher.find()) {
            val directive = Directive(
                    identifier = matcher.group(1)!!,
                    parameters = getParameters(matcher.group(3),
                            assembler.getLineNumbers()),
                    conditional = Conditional(
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
    private fun getType(text: String?): Conditional.ConditionalType {
        return when (text) {
            null -> Conditional.ConditionalType.NONE
            "if" -> Conditional.ConditionalType.IF
            "while" -> Conditional.ConditionalType.WHILE
            "until" -> Conditional.ConditionalType.UNTIL
            else -> Conditional.ConditionalType.UNLESS
        }
    }

    /**
     * Gets the parameters from the input string.
     *
     * @param text    The input string.
     * @param numbers The list of line numbers.
     * @return A map containing the directive parameters.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    // TODO: loadAs might throw another (undocumented) exception
    private fun getParameters(text: String?,
                              numbers: List<Int>): Map<String, Any> {
        if (text == null)
            return mapOf()

        return Yaml(Constructor(),
                Representer(),
                DumperOptions(),
                DirectiveResolver()).runCatching {
            loadAs(text, HashMap::class.java).mapKeys { it.toString() }
        }.getOrElse {
            throw if (it is MarkedYAMLException)
                AraraException(messages.getMessage(
                        Messages.ERROR_VALIDATE_YAML_EXCEPTION,
                        "(" + numbers.joinToString(", ") + ")"),
                        it)
            else
                it
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
    private fun replicateDirective(holder: Any, parameters: Map<String, Any>,
                                   directive: Directive): List<Directive> {
        return if (holder is List<*>) {
            // we received a file list, so we map that list to files
            holder.filterIsInstance<Any>()
                    .asSequence()
                    .map { File(it.toString()) }
                    .map(CommonUtils::getCanonicalFile)
                    // and because we want directives, we replicate our
                    // directive to be applied to that file
                    .map { reference ->
                        directive.copy(parameters = parameters
                                .plus("reference" to reference))
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
                throw AraraException(messages.getMessage(
                        Messages.ERROR_VALIDATE_REFERENCE_IS_RESERVED,
                        "(" + directive.lineNumbers.joinToString(", ") + ")"))

            if (parameters.containsKey("files")) {
                result.addAll(replicateDirective(parameters.getValue("files"),
                        parameters.minus("files"), directive))
            } else {
                result.add(directive.copy(parameters = parameters
                        .plus("reference" to
                                Arara.config[AraraSpec.Execution.reference])))
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
     * @param line    Provided line.
     * @return Logical value indicating if the provided line contains the
     * corresponding pattern, based on the file type, or an empty line.
     */
    private fun checkLinePattern(pattern: Pattern, line: String): Boolean {
        return line.isBlank() || pattern.matcher(line).find()
    }
}
