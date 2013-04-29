package com.github.arara.utils;

// needed import
import java.io.File;
import org.apache.commons.cli.*;

/**
 * Implements a command line parser to provide better control of flags and files
 * to process.
 *
 * @since 1.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class CommandLineAnalyzer {

    // the file to process
    private String theFile;
    // the command line arguments
    private String[] theArgs;
    // the command line options
    private Options commandLineOptions;
    // the verbose option
    private boolean showVerboseOutput;
    // the execution timeout, in milliseconds
    private long executionTimeout;
    // the dry-run option
    private boolean dryRun;
    // the maximum number of loops
    private long maximumNumberOfLoops;
    // the configuration
    private ConfigurationLoader configuration;
    // the localization class
    /** Constant <code>localization</code> */
    final static AraraLocalization localization = AraraLocalization.getInstance();

    /**
     * Constructor.
     *
     * @param theArgs The command line arguments.
     * @param configuration The current configuration.
     */
    public CommandLineAnalyzer(String[] theArgs, ConfigurationLoader configuration) {

        // set the array
        this.theArgs = theArgs;

        // set the configuration
        this.configuration = configuration;

        // create new options
        commandLineOptions = new Options();

        // set the default value to the output
        showVerboseOutput = false;
        
        // set the default value for dry-run
        dryRun = false;

    }

    /**
     * Parses the command line arguments and provides feedback to the main
     * class.
     *
     * @return A boolean value if there is a file to be processed by arara.
     */
    public boolean parse() {

        // create the version option
        Option optVersion = new Option("V", "version", false, localization.getMessage("Help_Version"));

        // create the help option
        Option optHelp = new Option("h", "help", false, localization.getMessage("Help_Help"));

        // create the log option
        Option optLog = new Option("l", "log", false, localization.getMessage("Help_Log"));

        // create the verbose option
        Option optVerbose = new Option("v", "verbose", false, localization.getMessage("Help_Verbose"));

        // create the timeout option
        Option optTimeout = new Option("t", "timeout", true, localization.getMessage("Help_Timeout"));

        // create the language option
        Option optLanguage = new Option("L", "language", true, localization.getMessage("Help_Language"));
        
        // create the dry-run option
        Option optDryRun = new Option("n", "dry-run", false, localization.getMessage("Help_DryRun"));
        
        // create the option for the number of loops
        Option optMaxNumberLoops = new Option("m", "max-loops", false, localization.getMessage("Help_MaximumNumberOfLoops"));

        // add version
        commandLineOptions.addOption(optVersion);

        // add help
        commandLineOptions.addOption(optHelp);

        // add log
        commandLineOptions.addOption(optLog);

        // add verbose
        commandLineOptions.addOption(optVerbose);

        // add timeout
        commandLineOptions.addOption(optTimeout);

        // add language
        commandLineOptions.addOption(optLanguage);
        
        // add dry-run
        commandLineOptions.addOption(optDryRun);
        
        // add maximum number of loops
        commandLineOptions.addOption(optMaxNumberLoops);

        // create a new basic parser
        CommandLineParser parser = new BasicParser();

        // lets try to parse everthing
        try {

            // parse the arguments
            CommandLine line = parser.parse(commandLineOptions, theArgs);

            // if -L or --language found
            if (line.hasOption("language")) {
                
                // create a new language controler
                LanguageController language = new LanguageController();
                
                // if the attempt to set the language according to the command
                // line has failed
                if (!language.setLanguage(line.getOptionValue("language"))) {

                    // print the list of available languages
                    language.printLanguageHelp();
                    
                    // print the usage
                    printUsage();
                    
                    // and simply return
                    return false;
                }
                else {
                    
                    // new language, refresh the options
                    optVersion.setDescription(localization.getMessage("Help_Version"));
                    optHelp.setDescription(localization.getMessage("Help_Help"));
                    optLog.setDescription(localization.getMessage("Help_Log"));
                    optVerbose.setDescription(localization.getMessage("Help_Verbose"));
                    optTimeout.setDescription(localization.getMessage("Help_Timeout"));
                    optLanguage.setDescription(localization.getMessage("Help_Language"));
                    optDryRun.setDescription(localization.getMessage("Help_DryRun"));
                    optMaxNumberLoops.setDescription(localization.getMessage("Help_MaximumNumberOfLoops"));
                }
            }

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

                    // print special thanks
                    printSpecialThanks();

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

                        // if -t or --timeout found
                        if (line.hasOption("timeout")) {

                            // try to convert the argument to a number
                            try {

                                // parse the long value
                                executionTimeout = Long.parseLong(line.getOptionValue("timeout"));

                                // if it's not a valid number
                                if (executionTimeout <= 0) {

                                    // print version
                                    printVersion();

                                    // usage
                                    printUsage();

                                    // and return
                                    return false;

                                }
                            } catch (NumberFormatException numberFormatException) {

                                // we have a bad conversion

                                // print version
                                printVersion();

                                // usage
                                printUsage();

                                // and return
                                return false;
                            }
                        } else {

                            // fallback to the default value, that is,
                            // timeout is disabled
                            executionTimeout = 0;
                        }
                        
                        // if -m or --max-loops found
                        if (line.hasOption("max-loops")) {

                            // try to convert the argument to a number
                            try {

                                // parse the long value
                                maximumNumberOfLoops = Long.parseLong(line.getOptionValue("max-loops"));

                                // if it's not a valid number
                                if (maximumNumberOfLoops <= 0) {

                                    // print version
                                    printVersion();

                                    // usage
                                    printUsage();

                                    // and return
                                    return false;

                                }
                            } catch (NumberFormatException numberFormatException) {

                                // we have a bad conversion

                                // print version
                                printVersion();

                                // usage
                                printUsage();

                                // and return
                                return false;
                            }
                        } else {

                            // fallback to the default value
                            maximumNumberOfLoops = AraraConstants.MAXLOOPS;
                        }

                        // active logging
                        AraraLogging.enableLogging(line.hasOption("log"));

                        // set verbose flag
                        showVerboseOutput = line.hasOption("verbose");
                        
                        // if we have -n or --dry-run
                        if (line.hasOption("dry-run")) {
                            
                            // disable verbose, since
                            // it won't matter
                            showVerboseOutput = false;
                            
                            // set dry-run mode
                            dryRun = true;
                        }

                        // everything is fine, set
                        // the file
                        theFile = files[0];

                        // check if file exists
                        if (!checkFile(theFile, configuration.getValidExtensions())) {

                            // file not found, return false
                            return false;

                        }
                        
                        // and return
                        return true;
                    }
                }
            }

        } catch (ParseException parseException) {

            // something happened, in the last case print version
            printVersion();

            // and usage
            printUsage();

            // and simply return
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
        formatter.printHelp("arara [file [--dry-run] [--log] [--verbose] [--timeout N] [--max-loops N] [--language L] | --help | --version]", commandLineOptions);
    }

    /**
     * Prints the application version.
     */
    private void printVersion() {

        // print the version
        System.out.println("arara ".concat(AraraConstants.VERSION).concat(" - ").concat(localization.getMessage("Header_Slogan")));
        System.out.println("Copyright (c) ".concat(AraraConstants.COPYRIGHTYEAR).concat(", Paulo Roberto Massa Cereda"));
        System.out.println(localization.getMessage("Header_AllRightsReserved").concat("\n"));
    }

    /**
     * Getter for file obtained from the command line.
     *
     * @return The file.
     */
    public String getFile() {

        // return it
        return theFile;
    }

    /**
     * Checks if file is valid.
     *
     * @param filename The file name.
     * @param extensions A priority list of allowed extensions.
     * @return A boolean value to determine if the file is valid.
     */
    private boolean checkFile(String filename, String[] extensions) {

        // flag to indicate the file was found
        boolean foundFile = false;

        // iterate through all the extensions
        for (String currentExtension : extensions) {

            // check if the file has the current extension
            if (filename.toLowerCase().endsWith(currentExtension)) {

                // set flag
                foundFile = true;

                // set configuration
                configuration.setChosenFilePattern(currentExtension);

                // break iteration
                break;
            }
        }

        // create a new file status
        File fileStatus = null;

        // check if a reference was found
        if (foundFile) {

            // create new instance
            fileStatus = new File(filename);

            // check if exists
            if (fileStatus.exists()) {

                // add file
                theFile = filename;

                // found it!
                return true;
            } else {

                // print message about it
                System.out.println(localization.getMessage("Error_FileDoesNotExist", filename));

                // not found
                return false;
            }

        } else {

            // there's no extension, we will add them and see if something
            // is found
            for (String currentExtension : extensions) {

                // new file with extension
                fileStatus = new File(filename + currentExtension);

                // let's check it
                if (fileStatus.exists()) {

                    // add file
                    theFile = filename + currentExtension;

                    // add reference
                    configuration.setChosenFilePattern(currentExtension);

                    // found it!
                    return true;

                }

            }

            // print message about it
            System.out.println(AraraUtils.wrap(localization.getMessage("Error_FileDoesNotExistWithExtensionsList", filename, getExtensionsList(extensions))));

            // file not found
            return false;

        }

    }

    /**
     * Returns a string with the extensions list.
     *
     * @param extensions The extensions list.
     * @return A string with the extensions list.
     */
    private String getExtensionsList(String[] extensions) {

        // if it's an empty vector
        if (extensions.length == 0) {

            // return an empty string
            return "";

        } else {

            // get first element
            String result = "[".concat(extensions[0]);

            // iterate through the other values
            for (int i = 1; i < extensions.length; i++) {

                // add the current extension
                result = result.concat(", ").concat(extensions[i]);
            }

            // close the list
            result = result.concat("]");

            // return it
            return result;
        }
    }

    /**
     * Checks if the output must be verbose.
     *
     * @return A boolean value.
     */
    public boolean isVerbose() {

        // return it
        return showVerboseOutput;
    }

    /**
     * Checks if arara must run in dry-run mode.
     *
     * @return A boolean indicating if arara must run in dry-run mode.
     */
    public boolean isDryRun() {
        return dryRun;
    }
    
    /**
     * Getter for the execution timeout.
     *
     * @return The execution timeout.
     */
    public long getExecutionTimeout() {

        // return it
        return executionTimeout;
    }

    /**
     * Prints the special thanks message.
     */
    private void printSpecialThanks() {
        System.out.println(AraraUtils.wrap(localization.getMessage("Msg_SpecialThanks")));
    }
    
    /**
     * Getter for the maximum number of loops.
     *
     * @return A long value indicating the maximum number of loops.
     */
    public long getMaximumNumberOfLoops() {
        return maximumNumberOfLoops;
    }
    
}
