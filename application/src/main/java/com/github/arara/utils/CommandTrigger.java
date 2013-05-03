package com.github.arara.utils;

// needed imports
import com.github.arara.exception.AraraException;
import com.github.arara.model.AraraCommand;
import com.github.arara.model.AraraConditionalType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an environment for running the arara commands.
 *
 * @since 3.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class CommandTrigger {

    // the logger
    /** Constant <code>logger</code> */
    final static Logger logger = LoggerFactory.getLogger(CommandTrigger.class);
    // the localization class
    /** Constant <code>localization</code> */
    final static AraraLocalization localization = AraraLocalization.getInstance();
    // the commands list
    private List<AraraCommand> commands;
    // flag to determine verbose output
    private boolean showVerboseOutput;
    // value for the execution timeout
    private long executionTimeout;
    // value for the maximum number of loops
    private long maximumNumberOfLoops;
    // flag to determine if we
    // are in dry-run mode
    private boolean dryRun;

    /**
     * Constructor.
     *
     * @param commands The commands list.
     */
    public CommandTrigger(List<AraraCommand> commands) {

        // set the list
        this.commands = commands;
    }

    /**
     * Setter for the execution timeout.
     *
     * @param executionTimeout The execution timeout.
     */
    public void setExecutionTimeout(long executionTimeout) {

        // set the value
        this.executionTimeout = executionTimeout;
    }

    /**
     * Setter for the maximum number of loops.
     *
     * @param number A long value indicating the maximum number of loops.
     */
    public void setMaximumNumberOfLoops(long number) {
        maximumNumberOfLoops = number;
    }

    /**
     * Setter for the verbose mode.
     *
     * @param verbose A flag to indicate the verbose mode.
     */
    public void setVerbose(boolean verbose) {

        // set the flag
        showVerboseOutput = verbose;
    }

    /**
     * Executes the arara commands provided in the list.
     *
     * @return A boolean value about the execution. To be honest, I don't think
     * it's necessary to return an actual value, but I kept it for possible
     * future use.
     * @throws com.github.arara.exception.AraraException Raised when the command
     * is not found in the underlying system.
     */
    public boolean execute() throws AraraException {

        // log action
        logger.info(localization.getMessage("Log_ReadyToRunCommands"));

        // for every command in the list
        for (AraraCommand currentAraraCommand : commands) {

            // we  are not in dry-run mode,
            // so let' execute the commands
            if (dryRun == false) {
                
                // set the current value of the file for
                // this command, it might be overriden by
                // a directive argument
                ConditionalMethods.setBasenameReference(AraraMethods.getBasename(currentAraraCommand.getFilename()));

                // if we have either IF or WHILE blocks, the conditional
                // is evaluated before the command
                if ((currentAraraCommand.getConditional().getType() == AraraConditionalType.IF)
                        || (currentAraraCommand.getConditional().getType() == AraraConditionalType.WHILE)) {

                    // if they evaluate to false, nothing to do here
                    if (ConditionalEvaluator.evaluate(currentAraraCommand.getConditional().getCondition()) == false) {

                        // proceed to the next command in the list
                        continue;
                    }

                }

                // we have a halt command, let's interrupt the loop
                if (AraraUtils.haltFromAraraTrigger(currentAraraCommand.getCommand())) {
                    break;
                }

                // set a few flags
                long counter = 0;
                boolean condition;

                do {

                    // print a message
                    System.out.print(localization.getMessage("Msg_RunningCommand", currentAraraCommand.getName()).concat(" "));

                    // if verbose
                    if (showVerboseOutput) {

                        // add two lines
                        System.out.println("\n");
                    }

                    // log action
                    logger.info(localization.getMessage("Log_CommandName", currentAraraCommand.getName()));

                    // log command
                    logger.trace(localization.getMessage("Log_Command", currentAraraCommand.getCommand()));

                    // if the execution was ok
                    if (runCommand(currentAraraCommand)) {

                        // print a message
                        System.out.println(localization.getMessage("Msg_Success"));

                        // log action
                        logger.info(localization.getMessage("Log_CommandSuccess", currentAraraCommand.getName()));

                    } else {

                        // something bad happened, print message
                        System.out.println(localization.getMessage("Msg_Failure"));

                        // log action
                        logger.warn(localization.getMessage("Log_CommandFailure", currentAraraCommand.getName()));

                        // and return false
                        return false;

                    }

                    // if verbose
                    if (showVerboseOutput) {

                        // add one line to the output
                        System.out.println("");
                    }

                    // we have no conditional or the conditional was an IF,
                    // so there's no reason to be in this loop
                    if (currentAraraCommand.getConditional().isEmpty() ||
                            currentAraraCommand.getConditional().getType() == AraraConditionalType.IF) {
                        
                        // stop, hammertime!
                        break;
                    }

                    // increment loop counter
                    counter++;

                    // evaluate the condition
                    condition = ConditionalEvaluator.evaluate(currentAraraCommand.getConditional().getCondition());

                    // now the tricky part: UNTIL requires the
                    // condition to be true in order to stop the
                    // loop, so we need to invert it here for our
                    // loop to work
                    if (currentAraraCommand.getConditional().getType() == AraraConditionalType.UNTIL) {
                        condition = !condition;
                    }

                } while (condition && (counter <= maximumNumberOfLoops));

            } else {
                
                // we are now in dry-run mode

                // we have a halt command, let's interrupt the loop
                if (AraraUtils.haltFromAraraTrigger(currentAraraCommand.getCommand())) {
                    break;
                }

                // let' print the command expansion
                System.out.println(currentAraraCommand.getName() + ": " + currentAraraCommand.getCommand());

            }

        }

        // log action
        logger.info(localization.getMessage("Log_AllCommandsSuccess"));

        // everything went ok, simply return true
        return true;

    }

    /**
     * Run the arara command.
     *
     * @param command The Arara command.
     * @return A boolean indicating if the execution was successful.
     * @throws com.github.arara.exception.AraraException Throws an exception in
     * case the command was not found.
     */
    private boolean runCommand(AraraCommand command) throws AraraException {

        // create a new byte array for the output logger
        ByteArrayOutputStream stringLogger = new ByteArrayOutputStream();

        // let's try
        try {

            // set a query
            String query = command.getCommand();

            // create a new command line from the query
            CommandLine commandLine = CommandLine.parse(query);

            // create a new tee
            TeeOutputStream tee;

            // create a new execution handler
            PumpStreamHandler streamHandler;

            // if verbose
            if (showVerboseOutput) {

                // the new tee should include the normal output
                // and the log
                tee = new TeeOutputStream(System.out, stringLogger);

                // verbose mode enables input mode as well
                streamHandler = new PumpStreamHandler(tee, tee, System.in);

            } else {

                // no verbose, so include only the log output
                tee = new TeeOutputStream(stringLogger);

                // define the handler only with the log output
                streamHandler = new PumpStreamHandler(tee);
            }

            // create a new executor
            DefaultExecutor executor = new DefaultExecutor();

            // set the handler
            executor.setStreamHandler(streamHandler);

            // check if there should be an execution timeout
            if (executionTimeout > 0) {

                // create a new watch dog
                ExecuteWatchdog watchDog = new ExecuteWatchdog(executionTimeout);

                // add it to the execution
                executor.setWatchdog(watchDog);
            }

            // set the shutdown hook
            ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();

            // add it to the executor
            executor.setProcessDestroyer(processDestroyer);

            // execute the command and get the exit code
            int exitValue = executor.execute(commandLine);

            // if we are in verbose mode
            if (showVerboseOutput) {

                // print the output status
                System.out.print(localization.getMessage("Msg_Status").concat(" "));

            }

            // add the output to the logger
            logger.trace(localization.getMessage("Log_OutputLogging"));
            logger.trace(stringLogger.toString());

            // return the status as a boolean
            return (exitValue == 0 ? true : false);

        } catch (ExecuteException executeException) {

            // in case of an error, or the watchdog in action,
            // the logger addition is not reached, so we need
            // to replicate it here
            logger.trace(localization.getMessage("Log_OutputLogging"));
            logger.trace(stringLogger.toString());

            // something bad happened, return false
            return false;

        } catch (IOException ioException) {

            // log error
            logger.error(localization.getMessage("Log_CommandNotFound", command.getName(), command.getCommand()));

            // command not found, throw exception
            throw new AraraException(localization.getMessage("Error_CommandNotFound", command.getName(), command.getCommand()));

        }
    }

    /**
     * Setter for the dry-run mode.
     *
     * @param dryRun A boolean indicating if the dry-run mode must be set.
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
}
