/**
 * \cond LICENSE
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
 * \endcond
 * 
 * Arara: The main class. Basically, this class will get the file name from the
 * arguments list and call the proper methods from the other helper classes.
 */
// package definition
package com.github.arara;

// needed imports
import com.github.arara.model.AraraDirective;
import com.github.arara.utils.*;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main arara class. The code logic is encapsulated in the helper class, so
 * if something goes wrong, the generic exception catch here will simply print
 * it. I tried to make the code as easier as possible.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 1.0
 */
public class Arara {

    // the logger
    final static Logger logger = LoggerFactory.getLogger(Arara.class);
    // the localization class
    final static AraraLocalization localization = AraraLocalization.getInstance();

    /**
     * The main method. The idea is to provide to arara only the supported file
     * name and let it handle. The application will remove the file extension
     * (if any) and call the other helper classes.
     *
     * @param args The command line arguments. In fact, arara supports a lot of
     * flags, all listed in the CommandLineAnalyzer.
     */
    public static void main(String[] args) {

        // the application status, zero for a normal execution,
        // or nonzero for an abnormal execution
        int exitStatus;

        // trying to have a normal execution
        try {

            // print the application header
            AraraUtils.printHeader();

            // create a configuration instance and load
            // the proper settings, if any
            ConfigurationLoader configuration = new ConfigurationLoader();
            configuration.load();

            // create a new analyzer
            CommandLineAnalyzer commandLine = new CommandLineAnalyzer(args, configuration);

            // if the minimum parameters are not satisfied or it's a simple
            // help or version request
            if (!commandLine.parse()) {

                // simply return
                exitStatus = 0;

                // exit right now, good job
                System.exit(exitStatus);
            }

            // it's a normal workflow, let's define the main file
            File file;

            // create a new file reference
            file = new File(commandLine.getFile());

            // welcome message
            logger.info(localization.getMessage("Log_WelcomeMessage"));

            // process file
            logger.info(localization.getMessage("Log_ProcessingFile", file.getName()));

            // file exists, let's proceed with the directive extractor
            DirectiveExtractor dirExtractor = new DirectiveExtractor(file, configuration);
            
            // extract all directives from the file
            dirExtractor.extract();

            // get all directives found
            List<AraraDirective> directives = dirExtractor.getDirectives();
            
            // set the overall result flag
            boolean overallResult = true;
            
            // check if any directive was found
            if (!directives.isEmpty()) {
            
                // now let's parse the directives
                DirectiveParser dirParser = new DirectiveParser(directives);

                // set the file
                dirParser.setFile(file);

                // parse the directives
                TaskDeployer taskDeployer = new TaskDeployer(dirParser.parse(), configuration);

                // deploy the tasks through a command trigger
                CommandTrigger commandTrigger = new CommandTrigger(taskDeployer.deploy());

                // set verbose option, if enabled
                commandTrigger.setVerbose(commandLine.isVerbose());

                // set an execution timeout, if set
                commandTrigger.setExecutionTimeout(commandLine.getExecutionTimeout());

                // execute the tasks
                overallResult = commandTrigger.execute();
            }
            else {
                
                // no directives found, add message to the log
                logger.info(localization.getMessage("Log_NoDirectivesFound", file.getName()));
                
                // print message
                System.out.println(AraraUtils.wrap(localization.getMessage("Msg_NoDirectivesFound", file.getName())));
            }

            // final message
            logger.info(localization.getMessage("Log_Done"));

            // if everything was ok, that is, every command in the command list
            // returned with an exit status of zero
            if (overallResult) {

                // everything fine
                exitStatus = 0;

            } else {

                // something wrong happened, so let's consider the whole
                // execution as abnormal
                exitStatus = 1;
            }

        } catch (Exception exception) {

            // something bad happened, print the error message
            System.out.println(AraraUtils.wrap(exception.getMessage()));

            // catch error in the log
            logger.error(localization.getMessage("Log_ExceptionRaised"));
            logger.error(exception.getMessage());

            // error, set status to an abnormal execution
            exitStatus = 1;

        }

        // send the exit code to the terminal, and we are done
        System.exit(exitStatus);
    }
}
