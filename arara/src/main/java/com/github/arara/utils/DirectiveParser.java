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
 * DirectiveParser.java: This class analyzes the list of directives and
 * converts them to a list of Arara tasks.
 */

// package definition
package com.github.arara.utils;

// needed imports
import com.github.arara.exceptions.AraraMalformedDirective;
import com.github.arara.model.AraraDirective;
import com.github.arara.model.AraraTask;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyzes the list of directives and converts them to a list
 * of Arara tasks.
 * @author Paulo Roberto Massa Cereda
 * @version 2.0
 * @since 1.0
 */
public class DirectiveParser {

    // the logger
    final static Logger logger = LoggerFactory.getLogger(DirectiveParser.class);
    
    // the list of directives
    private List<AraraDirective> directives;
    
    // the list of tasks
    private List<AraraTask> tasks;
    
    // the list of file names, to use as a temporary variable
    private List<String> fileNames;
    
    // the current file reference
    private File file;

    /**
     * Constructor
     */
    public DirectiveParser() {
    }

    /**
     * Constructor.
     * @param directives The Arara directives.
     */
    public DirectiveParser(List<AraraDirective> directives) {
        
        // set the directives
        this.directives = directives;
        
        // create a new list of tasks
        this.tasks = new ArrayList<AraraTask>();
        
        // create a new list of file names
        this.fileNames = new ArrayList<String>();
    }

    /**
     * Setter for file.
     * @param file The file.
     */
    public void setFile(File file) {
        
        // set the file
        this.file = file;
    }

    /**
     * Setter for the list of Arara directives.
     * @param directives The list of Arara directives.
     */
    public void setDirectives(List<AraraDirective> directives) {
        
        // set the list
        this.directives = directives;
    }

    /**
     * Parses the list of Arara directives.
     * @return A list of Arara tasks based on the directives.
     * @throws AraraMalformedDirective Raised when a malformed directive
     * is found.
     */
    public List<AraraTask> parse() throws AraraMalformedDirective {
        
        // log message
        logger.info("Parsing directives.");
        
        // for every directive
        for (AraraDirective currentDirective : directives) {

            // clear the list of files
            fileNames.clear();
            
            // clear the task name
            String taskName = "";
            
            // create a new map
            Map taskConfig = new HashMap();

            // if there's no configuration
            if (currentDirective.getConfig().isEmpty()) {
                
                // set the name
                taskName = currentDirective.getName();
                
            } else {
                
                // get the current config
                Map currentConfig = currentDirective.getConfig();
                
                // and get the directive name
                String directiveName = (String) currentConfig.keySet().iterator().next();

                // set the name to the task
                taskName = directiveName;

                // get the parameters
                Object currentConfigParameters = currentConfig.get(directiveName);
                
                // if it is a map
                if (currentConfigParameters instanceof Map) {
                    
                    // get the map
                    Map currentParameters = (Map) currentConfigParameters;
                    
                    // if it is empty, error
                    if (currentParameters.isEmpty()) {
                        
                        // raise an error
                        throw new AraraMalformedDirective("ERROR: Directive '" + directiveName + "' at line " + currentDirective.getLineNumber() + " has empty curly brackets.");
                        
                    } else {
                        
                        // for every parameter
                        for (Object currentParameterKey : currentParameters.keySet()) {
                            
                            // if the parameter is a string
                            if (currentParameters.get(currentParameterKey) instanceof String) {
                                
                                // if it is a file reference, error
                                if (((String) currentParameterKey).equals("files")) {
                                    
                                    // raise an error
                                    throw new AraraMalformedDirective("ERROR: Directive '" + directiveName + "' at line " + currentDirective.getLineNumber() + " has an invalid argument.\nArgument 'files' requires a list.");
                                }
                                
                                // add the parameter to the configuration
                                taskConfig.put(currentParameterKey, (String) currentParameters.get(currentParameterKey));
                                
                            } else {
                                
                                // if it is a list
                                if (currentParameters.get(currentParameterKey) instanceof List) {
                                    
                                    // if it is a file reference
                                    if (((String) currentParameterKey).equals("files")) {
                                        
                                        // get the list
                                        for (Object currentListValue : ((List) currentParameters.get(currentParameterKey))) {
                                            
                                            // add the file names
                                            fileNames.add(currentListValue.toString());
                                        }                                       
                                    } else {
                                        
                                        // raise error
                                        throw new AraraMalformedDirective("ERROR: Directive '" + directiveName + "' at line " + currentDirective.getLineNumber() + " has an invalid argument.\nOnly the argument 'files' can have a list.");
                                    }
                                } else {
                                                                            
                                    // raise error
                                    throw new AraraMalformedDirective("ERROR: Directive '" + directiveName + "' at line " + currentDirective.getLineNumber() + " has malformed arguments.");   
                                }
                            }
                        }
                    }
                } else {
                    
                    // raise error
                    throw new AraraMalformedDirective("ERROR: Directive '" + directiveName + "' at line " + currentDirective.getLineNumber() + " seems to be malformed.");
                }
            }

            // if the list of file names is empty
            if (fileNames.isEmpty()) {
                
                // create a new task
                AraraTask araraTask = new AraraTask();
                
                // add the name
                araraTask.setName(taskName);
                
                // put the single file
                taskConfig.put("file", file.getName());
                
                // set the parameters
                araraTask.setParameters(taskConfig);
                
                // and add to the list of tasks
                tasks.add(araraTask);
            } else {
                
                // for every file name found
                for (String fileName : fileNames) {
                    
                    // create a new task
                    AraraTask araraTask = new AraraTask();
                    
                    // set the name
                    araraTask.setName(taskName);
                    
                    // create a new map based on the current configuration
                    Map currentTaskConfig = new HashMap(taskConfig);
                    
                    // add the current file
                    currentTaskConfig.put("file", fileName);
                    
                    // set the parameters
                    araraTask.setParameters(currentTaskConfig);
                    
                    // and add the task to the list
                    tasks.add(araraTask);
                    
                    // the current task is now null
                    araraTask = null;
                }
                
                // clear the list of files
                fileNames.clear();
            }
        }
        
        // return the list of tasks
        return tasks;
    }
}
