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
 * CommandTrigger.java: This class is responsible for running the Arara
 * commands.
 */

// package definition
package com.github.arara.utils;

// needed imports
import com.github.arara.exceptions.AraraCommandNotFound;
import com.github.arara.exceptions.AraraInterruptedTask;
import com.github.arara.model.AraraCommand;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an environment for running the Arara commands.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class CommandTrigger {
    
    // the logger
    final static Logger logger = LoggerFactory.getLogger(CommandTrigger.class);

    // the commands list
    private List<AraraCommand> commands;
    
    // the main file
    private File theFile;

    /**
     * Constructor.
     * @param commands The commands list.
     */
    public CommandTrigger(List<AraraCommand> commands) {
        
        // set the list
        this.commands = commands;
    }

    /**
     * Executes the Arara commands provided in the list.
     * @return A boolean value about the execution. To be honest, I don't
     * think it's necessary to return an actual value, but I kept it for
     * possible future use.
     * @throws AraraCommandNotFound Raised when the command is not found in the
     * underlying system.
     * @throws AraraInterruptedTask Raised when the task was interrupted.
     */
    public boolean execute() throws AraraCommandNotFound, AraraInterruptedTask {

        // log action
        logger.info("Ready to run commands.");
        
        // for every command in the list
        for (AraraCommand currentAraraCommand : commands) {
            
            // print a message
            System.out.print("Running " + currentAraraCommand.getName() + "... ");
            
            // log action
            logger.info("Running {}.", currentAraraCommand.getName());
            
            // log command
            logger.trace("Command: {}", currentAraraCommand.getCommand());
                        
            // if the execution was ok
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
        }
        
        // log action
        logger.info("All commands were successfully executed.");
        
        // everything went ok,
        // simply return true
        return true;

    }

    /**
     * Runs the current Arara command.
     * @param command The current Arara command.
     * @return A boolean representing the status of the execution.
     * @throws AraraCommandNotFound Raised when the command is not found in the
     * underlying system.
     * @throws AraraInterruptedTask Raised when the task was interrupted.
     */
    public boolean runCommand(AraraCommand command) throws AraraCommandNotFound, AraraInterruptedTask {
        
        // let's try
        try {
                        
            // set a query
            String query = command.getCommand();
            
            // create a process and execute it
            
            final Process p;
            
            // for some reason, I had to add this check
            if (SystemUtils.IS_OS_WINDOWS) {
                
                // Windows requires this line
                p = Runtime.getRuntime().exec(query, new String[]{}, theFile.getParentFile());
                
            }
            else {
                
                // for Unix
                p = Runtime.getRuntime().exec(query);
            }
            
            // create a reader for standard error
            final AraraProcessResultReader araraStdErr = new AraraProcessResultReader(p.getErrorStream(), "STDERR");
            
            // create a reader for standard output
            final AraraProcessResultReader araraStdOut = new AraraProcessResultReader(p.getInputStream(), "STDOUT");
            
            // start both readers
            araraStdErr.start();
            araraStdOut.start();
            
            // get the return value
            final int exitValue = p.waitFor();
            
            // log values
            logger.trace("Standard error logging: {}", araraStdErr.toString());
            logger.trace("Standard output logging: {}", araraStdOut.toString());
                        
            // return the status as a boolean
            return (exitValue == 0 ? true : false);
            
        } catch (final IOException ioex) {
            
            // log error
            logger.error("{} was not found in the path. Execution attempt: {}", command.getName(), command.getCommand());
            
            // command not found, throw exception
            throw new AraraCommandNotFound("ERROR\n\nThe '" + command.getName() + "' task was not found.\nAre you sure the command '" + command.getCommand() + "' is correct?");
            
        } catch (final InterruptedException ine) {
            
            // log error
            logger.error("{} was interrupted. Execution attempt: {}", command.getName(), command.getCommand());
            
            // task was interrupted, throw exception
            throw new AraraInterruptedTask("ERROR\n\nThe '" + command.getName() + "' task was interrupted.");
        }
    }

    /**
     * Setter for the main file.
     * @param theFile The main file.
     */
    public void setFile(File theFile) {
        
        // set it
        this.theFile = theFile;
    }
       
}
