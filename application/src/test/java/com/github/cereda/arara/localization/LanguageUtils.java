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
package com.github.cereda.arara.localization;

import java.util.List;

/**
 * Utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 5.0
 * @since 5.0
 */
public class LanguageUtils {
    /**
     * Analyzes the list of lines.
     *
     * @param lines List of lines.
     * @return The language report.
     */
    static LanguageReport analyze(List<String> lines) {
        // temporary result
        LanguageReport report = new LanguageReport();

        // holds the current line number
        int number = 1;
        // holds the number of checked lines
        int checked = 0;

        // flag that holds the
        // current analysis
        int check;

        // check every line of the language file
        for (String line : lines) {
            // let's only analyze lines
            // that are not comments
            if (!line.trim().startsWith("#")) {
                // increment the checked
                // line counter
                checked++;

                // line is a parametrized message
                if (line.contains("{0}")) {
                    // check the corresponding pattern
                    check = checkParametrizedMessage(line);
                } else {
                    // check the corresponding pattern
                    check = checkMessage(line);
                }

                // we found an error,
                // report it
                if (check != 0) {
                    // add line and error type to the report
                    report.addLine(number, (check == 1 ? 'P' : 'S'));
                }
            }

            // let's move to the next line
            number++;
        }

        // set total of checked lines
        report.setTotal(checked);

        // return the language report
        return report;
    }

    /**
     * Checks if the provided message follows the simple format.
     *
     * @param text Message.
     * @return An integer value.
     */
    private static int checkMessage(String text) {
        int i = 0;
        char c;
        for (int j = 0; j < text.length(); j++) {
            c = text.charAt(j);
            if (c == '\'') {
                if (i == 1) {
                    return 2;
                } else {
                    i = 1;
                }
            } else {
                i = 0;
            }
        }
        return 0;
    }

    /**
     * Checks if the provided message follows the parametrized format.
     *
     * @param text Message.
     * @return An integer value.
     */
    private static int checkParametrizedMessage(String text) {
        int i = 0;
        char c;
        for (int j = 0; j < text.length(); j++) {
            c = text.charAt(j);
            if (c == '\'') {
                i = i + 1;
            } else {
                if (i != 0) {
                    if (i != 2) {
                        return 1;
                    } else {
                        i = 0;
                    }
                }
            }
        }
        return 0;
    }
}
