/**
 * Arara, the cool TeX automation tool
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
 */
package com.github.cereda.arara.controller;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;

/**
 * Implements the logging controller. This class actually sets the logging
 * configuration in order to allow appending results to a file.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class LoggingController {

    /**
     * Sets the logging configuration according to the provided boolean value.
     * If the value is set to true, the log entries will be appended to a file,
     * otherwise the logging feature will keep silent.
     * @param enable A boolean value that indicates the logging behaviour
     * throughout the application.
     */
    public static void enableLogging(boolean enable) {

        // get the logger context from a factory, set a
        // new context and reset it
        LoggerContext loggerContext =
                (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            
            // get a new configuration and set
            // the context
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            loggerContext.reset();
            
            // if enabled, the log entries will be
            // appended to a file, otherwise it will
            // remain silent
            if (enable) {
                
                // set the file name and configure
                // the logging controller to append
                // entries to the file
                String name = (String) ConfigurationController.
                        getInstance().get("execution.log.name");
                loggerContext.putProperty("name", name);
                configurator.doConfigure(getResource());
            }
        } catch (JoranException exception) {
            // quack, quack, quack!
        }
    }

    /**
     * Get the configuration resource as an input stream. The configuration
     * is actually a XML file.
     * @return An input stream of the provided configuration XML resource.
     */
    private static InputStream getResource() {
        String resource = "/com/github/cereda/arara/configuration/logback.xml";
        return LoggingController.class.getResourceAsStream(resource);
    }

    /**
     * Initializes the logging controller by disabling it. I don't want an odd
     * behaviour out of the box.
     */
    public static void init() {
        enableLogging(false);
    }

}
