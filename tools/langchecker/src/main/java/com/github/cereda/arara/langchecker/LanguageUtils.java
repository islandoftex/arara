/**
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
package com.github.cereda.arara.langchecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * Utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class LanguageUtils {
   
    /**
     * Finds all language files from the provided directory.
     * @param directory The provided directory.
     * @return A list of files matching the language file pattern.
     */
    public static List<File> findLanguages(File directory) {
        
        // we expect a directory, if not, halt
        if (!directory.isDirectory()) {

            // print error message
            System.err.println(
                    WordUtils.wrap(
                            "Fatal exception: The provided argument could not "
                          + "be resolved to a proper directory. I am afraid "
                          + "I won't be able to continue. Please make sure the "
                          + "argument resolves to a valid directory reference. "
                          + "The application will halt now.",
                            60
                    )
            );
            System.exit(1);
        }
        
        // we have a valid directory, in that case, return
        // a list of all files ending with .properties
        return new ArrayList(
                FileUtils.listFiles(
                        directory,
                        new SuffixFileFilter(".properties"),
                        FalseFileFilter.FALSE
                )
        );    
    }
    
    /**
     * Reads a list of files and returns a list of reports of each language.
     * @param files A list of files.
     * @return A list of reports of each language.
     */
    public static List<LanguageReport> readLanguages(List<File> files) {
        
        // check if the provided list is empty
        if (files.isEmpty()) {

            // print error message
            System.err.println(
                    WordUtils.wrap(
                            "Fatal exception: I could not find any languages "
                          + "in the provided directory. I am afraid I won't be "
                          + "be able to continue. Please make sure the "
                          + "provided directory contains at least one language "
                          + "to be analyzed. The application will halt now.",
                            60
                    )
            );
        }
        
        // the resulting list
        List<LanguageReport> reports = new ArrayList<>();
        
        // read each file of the list and extract
        // each task found
        for (File file : files) {
            
            try {
                
                // read each file into a list
                // of strings
                List<String> lines = FileUtils.readLines(file, "UTF-8");
                
                // get the line analysis
                LanguageReport report = analyze(lines);
                
                // set the file reference
                report.setReference(file);
                
                // add to the list
                reports.add(report);
                
            }
            catch (IOException exception) {

                // print error message
                System.err.println(
                        WordUtils.wrap(
                                "Fatal exception: an error was raised while "
                              + "trying to read one of the languages. Please "
                              + "make sure all languages in the provided "
                              + "directory have read permission. I won't be "
                              + "able to continue. The application will halt "
                              + "now.",
                                60
                        )
                );
                System.exit(1);
            }
            
        }

        // return the list of
        // analyzed languages
        return reports;
        
    }
    
    /**
     * Analyzes the list of lines.
     * @param lines List of lines.
     * @return The language report.
     */
    private static LanguageReport analyze(List<String> lines) {
        
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
                    
                }
                else {
                    
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
                }
                else {
                    i = 1;
                }
            }
            else {
                i = 0;
            }
        }
        return 0;
    }
    
    /**
     * Checks if the provided message follows the parametrized format.
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
            }
            else {
                if (i != 0) {
                    if ( i != 2) {
                        return 1;
                    }
                    else {
                       i = 0;
                    }
                }
            }
        }
        return 0;
    }
    
    /**
     * Prints the report for every language report.
     * @param languages The list of language reports.
     */
    public static void printReport(List<LanguageReport> languages) {
        
        // print header
        System.out.println(
                StringUtils.center(
                        " Language coverage report ",
                        60,
                        "-"
                ).concat("\n")
        );
        
        // let's sort our list of reports according to
        // each language coverage
        Collections.sort(languages, new Comparator<LanguageReport>() {

            @Override
            public int compare(LanguageReport t1, LanguageReport t2) {

                // get values of each language
                float a1 = t1.getCoverage();
                float a2 = t2.getCoverage();

                // they are equal, do nothing
                if (a1 == a2) {
                    return 0;
                }
                else {

                    // we want to sort in reverse order,
                    // so return a negative value here
                    if (a1 > a2) {
                        return -1;
                    }
                    else {

                        // return a positive value, since
                        // the first statement was false
                        return 1;
                    }
                }
            }
        });

        // list of languages to be fixed
        List<LanguageReport> fix = new ArrayList<>();
        
        // for each report, print
        // the corresponding entry
        for (LanguageReport language : languages) {

            // if there are problematic lines,
            // add the current language report
            if (!language.getLines().isEmpty()) {
                fix.add(language);
            }
            
            // build the beginning of the line
            String line = String.format(
                    "- %s ",
                    language.getReference().getName()
            );

            // build the coverage information
            String coverage = String.format(
                    " %2.2f%%",
                    language.getCoverage()
            );

            // generate the line by concatenating
            // the beginning and coverage
            line = line.concat(
                    StringUtils.repeat(
                            ".",
                            60 - line.length() - coverage.length()
                    )
            ).concat(coverage);

            // print the line
            System.out.println(line);
        }
        
        // we have some fixes to do
        if (!fix.isEmpty()) {
            
            // print header
            System.out.println();
            System.out.println(
                    StringUtils.center(
                            " Lines to fix ",
                            60,
                            "-"
                    ).concat("\n")
            );
            
            // print legend for a simple message
            System.out.println(StringUtils.center(
                    "S: Simple message, single quotes should not be doubled",
                    60)
            );
            
            // print legend for a parametrized
            System.out.println(StringUtils.center(
                    "P: Parametrized message, single quotes must be doubled",
                    60).concat("\n")
            );
            
            // print a line separator
            System.out.println(StringUtils.repeat("-", 60));
            
            // print each language and its
            // corresponding lines
            for (LanguageReport report : fix) {
                
                // build the beginning of the line
                String line = String.format(
                        "- %s ",
                        report.getReference().getName()
                );

                // build the first batch
                String batch = pump(report.getLines(), 2);

                // generate the line by concatenating
                // the beginning and batch
                line = line.concat(
                        StringUtils.repeat(
                                " ",
                                60 - line.length() - batch.length()
                        )
                ).concat(batch);

                // print the line
                System.out.println(line);
                
                // get the next batch, if any
                batch = pump(report.getLines(), 2);
                
                // repeat while there
                // are other batches
                while (!batch.isEmpty()) {
                    
                    // print current line
                    System.out.println(StringUtils.leftPad(batch, 60));
                    
                    // get the next batch and let
                    // the condition handle it
                    batch = pump(report.getLines(), 2);
                }
                
                // print a line separator
                System.out.println(StringUtils.repeat("-", 60));
            }
        }
        
    }

    /**
     * Prints the application logo.
     */
    public static void printLogo() {
        StringBuilder sb = new StringBuilder();
        sb.append(" _                    _           _           \n");
        sb.append("| |___ ___ ___    ___| |_ ___ ___| |_ ___ ___ \n");
        sb.append("| | .'|   | . |  |  _|   | -_|  _| '_| -_|  _|\n");
        sb.append("|_|__,|_|_|_  |  |___|_|_|___|___|_,_|___|_|  \n");
        sb.append("          |___|                               \n");
        System.out.println(sb.toString());
    }
    
    /**
     * Parses the command line arguments.
     * @param arguments An array containing the command line arguments.
     * @return A pair containing a boolean value and the file reference.
     */
    public static File parse(String[] arguments) {
       
        // command line options
        Options options = new Options();
        
        try {

            // create the parse
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, arguments);

            // check if there is a
            // file for reference
            if (line.getArgList().size() != 1) {
                throw new ParseException("Quack");
            }
            
            // return the file
            return new File(line.getArgList().get(0));
            
        }
        catch (ParseException exception) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("langchecker file", options);
            System.exit(1);
        }
        
        // never reach it
        return null;
        
    }
    
    /**
     * Pumps the elements to a string representation according to the provided
     * number of times.
     * @param lines The list of pairs.
     * @param number The number of times for elements to be pumped.
     * @return A string representation.
     */
    private static String pump(List<Pair<Integer,
            Character>> lines, int number) {
        
        // local counter, acting as a
        // safe check for elements
        int i = 0;
        
        // if there is nothing else to
        // be pumped, return an empty string
        if (lines.isEmpty()) {
            return "";
        }
        
        // at first, the result is an
        // empty string
        String result = "";
        
        // let's get the correct number of elements
        // or return the result as it is in case of
        // less elements than expected
        while ((i < number) && (!lines.isEmpty())) {
            
            // build the result, removing the first
            // element in the provided list
            result = result.concat(
                    String.valueOf(lines.remove(0))
            ).concat(" ");
            i++;
        }
        
        // return the trimmed element, so the trailing
        // space added in the previous loop is gone
        return result.trim();
    }
    
}
