// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import java.nio.file.Paths
import java.util.regex.Pattern
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.localization.LanguageController

data class DirectiveFetchingHooks(
    /**
     * Whenever the parser found a potential directive, this will be
     * executed. You may manipulate the *textual* representation of the
     * directive here.
     */
    val processPotentialDirective: (Int, String) -> String = { _, s -> s },
    /**
     * Given the directive's characteristics identifier, parameters,
     * conditional and line numbers choose a directive implementation and
     * create a directive. Please make sure to resolve parameters.
     */
    val buildDirectiveRaw: (String, String?, DirectiveConditional, List<Int>) -> Directive = {
        _, _, _, _ -> TODO("directives can't be built by default")
    },
    /**
     * Given the directive's characteristics identifier, parameters,
     * conditional and line numbers choose a directive implementation and
     * create a directive. Please make sure to resolve parameters.
     */
    val buildDirective: (String, Map<String, Any>, DirectiveConditional, List<Int>) -> Directive = {
        _, _, _, _ -> TODO("directives can't be built by default")
    }
)

/**
 * Implements directive auxiliary methods.
 */
object Directives {
    var hooks = DirectiveFetchingHooks()

    private const val directivestart = """^\s*(\w+)\s*(:\s*(\{.*\})\s*)?"""
    private const val pattern = """(\s+(if|while|until|unless)\s+(\S.*))?$"""

    // pattern to match directives against
    private val directivePattern = (directivestart + pattern).toPattern()

    // math the arara part in `% arara: pdflatex`
    private const val namePattern = "arara:\\s"

    // what to expect after a line break in a directive
    private val linebreakPattern = "^\\s*-->\\s(.*)$".toPattern()

    /**
     * This function filters the lines of a file to identify the potential
     * directives.
     *
     * @param lines The lines of the file.
     * @param parseOnlyHeader Whether to parse only the header.
     * @param pattern The file type's pattern to use.
     * @return A map containing the line number and the line's content.
     */
    private fun getPotentialDirectiveLines(
        lines: List<String>,
        parseOnlyHeader: Boolean,
        pattern: String
    ): Map<Int, String> {
        val validLinePattern = pattern.toPattern()
        val validLineStartPattern = (pattern + namePattern).toPattern()
        val map = mutableMapOf<Int, String>()
        for ((i, text) in lines.withIndex()) {
            val validLineMatcher = validLineStartPattern.matcher(text)
            if (validLineMatcher.find()) {
                val line = text.substring(validLineMatcher.end())
                map[i + 1] = hooks.processPotentialDirective(i + 1, line)
            } else if (parseOnlyHeader && !checkLinePattern(validLinePattern, text)) {
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
    ): List<Directive> {
        val pairs = getPotentialDirectiveLines(lines, parseOnlyHeader, fileType.pattern)
                .takeIf { it.isNotEmpty() }
                ?: throw AraraException(
                        LanguageController.messages.ERROR_VALIDATE_NO_DIRECTIVES_FOUND
                )

        val assemblers = mutableListOf<DirectiveAssembler>()
        var assembler = DirectiveAssembler()
        for ((lineno, content) in pairs) {
            val linebreakMatcher = linebreakPattern.matcher(content)
            if (linebreakMatcher.find()) {
                if (!assembler.isAppendAllowed) {
                    throw AraraException(LanguageController
                            .messages.ERROR_VALIDATE_ORPHAN_LINEBREAK
                            .format(lineno))
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
    internal fun generateDirective(assembler: DirectiveAssembler): Directive {
        val matcher = directivePattern.matcher(assembler.text)
        if (matcher.find()) {
            return hooks.buildDirectiveRaw(
                    // identifier
                    matcher.group(1)!!,
                    // parameters
                    matcher.group(3),
                    // conditional
                    DirectiveConditional(
                            type = matcher.group(5).toDirectiveConditional(),
                            condition = matcher.group(6) ?: ""
                    ),
                    // line numbers
                    assembler.getLineNumbers()
            )
        } else {
            throw AraraException(
                    LanguageController.messages.ERROR_VALIDATE_INVALID_DIRECTIVE_FORMAT
                            .format(
                                    assembler.getLineNumbers().joinToString(", ", "(", ")")
                            )
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
    fun replicateDirective(
        holder: Any,
        parameters: Map<String, Any>,
        directive: Directive
    ): List<Directive> {
        return if (holder is List<*>) {
            // we received a file list, so we map that list to files
            holder.filterIsInstance<Any>()
                    .asSequence()
                    .map { Paths.get(it.toString()) }
                    .map(FileHandling::normalize)
                    .map { it.toFile() }
                    // and because we want directives, we replicate our
                    // directive to be applied to that file
                    .map { reference ->
                        hooks.buildDirective(
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
                    ?: throw AraraException(
                            LanguageController.messages.ERROR_VALIDATE_EMPTY_FILES_LIST
                                    .format(
                                            directive.lineNumbers.joinToString(", ", "(", ")")
                                    )
                    )
        } else {
            throw AraraException(
                    LanguageController.messages.ERROR_VALIDATE_FILES_IS_NOT_A_LIST
                            .format(
                                    directive.lineNumbers.joinToString(", ", "(", ")")
                            )
            )
        }
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

/**
 * Gets the conditional type based on an input string.
 *
 * @return The conditional type.
 */
internal fun String?.toDirectiveConditional() = when (this) {
    null -> DirectiveConditionalType.NONE
    "if" -> DirectiveConditionalType.IF
    "while" -> DirectiveConditionalType.WHILE
    "until" -> DirectiveConditionalType.UNTIL
    else -> DirectiveConditionalType.UNLESS
}
