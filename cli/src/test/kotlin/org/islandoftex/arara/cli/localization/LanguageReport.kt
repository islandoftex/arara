// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.localization

import java.io.File

/**
 * Implements the language report model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
data class LanguageReport(
    /**
     * The file reference.
     */
    val reference: File,
    /**
     * Total of checked lines.
     */
    val total: Int,
    /**
     * List of problematic lines and their corresponding error types.
     */
    val lines: Map<Int, Char>
) {
    /**
     * Language coverage of the file.
     */
    val coverage: Float
        get() = if (lines.isEmpty()) {
            100f
        } else {
            (1f - lines.size.toFloat() / total) * 100f
        }

    companion object {
        /**
         * Analyzes the list of lines.
         *
         * @param file The file to read.
         * @return The language report.
         */
        internal fun analyze(file: File): LanguageReport {
            // holds the current line number
            var number = 1
            // holds the number of checked lines
            var checked = 0

            // flag that holds the
            // current analysis
            var check: Int

            val reportLines = mutableMapOf<Int, Char>()

            // check every line of the language file
            file.forEachLine { line ->
                // let's only analyze lines
                // that are not comments
                if (!line.trim().startsWith("#")) {
                    // increment the checked
                    // line counter
                    checked++

                    // line is a parametrized message
                    check = if (line.contains("{0}")) {
                        // check the corresponding pattern
                        checkParametrizedMessage(line)
                    } else {
                        // check the corresponding pattern
                        checkMessage(line)
                    }

                    // we found an error,
                    // report it
                    if (check != 0) {
                        // add line and error type to the report
                        reportLines[number] = if (check == 1) 'P' else 'S'
                    }
                }

                // let's move to the next line
                number++
            }

            // return the language report
            return LanguageReport(
                    reference = file,
                    total = checked,
                    lines = reportLines)
        }

        /**
         * Checks if the provided message follows the simple format.
         *
         * @param text Message.
         * @return An integer value.
         */
        private fun checkMessage(text: String): Int {
            var i = 0
            var c: Char
            for (element in text) {
                c = element
                i = if (c == '\'') {
                    if (i == 1) {
                        return 2
                    } else {
                        1
                    }
                } else {
                    0
                }
            }
            return 0
        }

        /**
         * Checks if the provided message follows the parametrized format.
         *
         * @param text Message.
         * @return An integer value.
         */
        private fun checkParametrizedMessage(text: String): Int {
            var i = 0
            var c: Char
            for (element in text) {
                c = element
                if (c == '\'') {
                    i += 1
                } else {
                    if (i != 0) {
                        if (i != 2) {
                            return 1
                        } else {
                            i = 0
                        }
                    }
                }
            }
            return 0
        }
    }
}
