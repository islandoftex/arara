/**
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
 *
 * CommandTrigger.java: This class is responsible for running the Arara
 * commands.
 */

// package definition
package com.github.arara.utils;

// needed imports
import com.github.arara.exceptions.AraraCommandNotFound;
import com.github.arara.model.AraraCommand;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an environment for running the Arara commands.
 * @author Paulo Roberto Massa Cereda
 * @version 2.0
 * @since 1.0
 */
public class CommandTrigger {
    
    // the logger
    final static Logger logger = LoggerFactory.getLogger(CommandTrigger.class);

    // the commands list
    private List<AraraCommand> commands;
    
    // the main file
    //private File theFile;
    
    // flag to determine verbose output
    private boolean showVerboseOutput;
    
    // value for the execution timeout
    private long executionTimeout;

    /**
     * Constructor.
     * @param commands The commands list.
     */
    public CommandTrigger(List<AraraCommand> commands) {
        
        // set the list
        this.commands = commands;
    }

    /**
     * Setter for the execution timeout.
     * @param executionTimeout The execution timeout.
     */
    public void setExecutionTimeout(long executionTimeout) {
        
        // set the value
        this.executionTimeout = executionTimeout;
    }
    
    /**
     * Setter for the verbose mode.
     * @param verbose A flag to indicate the verbose mode.
     */
    public void setVerbose(boolean verbose) {
        
        // set the flag
        showVerboseOutput = verbose;
    }

    /**
     * Executes the Arara commands provided in the list.
     * @return A boolean value about the execution. To be honest, I don't
     * think it's necessary to return an actual value, but I kept it for
     * possible future use.
     * @throws AraraCommandNotFound Raised when the command is not found in the
     * underlying system.
     */
    public boolean execute() throws AraraCommandNotFound {

        // log action
        logger.info("Ready to run commands.");
        
        // for every command in the list
        for (AraraCommand currentAraraCommand : commands) {
            
            // print a message
            System.out.print("Running " + currentAraraCommand.getName() + "... ");
            
            // if verbose
            if (showVerboseOutput) {
                
                // add two lines
                System.out.println("\n");
            }
            
            // log action
            logger.info("Running {}.", currentAraraCommand.getName());
            
            // log command
            logger.trace("Command: {}", currentAraraCommand.getCommand());
            
            // if the execution was ok
            //if (runCommand(currentAraraCommand)) {
            if (runCommand(currentAraraCommand)) {
                              
                // print a message
                System.out.println("SUCCESS");
                
                // log action
                logger.info("{} was successfully executed.", currentAraraCommand.getName());
                
            } else {
                
                // something bad happened,
                // print message
                System.out.println("FAILURE");
                
                // log action
                logger.warn("{} returned an error status.", currentAraraCommand.getName());
                
                // and return false
                return false;
                
            }
            
            // if verbose
            if (showVerboseOutput) {
                
                // add one line to the output
                System.out.println("");
            }
            
        }
        
        // log action
        logger.info("All commands were successfully executed.");
        
        // everything went ok,
        // simply return true
        return true;

    }
    
    /**
     * Run the Arara command.
     * @param command The Arara command.
     * @return A boolean indicating if the execution was successful.
     * @throws AraraCommandNotFound Throws an exception in case the
     * command was not found.
     */
    private boolean runCommand(AraraCommand command) throws AraraCommandNotFound {
        
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
                
            }
            else {
                
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
            
            // execute the command and get the exit code
            int exitValue = executor.execute(commandLine);
            
            // if we are in verbose mode
            if (showVerboseOutput) {
                
                // print the output status
                System.out.print("Status: ");
                
            }
            
            // add the output to the logger
            logger.trace("Output logging: {}", stringLogger.toString());
                                    
            // return the status as a boolean
            return (exitValue == 0 ? true : false);
            
        } catch (ExecuteException eee) {
            
            // in case of an error, or the watchdog in action,
            // the logger addition is not reached, so we need
            // to replicate it here
            logger.trace("Output logging: {}", stringLogger.toString());
            
            // something bad happened, return false
            return false;
            
        } catch (IOException ioex) {
            
            // log error
            logger.error("{} was not found in the path. Execution attempt: {}", command.getName(), command.getCommand());
            
            // command not found, throw exception
            throw new AraraCommandNotFound("ERROR\n\nThe '" + command.getName() + "' task was not found.\nAre you sure the command '" + command.getCommand() + "' is correct?");
            
        }
    }
       
}
