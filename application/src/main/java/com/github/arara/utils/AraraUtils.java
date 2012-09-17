/**
 * \cond LICENSE
 * Arara -- the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda
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
 * \endcond
 *
 * AraraUtils: This class contains some helper methods used for general purpose.
 */
// package definition
package com.github.arara.utils;

// needed imports
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.WordUtils;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/**
 * Contains some helper methods used for general purpose.
 * 
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class AraraUtils {
    
    // the localization class
    final static AraraLocalization localization = AraraLocalization.getInstance();

    /**
     * Removes the reserved keyword in the strings to be analyzed by the MVEL
     * template engine. The keyword is set in AraraConstants.
     * 
     * @param line The line to be analyzed.
     * @return The new line without the keyword.
     */
    public static String removeKeyword(String line) {
        
        // set a new pattern and matcher
        Pattern pattern = Pattern.compile(AraraConstants.RULEPATTERN);
        Matcher matcher = pattern.matcher(line);
        
        // if the keyword is found
        if (matcher.find()) {
            
            // remove the keyword
            line = (line.substring(matcher.end(), line.length())).trim();
        }
        
        // and return it
        return line;
    }

    /**
     * Prints the arara ASCII header.
     */
    public static void printHeader() {

        // print the ASCII art
        System.out.println("  __ _ _ __ __ _ _ __ __ _ ");
        System.out.println(" / _` | '__/ _` | '__/ _` |");
        System.out.println("| (_| | | | (_| | | | (_| |");
        System.out.println(" \\__,_|_|  \\__,_|_|  \\__,_|\n");

    }

    /**
     * Wraps the string to several lines, according to the wrap length set in
     * the AraraConstants class.
     * 
     * @param message The message to be wrapped.
     * @return A new message.
     */
    public static String wrap(String message) {
        
        // create a new tokenizer in order to allow line breaks, so
        // every line is processed individually
        StringTokenizer tokens = new StringTokenizer(message, "\n", true);
        String result = "";
        
        // iterate through tokens
        while (tokens.hasMoreTokens()) {
            
            // wrap line according to the wrap length
            result = result.concat(WordUtils.wrap(tokens.nextToken(), AraraConstants.WRAPLENGTH));
        }
        
        // return the result
        return result;
    }

    /**
     * Gets the variable reference from a RuntimeException object.
     * @param runtimeException The RuntimeException object.
     * 
     * @return The variable reference name.
     */
    public static String getVariableFromException(RuntimeException runtimeException) {
    
        // set pattern and matcher
        Pattern pattern = Pattern.compile(AraraConstants.VAREXCEPTIONPATTERN);
        Matcher matcher = pattern.matcher(runtimeException.getMessage());
        
        // if the variable is found
        if (matcher.find()) {
            
            // check if it's a valid reference
            if (matcher.groupCount() != 0) {
                
                // return the variable name
                return matcher.group(1);
            }
            
        } else {
                        
            // maybe it's a method call, let's investigate
            pattern = Pattern.compile(AraraConstants.METHODEXCEPTIONPATTERN);
            matcher = pattern.matcher(runtimeException.getMessage());
            
            if (matcher.find()) {
                
                // check if it's a valid reference
                if (matcher.groupCount() > 1) {

                    // return the method name
                    return matcher.group(2);
                }
                
            }
            
        }
        
        // nothing is found, so return an unknown reference
        return "<unknown>";
    }
    
    /**
     * Extracts information from a MarkedYAMLException object.
     * 
     * @param yamlException The exception object.
     * @return A string containing all the available information from the
     * exception object.
     */
    public static String extractInformationFromYAMLException(MarkedYAMLException yamlException) {
        StringBuilder stringBuilder = new StringBuilder();
        if (yamlException.getContext() != null) {
            stringBuilder.append(localization.getMessage("YamlError_Context", yamlException.getContext())).append("\n");
        }
        if (yamlException.getProblem() != null) {
            stringBuilder.append(localization.getMessage("YamlError_Problem", yamlException.getProblem())).append("\n");
        }
        if (yamlException.getProblemMark() != null) {
            stringBuilder.append(localization.getMessage("YamlError_ErrorLocation", yamlException.getProblemMark().getLine(), yamlException.getProblemMark().getColumn())).append("\n");
        }
        if (yamlException.getProblemMark().get_snippet() != null) {
            stringBuilder.append(yamlException.getProblemMark().get_snippet());
        }
        return stringBuilder.toString();
    }
}
