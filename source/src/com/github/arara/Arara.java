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
 * Arara.java: The main class. Basically, this class will get the file name
 * from the arguments list and call the proper methods from the other helper
 * classes.
 */

// package definition
package com.github.arara;

// needed imports
import com.github.arara.utils.*;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main Arara class. The code logic is encapsulated in the helper class,
 * so if something goes wrong, the generic exception catch here will simply
 * print it. I tried to make the code as easier as possible.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Arara {
    
    // the logger
    final static Logger logger = LoggerFactory.getLogger(Arara.class);
    
    /**
     * The main method. The idea is to provide Arara only the <code>tex</code>
     * file name and let it handle it. Arara will remove the file extension
     * (if any) and call the other helper classes.
     * @param args The command line arguments. In fact, Arara only expects one
     * argument, a <code>tex</code> file.
     */
    public static void main(String[] args) {
        
        // the application status, zero for a normal
        // execution, or nonzero for an abnormal execution
        int exitStatus;
        
        // lets try
        try {
            
            // create a new analyzer
            CommandLineAnalyzer commandLine = new CommandLineAnalyzer(args);
            
            // if there's no file to parse
            if (!commandLine.parse()) {
                
                // simply return
                return;
            }
            
            // define the main file
            File file;
            
            // create a new file
            file = new File(commandLine.getFile());
                    
            // file does not exist
            if (!file.exists()) {
                
                // print message about it
                System.out.println("ERROR: File '" + file.getName() + "' does not exist.");
                
                // and return, end of execution
                return;
            }
            
            // welcome message
            logger.info("Welcome to Arara!");
            
            // process file
            logger.info("Processing file {}, please wait.", file.getName());
            
            // file exist, let's proceed with the
            // directive extractor
            DirectiveExtractor dirExtractor = new DirectiveExtractor(file);
            
            // extract all directives from the file
            dirExtractor.extract();
            
            // now let's parse the directives
            DirectiveParser dirParser = new DirectiveParser(dirExtractor.getDirectives());
            
            // the .tex extension is now removed just to make
            // the rules definition more easy
            
            // the file without extension doesn't need to exist, as
            // it's just a reference
            file = new File(commandLine.getFile().substring(0, commandLine.getFile().length() - ".tex".length()));
            
            // set the file
            dirParser.setFile(file);
            
            // parse the directives
            TaskDeployer taskDeployer = new TaskDeployer(dirParser.parse());
            
            // deploy the tasks through a command trigger
            CommandTrigger commandTrigger = new CommandTrigger(taskDeployer.deploy());
            
            commandTrigger.setFile(new File(commandLine.getFile()));
            
            // execute the tasks
            commandTrigger.execute();
            
            // final message
            logger.info("Done.");
            
            // everything fine
            exitStatus = 0;
            
        } catch (Exception e) {
                        
            // something bad happened, exit
            System.out.println(e.getMessage());
            
            // catch error in the log
            logger.error("Arara raised an exception: {}", e.getMessage().replaceAll("\\n", " "));
            
            // error, set status
            exitStatus = 1;

        }
        
        // send the exit code to the terminal
        System.exit(exitStatus);
    }
}
