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

import com.github.cereda.arara.configuration.Configuration
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.model.Interpreter
import com.github.cereda.arara.utils.CommonUtils
import com.github.cereda.arara.utils.DisplayUtils
import com.github.cereda.arara.utils.InterpreterUtils
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.MarkedYAMLException
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.util.*
import java.util.regex.Matcher
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
     * Extracts a list of directives from a list of strings.
     *
     * @param lines List of strings.
     * @return A list of directives.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun extractDirectives(lines: List<String>): List<Directive> {
        val header = Configuration["execution.header"] as Boolean
        var regex = Configuration["execution.file.pattern"] as String
        val linecheck = Pattern.compile(regex)
        regex += Configuration["application.pattern"] as String
        var pattern = Pattern.compile(regex)
        val pairs = mutableListOf<Pair<Int, String>>()
        var matcher: Matcher
        for (i in lines.indices) {
            matcher = pattern.matcher(lines[i])
            if (matcher.find()) {
                val line = lines[i].substring(
                        matcher.end())
                val pair = Pair(i + 1, line)
                pairs.add(pair)

                logger.info(
                        messages.getMessage(
                                Messages.LOG_INFO_POTENTIAL_PATTERN_FOUND,
                                i + 1,
                                line.trim()
                        )
                )
            } else {
                if (header) {
                    if (!checkLinePattern(linecheck, lines[i])) {
                        break
                    }
                }
            }
        }

        if (pairs.isEmpty()) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATE_NO_DIRECTIVES_FOUND
                    )
            )
        }

        val assemblers = mutableListOf<DirectiveAssembler>()
        var assembler = DirectiveAssembler()
        regex = Configuration["directives.linebreak.pattern"] as String
        pattern = Pattern.compile(regex)
        for ((first, second) in pairs) {
            matcher = pattern.matcher(second)
            if (matcher.find()) {
                if (!assembler.isAppendAllowed) {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATE_ORPHAN_LINEBREAK,
                                    first
                            )
                    )
                } else {
                    assembler.addLineNumber(first)
                    assembler.appendLine(matcher.group(1))
                }
            } else {
                if (assembler.isAppendAllowed) {
                    assemblers.add(assembler)
                }
                assembler = DirectiveAssembler()
                assembler.addLineNumber(first)
                assembler.appendLine(second)
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
    fun generateDirective(assembler: DirectiveAssembler): Directive {
        val regex = Configuration["directives.pattern"] as String
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(assembler.getText())
        if (matcher.find()) {
            val directive = Directive(
                    identifier = matcher.group(1),
                    parameters = getParameters(matcher.group(3), assembler.getLineNumbers()),
                    conditional = Conditional(
                            type = getType(matcher.group(5)),
                            condition = matcher.group(6) ?: ""
                    ),
                    lineNumbers = assembler.getLineNumbers()
            )

            logger.info(
                    messages.getMessage(
                            Messages.LOG_INFO_POTENTIAL_DIRECTIVE_FOUND,
                            directive
                    )
            )

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
    private fun getParameters(text: String?,
                              numbers: List<Int>): Map<String, Any> {
        if (text == null)
            return mapOf()

        val yaml = Yaml(
                Constructor(),
                Representer(),
                DumperOptions(),
                DirectiveResolver()
        )
        try {
            // TODO: check type checking
            return yaml.loadAs(text, HashMap::class.java)
                    .mapKeys { it.toString() }
        } catch (exception: MarkedYAMLException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATE_YAML_EXCEPTION,
                            "(" + numbers.joinToString(", ") + ")"
                    ),
                    exception
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
    fun validate(directives: List<Directive>): List<Directive> {
        val result = mutableListOf<Directive>()
        for (directive in directives) {
            val parameters = directive.parameters.toMutableMap()

            if (parameters.containsKey("file")) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_VALIDATE_FILE_IS_RESERVED,
                                "(" + directive.lineNumbers.joinToString(", ") + ")"
                        )
                )
            }

            if (parameters.containsKey("reference")) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_VALIDATE_REFERENCE_IS_RESERVED,
                                "(" + directive.lineNumbers.joinToString(", ") + ")"
                        )
                )
            }

            if (parameters.containsKey("files")) {
                val holder = parameters["files"]
                if (holder is List<*>) {
                    val files = holder as List<Any>
                    parameters.remove("files")
                    if (files.isEmpty()) {
                        throw AraraException(
                                messages.getMessage(
                                        Messages.ERROR_VALIDATE_EMPTY_FILES_LIST,
                                        "(" + directive.lineNumbers.joinToString(", ") + ")"
                                )
                        )
                    }
                    for (file in files) {
                        // copy the parameters map into a new one
                        val map = parameters.toMutableMap()
                        val representation = CommonUtils.getCanonicalFile(file.toString())

                        map["reference"] = representation
                        map["file"] = representation.name

                        result.add(Directive(
                                identifier = directive.identifier,
                                conditional = Conditional(
                                        type = directive.conditional.type,
                                        condition = directive.conditional.condition
                                ),
                                parameters = map,
                                lineNumbers = directive.lineNumbers
                        ))
                    }
                } else {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATE_FILES_IS_NOT_A_LIST,
                                    "(" + directive.lineNumbers.joinToString(", ") + ")"
                            )
                    )
                }
            } else {
                val representation = Configuration["execution.reference"] as File
                parameters["file"] = representation.name
                parameters["reference"] = representation
                result.add(directive.copy(parameters = parameters))
            }
        }

        logger.info(
                messages.getMessage(
                        Messages.LOG_INFO_VALIDATED_DIRECTIVES
                )
        )
        logger.info(DisplayUtils.displayOutputSeparator(
                messages.getMessage(
                        Messages.LOG_INFO_DIRECTIVES_BLOCK
                )
        ))
        for (directive in result) {
            logger.info(directive.toString())
        }

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
