// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

/**
 * Implements a directive assembler in order to help build a directive from a
 * list of strings.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class DirectiveAssembler {
    /**
     * This variable holds a list of line numbers indicating which lines
     * composed the resulting potential directive.
     */
    private val lineNumbers = mutableListOf<Int>()

    /**
     * This variable holds the textual representation of the directive
     */
    var text: String = ""
        get() = field.trim()
        private set

    /**
     * Checks if an append operation is allowed.
     */
    val isAppendAllowed: Boolean
        get() = lineNumbers.isNotEmpty()

    /**
     * Adds a line number to the assembler.
     *
     * @param line An integer representing the line number.
     */
    fun addLineNumber(line: Int) = lineNumbers.add(line)

    /**
     * Appends the provided line to the assembler text.
     * @param line The provided line.
     */
    fun appendLine(line: String) {
        text = text + " " + line.trim()
    }

    /**
     * Gets the list of line numbers.
     * @return The list of line numbers.
     */
    fun getLineNumbers(): List<Int> = lineNumbers
}
