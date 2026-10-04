// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements directive extraction: the line patterns, the parsing
 * hooks and the replication the file list asks for.
 */
object Directives {
    /**
     * The hooks the directive extraction consults, so the execution
     * flow can observe every potential directive line.
     */
    var hooks = DirectiveFetchingHooks()

    /**
     * The head of a directive line: the identifier and its
     * optional parameter block.
     */
    private const val DIRECTIVE_START = """^\s*(\w+)\s*(:\s*(\{.*\})\s*)?"""
    /**
     * The optional conditional tail of a directive line.
     */
    private const val PATTERN = """(\s+(if|while|until|unless)\s+(\S.*))?$"""

    /**
     * The pattern a directive line must match.
     */
    private val directivePattern = (DIRECTIVE_START + PATTERN).toRegex()

    /**
     * The marker of the arara directive part, looked for within
     * a line of the file type's pattern.
     */
    private const val NAME_PATTERN = "arara:\\s"

    /**
     * The pattern a linebreak continuation must match.
     */
    private val linebreakPattern = "^\\s*-->\\s(.*)$".toRegex()

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
        val validLinePattern = pattern.toRegex()
        val validLineStartPattern = (pattern + NAME_PATTERN).toRegex()
        val map = mutableMapOf<Int, String>()
        for ((i, text) in lines.withIndex()) {
            val validLineMatch = validLineStartPattern.find(text)
            if (validLineMatch != null) {
                val line = text.substring(validLineMatch.range.endInclusive + 1)
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
     * Extracts a list of directives from a list of strings. Might be empty.
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
            .takeIf { it.isNotEmpty() } ?: return emptyList()

        val assemblers = mutableListOf<DirectiveAssembler>()
        var assembler = DirectiveAssembler()
        for ((lineno, content) in pairs) {
            val linebreakMatch = linebreakPattern.find(content)
            if (linebreakMatch != null) {
                if (!assembler.isAppendAllowed) {
                    throw AraraException(
                        LanguageController
                            .messages.errorValidateOrphanLinebreak(lineno)
                    )
                } else {
                    assembler.addLineNumber(lineno)
                    assembler.appendLine(linebreakMatch.groupValues[1])
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
    internal fun generateDirective(assembler: DirectiveAssembler): Directive =
        directivePattern.find(assembler.text)?.let { match ->
            // NOTE: in match.groupValues all capture group have a string value;
            //       if the group was optional and empty, there's an empty
            //       string indicating this behavior
            hooks.buildDirectiveRaw(
                // identifier
                match.groupValues[1],
                // parameters
                match.groupValues[3].takeIf { it.isNotEmpty() },
                // conditional
                DirectiveConditional(
                    type = match.groupValues[5]
                        .takeIf { it.isNotEmpty() }
                        .toDirectiveConditional(),
                    condition = match.groupValues[6]
                ),
                // line numbers
                assembler.lineNumbers
            )
        } ?: throw AraraException(
            LanguageController.messages.errorValidateInvalidDirectiveFormat(
                assembler.lineNumbers
                    .joinToString(", ", "(", ")")
            )
        )

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
                .map { MPPPath(it.toString()).normalize() }
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
                // file, and we did not filter out any invalid argument
                .takeIf { it.isNotEmpty() && holder.size == it.size }
                ?: throw AraraException(
                    LanguageController.messages
                        .errorValidateEmptyFilesList(
                            directive.lineNumbers
                                .joinToString(", ", "(", ")")
                        )
                )
        } else {
            throw AraraException(
                LanguageController.messages.errorValidateFilesIsNotAList(
                    directive.lineNumbers
                        .joinToString(", ", "(", ")")
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
    private fun checkLinePattern(pattern: Regex, line: String): Boolean {
        return line.isBlank() || pattern.containsMatchIn(line)
    }
}

/**
 * Gets the conditional type based on an input string.
 *
 * @return The conditional type.
 */
private fun String?.toDirectiveConditional() = when (this) {
    null -> DirectiveConditionalType.NONE
    "if" -> DirectiveConditionalType.IF
    "while" -> DirectiveConditionalType.WHILE
    "until" -> DirectiveConditionalType.UNTIL
    "unless" -> DirectiveConditionalType.UNLESS
    // the directive pattern constrains the captured token to the four
    // conditional keywords, so anything else is a programming error
    else -> error(
        "Unknown conditional type '$this'."
    )
}
