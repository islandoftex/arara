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
package com.github.cereda.arara.localization

import java.io.File

/**
 * Implements the language report model.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 5.0
 * @since 5.0
 */
data class LanguageReport(
        // the file reference
        val reference: File,
        // total of checked lines
        val total: Int,
        // list of problematic lines and
        // their corresponding error types
        val lines: Map<Int, Char>) {

    // language coverage
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
         * @param lines List of lines.
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
