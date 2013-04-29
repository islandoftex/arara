package com.github.arara.utils;

// needed imports
import com.github.arara.model.AraraConditionalType;
import java.io.File;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.WordUtils;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/**
 * Contains some helper methods used for general purpose.
 *
 * @since 3.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraUtils {
    
    // the localization class
    /** Constant <code>localization</code> */
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
     *
     * @param runtimeException The RuntimeException object.
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
    
    /**
     * Checks for a valid directive.
     *
     * @param line The current line.
     * @param regex The regular expression pattern.
     * @return A boolean indicating if the pattern was found.
     */
    public static boolean checkForValidDirective(String line, String regex) {
        
        // create a pattern and a matcher
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        
        // return the lookup result
        return matcher.find();
    }
    
    /**
     * Extracts the directive from the current line.
     *
     * @param line The current line.
     * @param regex The regular expression pattern.
     * @return An array of strings containing each block of the pattern.
     */
    public static String[] extractDirective(String line, String regex) {
        
        // create a pattern and a matcher
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        
        // directive found
        if (matcher.find()) {
            
            // get the number of blocks and allocate the
            // size for the result array
            String[] result = new String[matcher.groupCount()];
            
            // iterate through the groups
            for (int i = 0; i < matcher.groupCount(); i++) {
                
                // add current group to the result
                result[i] = matcher.group(i + 1);
            }
            
            // return the array
            return result;
        }
        else {
            
            // nothing was found, return
            // a null value
            return null;
        }
    }
    
    /**
     * Searches for the configuration file in the array.
     *
     * @param files An array of strings representing a list of locations.
     * @return A file reference to the first occurrence found in the list.
     */
    public static File getConfigurationFile(String[] files) {
        
        // create reference
        File file = null;
        
        // for every element in the array
        for (int i = 0; i < files.length; i++) {
            
            // get the reference and
            // check if it exists
            file = new File(files[i]);
            if (file.exists()) {
                return file;
            }
        }
        
        // simply return the last element
        // in the list
        return file;
    }
    
    /**
     * Gets the conditional type based on the corresponding keyword.
     *
     * @param keyword A string containing the conditional keyword.
     * @return A conditional type object.
     */
    public static AraraConditionalType getConditionalType(String keyword) {
        
        // we have an IF
        if (keyword.equals("if")) {
            return AraraConditionalType.IF;
        }
        else {
            
            // we have a WHILE
            if (keyword.equals("while")) {
                return AraraConditionalType.WHILE;
            }
            else {
                
                // we have UNTIL
                if (keyword.equals("until")) {
                    return AraraConditionalType.UNTIL;
                }
                else {
                    
                    // we have nothing!
                    return AraraConditionalType.NONE;
                }
            }
        }
    }
    
    /**
     * Checks if the line has an arara trigger.
     *
     * @param line The current line.
     * @return A boolean flag indicating if the line has an arara trigger.
     */
    public static boolean checkForAraraTrigger(String line) {
        
        // create a pattern and a matcher
        Pattern pattern = Pattern.compile(AraraConstants.ARARATRIGGER);
        Matcher matcher = pattern.matcher(line);
        
        // return the matcher result
        return matcher.find();
    }
    
    /**
     * Extracts the arara trigger.
     *
     * @param line The current line.
     * @return An array of strings containing both the action and parameters.
     */
    public static String[] extractAraraTrigger(String line) {
        
        // create a pattern and a matcher
        Pattern pattern = Pattern.compile(AraraConstants.ARARATRIGGER);
        Matcher matcher = pattern.matcher(line);
        
        // directive found
        if (matcher.find()) {
            
            // the result array
            String[] result;
            
            // if we have no parameters
            if (matcher.group(2) == null) {
                
                // set only the action
                result = new String[1];
                result[0] = matcher.group(1);
            }
            else {
                
                // we have parameters, then
                // let's set both
                result = new String[2];
                result[0] = matcher.group(1);
                result[1] = matcher.group(2);
            }
            
            // return the array
            return result;
        }
        else {
            
            // nothing was found, return
            // a null value
            return null;
        }
    }
    
    /**
     * Checks if we should halt the execution from a trigger.
     *
     * @param line The current line.
     * @return A boolean indicating if we should halt the execution.
     */
    public static boolean haltFromAraraTrigger(String line) {
        
        // if we have an arara trigger
        if (checkForAraraTrigger(line)) {
            
            // get the result
            String[] result = extractAraraTrigger(line);
            
            // check if the action
            // is a halt operation
            if (result[0].equals("halt")) {
                
                // return true
                return true;
            }
            else {
                
                // other operation,
                // return false
                return false;
            }
        }
        else {
            
            // nothing was found,
            // simply return
            return false;
        }
    }
    
}
