/**
 * Rule checker, a tool for Arara
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
package com.github.cereda.arara.rulechecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class RuleUtils {

    /**
     * Finds all rules from the provided directory.
     * @param directory The provided directory.
     * @return A list of files matching the rule pattern.
     */
    public static List<File> findRules(File directory) {

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
        // a list of all files ending with .yaml
        return new ArrayList(
                FileUtils.listFiles(
                        directory,
                        new SuffixFileFilter(".yaml"),
                        FalseFileFilter.FALSE
                )
        );
    }

    /**
     * Reads a list of files and returns a list of reports of each rule.
     * @param files A list of files.
     * @return A list of reports of each rule.
     */
    public static List<RuleReport> readRules(List<File> files) {

        // check if the provided list is empty
        if (files.isEmpty()) {

            // print error message
            System.err.println(
                    WordUtils.wrap(
                            "Fatal exception: I could not find any rules in "
                          + "the provided directory. I am afraid I won't be "
                          + "be able to continue. Please make sure the "
                          + "provided directory contains at least one rule to "
                          + "be analyzed. The application will halt now.",
                            60
                    )
            );
        }

        // the resulting list
        List<RuleReport> reports = new ArrayList<>();

        // read each file of the list and extract
        // each task found
        for (File file : files) {

            try {

                // read each file into a list
                // of strings
                List<String> lines = FileUtils.readLines(file, "UTF-8");

                // create a list of pair of tasks
                List<Pair<Boolean, String>> tasks = new ArrayList<>();

                // iterate through each line
                // of the current file
                for (String line : lines) {

                    // try to extract the task and
                    // add it to the list of tasks
                    update(line, tasks);

                }

                // create a new report
                RuleReport rule = new RuleReport(file);

                // add the list of tasks to
                // this report
                rule.setTasks(tasks);

                // and add the report
                // to the list
                reports.add(rule);

            }
            catch (IOException exception) {

                // print error message
                System.err.println(
                        WordUtils.wrap(
                                "Fatal exception: an error was raised while "
                              + "trying to read one of the rules. Please make "
                              + "sure all rules in the provided directory have "
                              + "read permission. I won't be able to continue. "
                              + "The application will halt now.",
                                60
                        )
                );
                System.exit(1);
            }
        }

        // return the list of
        // analyzed rules
        return reports;
    }

    /**
     * Checks if the current line is configured as a task.
     * @param line The current line.
     * @param list The list of tasks.
     */
    private static void update(String line, List<Pair<Boolean, String>> list) {

        // let's trim the string,
        // so no leading and trailing
        // spaces will bother us
        line = line.trim();

        // create the task pattern
        Pattern pattern = Pattern.compile(
                "^\\s*#\\s+\\[(\\s|x)\\]\\s+(\\S.*)$"
        );

        // apply the pattern to the
        // current line
        Matcher matcher = pattern.matcher(line);

        // we have a match
        if (matcher.find()) {

            // get the mark, that is,
            // if the task is completed
            // or not
            String marked = matcher.group(1).trim();

            // get the task description
            // itself
            String entry = matcher.group(2).trim();

            // create a new pair and add
            // the corresponding values
            Pair<Boolean, String> pair = new Pair<>();
            pair.setFirst(!marked.isEmpty());
            pair.setSecond(entry);

            // add this pair to the
            // list of pairs
            list.add(pair);
        }
    }

    /**
     * Prints the report for every rule report.
     * @param rules The list of rule report.
     */
    public static void printReport(List<RuleReport> rules) {

        // print header
        System.out.println(
                StringUtils.center(
                        " Task coverage report ",
                        60,
                        "-"
                ).concat("\n")
        );

        // let's sort our list of reports according to
        // each rule coverage
        Collections.sort(rules, new Comparator<RuleReport>() {

            @Override
            public int compare(RuleReport t1, RuleReport t2) {

                // get values of each rule
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

        // for each report, print
        // the corresponding entry
        for (RuleReport rule : rules) {

            // build the beginning of the line
            String line = String.format(
                    "- %s ",
                    rule.getReference().getName()
            );

            // build the coverage information
            String coverage = String.format(
                    " %2.2f%%",
                    rule.getCoverage()
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
    }

    /**
     * Checks if the provided line matches the task pattern.
     * @param line The current line.
     * @return A boolean value.
     */
    private static boolean match(String line) {

        // let's trim the string,
        // so no leading and trailing
        // spaces will bother us
        line = line.trim();

        // create the task pattern
        Pattern pattern = Pattern.compile(
                "^\\s*#\\s+\\[(\\s|x)\\]\\s+(\\S.*)$"
        );

        // apply the pattern to the
        // current line
        Matcher matcher = pattern.matcher(line);

        // return the matching result
        return matcher.find();

    }

    public static void updateRules(List<File> files) {

        // check if the provided list is empty
        if (files.isEmpty()) {

            // print error message
            System.err.println(
                    WordUtils.wrap(
                            "Fatal exception: I could not find any rules in "
                          + "the provided directory. I am afraid I won't be "
                          + "be able to continue. Please make sure the "
                          + "provided directory contains at least one rule to "
                          + "be analyzed. The application will halt now.",
                            60
                    )
            );
        }

        // print header
        System.out.println(
                StringUtils.center(
                        " Update report ",
                        60,
                        "-"
                ).concat("\n")
        );

        // read each file of the list and extract
        // each task found
        for (File file : files) {

            try {

                // read each file into a list
                // of strings
                List<String> lines = FileUtils.readLines(file, "UTF-8");

                // create the potential
                // output for the updated file
                List<String> output = new ArrayList<>();

                // iterate through each line
                for (String line : lines) {

                    // only add lines not matching
                    // the task pattern
                    if (!match(line)) {
                        output.add(line);
                    }

                }

                // result string
                String result;

                // if the sizes are the same,
                // nothing happened
                if (lines.size() == output.size()) {

                    // update status
                    result = " Unchanged";
                }
                else {

                    // update the file reference
                    FileUtils.writeLines(file, "UTF-8", output);

                    // update status
                    result = " Updated";
                }

                // build the beginning of the line
                String line = String.format(
                        "- %s ",
                        file.getName()
                );

                // generate the full entry
                line = line.concat(
                        StringUtils.repeat(
                                ".",
                                60 - line.length() - result.length()
                        )
                ).concat(result);

                // print entry
                System.out.println(line);

            }
            catch (IOException exception) {

                // print error message
                System.err.println(
                        WordUtils.wrap(
                                "Fatal exception: an error was raised while "
                              + "trying to read one of the rules. Please make "
                              + "sure all rules in the provided directory have "
                              + "read permission. I won't be able to continue. "
                              + "The application will halt now.",
                                60
                        )
                );
                System.exit(1);
            }
        }
    }

    /**
     * Parses the command line arguments.
     * @param arguments An array containing the command line arguments.
     * @return A pair containing a boolean value and the file reference.
     */
    public static Pair<Boolean, File> parse(String[] arguments) {

        // command line options
        Options options = new Options();
        options.addOption("r", "release", false, "update rules for release");

        try {

            // create the parse
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, arguments);

            // check if it is a release
            boolean release = line.hasOption("release");

            // check if there is a
            // file for reference
            if (line.getArgList().size() != 1) {
                throw new ParseException("Quack");
            }

            // provide the resulting pair
            Pair<Boolean, File> result = new Pair<>();
            result.setFirst(release);
            result.setSecond(new File(line.getArgList().get(0)));
            return result;

        }
        catch (ParseException exception) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("rulechecker [ --release ] file", options);
            System.exit(1);
        }

        // never reach it
        return null;
    }

    /**
     * Prints the application logo.
     */
    public static void printLogo() {
        StringBuilder sb = new StringBuilder();
        sb.append("         _            _           _           \n");
        sb.append(" ___ _ _| |___    ___| |_ ___ ___| |_ ___ ___ \n");
        sb.append("|  _| | | | -_|  |  _|   | -_|  _| '_| -_|  _|\n");
        sb.append("|_| |___|_|___|  |___|_|_|___|___|_,_|___|_|  \n");
        System.out.println(sb.toString());
    }

}
