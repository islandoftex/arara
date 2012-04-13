/**
 * \cond LICENSE
 * ********************************************************************
 * This is a conditional block for preventing the DoxyGen documentation
 * tool to include this license header within the description of each
 * source code file. If you want to include this block, please define
 * the LICENSE parameter into the provided DoxyFile.
 * ********************************************************************
 *
 * Arara -- the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the project's author nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * ********************************************************************
 * End of the LICENSE conditional block
 * ********************************************************************
 * \endcond
 *
 * CommandLineAnalyzer.java: This class implements a command line parser to
 * provide better control of flags and files to process.
 */

// package definition
package com.github.arara.utils;

// needed import
import org.apache.commons.cli.*;

/**
 * Implements a command line parser to provide better control of flags
 * and files to process.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class CommandLineAnalyzer {

    // the version number
    private final String VERSION = "1.0";
    
    // the file to process
    private String theFile;
    
    // the command line arguments
    private String[] theArgs;
    
    // the command line options
    private Options commandLineOptions;

    /**
     * Constructor.
     * @param theArgs The command line arguments.
     */
    public CommandLineAnalyzer(String[] theArgs) {
        
        // set the array
        this.theArgs = theArgs;
        
        // create new options
        commandLineOptions = new Options();

    }

    /**
     * Parses the command line arguments and provides feedback to the main
     * class.
     * @return A boolean value if there is a file to be processed by Arara.
     */
    public boolean parse() {

        // create version option
        Option optVersion = new Option("v", "version", false, "print the application version");
        
        // create help option
        Option optHelp = new Option("h", "help", false, "print the help message");
        
        // create log option
        Option optLog = new Option("l", "log", false, "generate a log output");

        // add version
        commandLineOptions.addOption(optVersion);
        
        // add help
        commandLineOptions.addOption(optHelp);
        
        // add log
        commandLineOptions.addOption(optLog);
        
        // create a new basic parser
        CommandLineParser parser = new BasicParser();

        // print header
        printHeader();

        // lets try
        try {

            // parse the arguments
            CommandLine line = parser.parse(commandLineOptions, theArgs);

            // if -h or --help found
            if (line.hasOption("help")) {
                
                // print version
                printVersion();
                
                // and usage
                printUsage();
                
                // return
                return false;
                
            } else {
                
                // if -v or --version found
                if (line.hasOption("version")) {
                    
                    // print version
                    printVersion();
                    
                    // and return
                    return false;
                    
                } else {
                    
                    // get the list of files
                    String[] files = line.getArgs();
                    
                    // we only expect one file
                    if (files.length != 1) {
                        
                        // print version
                        printVersion();
                        
                        // usage
                        printUsage();
                        
                        // and return
                        return false;
                        
                    } else {
                        
                        // active logging
                        AraraLogging.enableLogging(line.hasOption("log"));
                        
                        // everything is fine, set
                        // the file
                        theFile = files[0];
                        
                        // if there's no extension
                        if (!theFile.toLowerCase().endsWith(".tex")) {
                            
                            // add it
                            theFile = theFile.concat(".tex");
                        }
                        
                        // and return
                        return true;
                    }
                }
            }

        } catch (ParseException exp) {
            
            // something happened,
            // print version
            printVersion();
            
            // and usage
            printUsage();
            
            // return
            return false;

        }
    }

    /**
     * Prints the usage message.
     */
    private void printUsage() {
        
        // new formatter
        HelpFormatter formatter = new HelpFormatter();
        
        // add the text and print
        formatter.printHelp("arara [ file [ --log ] | --help | --version ]\n", commandLineOptions);
    }

    /**
     * Prints the application header. It's simply an ASCII art of the word
     * "arara".
     */
    private void printHeader() {

        // print the ASCII art
        System.out.println("  __ _ _ __ __ _ _ __ __ _ ");
        System.out.println(" / _` | '__/ _` | '__/ _` |");
        System.out.println("| (_| | | | (_| | | | (_| |");
        System.out.println(" \\__,_|_|  \\__,_|_|  \\__,_|\n");

    }

    /**
     * Prints the application version.
     */
    private void printVersion() {
        
        // print the version
        System.out.println("Arara " + VERSION + " - The cool TeX automation tool");
        System.out.println("Copyright (c) 2012, Paulo Roberto Massa Cereda");
        System.out.println("All rights reserved.\n");
    }

    /**
     * Getter for file obtained from the command line.
     * @return The file.
     */
    public String getFile() {
        
        // return it
        return theFile;
    }
}
