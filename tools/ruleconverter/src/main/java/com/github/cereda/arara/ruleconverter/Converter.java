/**
 * Rule converter, a tool for Arara
 * Copyright (c) 2018, Paulo Roberto Massa Cereda
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
package com.github.cereda.arara.ruleconverter;

import com.github.cereda.arara.ruleconverter.model.ORule;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Main class.
 * 
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Converter {

    /**
     * Main method.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        
        // draw the application logo
        // in the command line
        System.out.println(logo());

        // let us try the conversion of
        // an older format to a new one
        try {

            // the application requires
            // exactly one argument
            if (args.length != 1) {
                
                // throw an exception about the
                // wrong number of arguments
                throw new Exception("This tool expects the YAML rule from "
                        + "previous versions of arara. Please, provide "
                        + "a proper YAML file containing the old rule as "
                        + "a parameter and try again. I will do my best "
                        + "to convert the rule to the new version 4.0 "
                        + "format.");
            }

            // get the input file
            // from the argument
            File input = new File(args[0]);

            // the input file
            // must exist!
            if (!input.exists()) {
                
                // thow an exception about
                // a missing input file
                throw new Exception("I am sorry to inform you that the "
                        + "provided file does not exist! Please, make sure "
                        + "to provide a valid YAML file containing the old "
                        + "rule as a parameter and try again.");
            }

            // check if the file is
            // actually a file
            if (!input.isFile()) {
                
                // throw an exception about
                // the file being a directory
                throw new Exception("I am sorry to inform you that the "
                        + "provided reference is actually a directory! I "
                        + "am prepared to handle one file at a time. "
                        + "Please, make sure to provide a valid YAML file "
                        + "containing the old rule as a parameter and "
                        + "try again.");
            }

            // check if the provided file
            // has the correct YAML extension
            if (!input.getName().endsWith(".yaml")) {
                
                // throw an exception about
                // the wrong file extension
                throw new Exception("I am sorry to inform you that the "
                        + "provided file does not have a proper '.yaml' "
                        + "extension. Please, make sure your YAML file "
                        + "has the correct extension and try again.");
            }

            // get the rule in
            // the old format
            ORule rule = Utils.read(input);

            // the rule must
            // have an identifier
            if (rule.getIdentifier() == null) {
                
                // throw an exception about
                // a missing identifier
                throw new Exception("I noticed the provided rule does not "
                        + "have an identifier! Are you sure this YAML file "
                        + "is a valid arara rule? Please, refer to the "
                        + "user manual and fix the rule. Sadly, I cannot "
                        + "help you on this issue.");
            }

            // the rule must
            // have a name
            if (rule.getName() == null) {
                
                // throw an exception about
                // a missing name
                throw new Exception("I noticed the provided rule does not "
                        + "have a name! Are you sure this YAML file is a "
                        + "valid arara rule? Please, refer to the user "
                        + "manual and fix the rule. Sadly, I cannot help "
                        + "you on this issue.");
            }

            // the rule must have a
            // list of arguments
            if (rule.getArguments() == null) {
                
                // throw an exception about the
                // missing list of arguments
                throw new Exception("I noticed the provided rule does not "
                        + "have a proper argument list! Are you sure this "
                        + "YAML file is a valid arara rule? Please, refer "
                        + "to the user manual and fix the rule. Sadly, I "
                        + "cannot help you on this issue.");
            }

            // the rule must have either one
            // command or a list of commands
            if ((rule.getCommand() == null) && (rule.getCommands() == null)) {
                
                // throw an exception about the
                // missing command or commands
                throw new Exception("I noticed the provided rule does not "
                        + "have either a command or a list of commands! Are "
                        + "you sure this YAML file is a valid arara rule? "
                        + "Please, refer to the user manual and fix the "
                        + "rule. Sadly, I cannot help you on this issue.");
            }

            // get the output reference
            // from the input file
            File output = Utils.getOutput(input);
            
            // notify the user about the
            // beginning of the conversion
            System.out.println(wrap("The provided YAML rule looks OK. I will "
                    + "try my best to convert it to the new version 4.0 "
                    + "format adopted by arara. The new rule name will be "
                    + "written in the same directory of the original one and "
                    + "will have a '_v4' suffix to it. Keep in mind that the "
                    + "base name must match the identifier!"));

            // write the new rule based
            // on the new format
            Utils.write(Utils.update(rule), output);

            // cool, the conversion
            // was successful!
            System.out.println();
            System.out.println(StringUtils.rightPad("YAY! ", 60, "-"));
            System.out.println(wrap("Good news, everybody! The provided YAML "
                    + "rule was updated successfully to the new version 4.0 "
                    + "format of arara! Of course, there are no guarantees "
                    + "this new rule will work out of the box, so fingers "
                    + "crossed! Take a closer look at the manual and update "
                    + "your rule to use the new enhancements of arara. Have "
                    + "a great time!"));
            
        } catch (Exception exception) {
            
            // something wrong has happened,
            // so let us print the message
            System.out.println(StringUtils.rightPad("OH NO! ", 60, "-"));
            System.err.println(wrap(exception.getMessage()));
        }
    }

    /**
     * Wraps the text.
     * @param text The text.
     * @return The wrapped text.
     */
    private static String wrap(String text) {
        return WordUtils.wrap(text, 60, "\n", true);
    }

    /**
     * Draws the application logo.
     * @return A string containing the application logo.
     */
    private static String logo() {
        StringBuilder sb = new StringBuilder();
        sb.append("         _                                _\n");
        sb.append(" ___ _ _| |___    ___ ___ ___ _ _ ___ ___| |_ ___ ___\n");
        sb.append("|  _| | | | -_|  |  _| . |   | | | -_|  _|  _| -_|  _|\n");
        sb.append("|_| |___|_|___|  |___|___|_|_|\\_/|___|_| |_| |___|_|\n");  
        return sb.toString();
    }

}
