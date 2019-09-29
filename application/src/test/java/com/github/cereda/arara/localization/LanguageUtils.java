/*
 * Language checker, a tool for Arara
 * Copyright (c) 2015, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated documentation  files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, sublicense,  and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and this  permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT  SHALL THE AUTHORS OR COPYRIGHT HOLDERS  BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR  OTHER LIABILITY, WHETHER IN AN  ACTION OF CONTRACT,
 * TORT OR  OTHERWISE, ARISING  FROM, OUT  OF OR  IN CONNECTION  WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.cereda.arara.localization;

import java.util.List;

/**
 * Utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
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
